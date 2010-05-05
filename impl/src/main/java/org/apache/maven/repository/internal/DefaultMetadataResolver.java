package org.apache.maven.repository.internal;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.maven.repository.LocalRepositoryManager;
import org.apache.maven.repository.Metadata;
import org.apache.maven.repository.MetadataNotFoundException;
import org.apache.maven.repository.MetadataRequest;
import org.apache.maven.repository.MetadataResult;
import org.apache.maven.repository.MetadataTransferException;
import org.apache.maven.repository.NoRepositoryReaderException;
import org.apache.maven.repository.RemoteRepository;
import org.apache.maven.repository.RepositoryContext;
import org.apache.maven.repository.RepositoryPolicy;
import org.apache.maven.repository.spi.Logger;
import org.apache.maven.repository.spi.MetadataDownload;
import org.apache.maven.repository.spi.MetadataResolver;
import org.apache.maven.repository.spi.NullLogger;
import org.apache.maven.repository.spi.RemoteRepositoryManager;
import org.apache.maven.repository.spi.RepositoryReader;
import org.apache.maven.repository.spi.UpdateCheck;
import org.apache.maven.repository.spi.UpdateCheckManager;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

/**
 * @author Benjamin Bentmann
 */
@Component( role = MetadataResolver.class )
public class DefaultMetadataResolver
    implements MetadataResolver
{

    @Requirement
    private Logger logger = NullLogger.INSTANCE;

    @Requirement
    private UpdateCheckManager updateCheckManager;

    @Requirement
    private RemoteRepositoryManager remoteRepositoryManager;

    public DefaultMetadataResolver setLogger( Logger logger )
    {
        this.logger = ( logger != null ) ? logger : NullLogger.INSTANCE;
        return this;
    }

    public DefaultMetadataResolver setUpdateCheckManager( UpdateCheckManager updateCheckManager )
    {
        if ( updateCheckManager == null )
        {
            throw new IllegalArgumentException( "update check manager has not been specified" );
        }
        this.updateCheckManager = updateCheckManager;
        return this;
    }

    public DefaultMetadataResolver setRemoteRepositoryManager( RemoteRepositoryManager remoteRepositoryManager )
    {
        if ( remoteRepositoryManager == null )
        {
            throw new IllegalArgumentException( "remote repository manager has not been specified" );
        }
        this.remoteRepositoryManager = remoteRepositoryManager;
        return this;
    }

    public List<MetadataResult> resolveMetadata( RepositoryContext context,
                                                 Collection<? extends MetadataRequest> requests )
    {
        List<MetadataResult> results = new ArrayList<MetadataResult>( requests.size() );

        List<ResolveTask> tasks = new ArrayList<ResolveTask>( requests.size() );
        CountDownLatch latch = new CountDownLatch( requests.size() );

        Map<File, Long> localLastUpdates = new HashMap<File, Long>();

        for ( MetadataRequest request : requests )
        {
            MetadataResult result = new MetadataResult( request );
            results.add( result );

            Metadata metadata = request.getMetadata();
            RemoteRepository repository = request.getRemoteRepository();

            RepositoryPolicy policy = getPolicy( context, repository, metadata.getNature() );

            if ( !policy.isEnabled() )
            {
                continue;
            }

            File metadataFile = getFile( context, metadata, repository );

            if ( context.isOffline() )
            {
                if ( metadataFile.isFile() )
                {
                    metadata.setFile( metadataFile );
                }
                else
                {
                    String msg =
                        "The repository system is offline but the metadata " + metadata + " from " + repository
                            + " is not available in the local repository.";
                    result.setException( new MetadataNotFoundException( metadata, repository, msg ) );
                }
                continue;
            }

            File localFile = getFile( context, metadata, null );
            Long localLastUpdate = localLastUpdates.get( localFile );
            if ( localLastUpdate == null )
            {
                localLastUpdate = Long.valueOf( localFile.lastModified() );
                localLastUpdates.put( localFile, localLastUpdate );
            }

            UpdateCheck<Metadata, MetadataTransferException> check =
                new UpdateCheck<Metadata, MetadataTransferException>();
            check.setLocalLastUpdated( localLastUpdate.longValue() );
            check.setItem( metadata );
            check.setFile( metadataFile );
            check.setRepository( repository );
            check.setPolicy( policy.getUpdatePolicy() );
            updateCheckManager.checkMetadata( context, check );

            if ( check.isRequired() )
            {
                ResolveTask task = new ResolveTask( context, result, check, policy.getChecksumPolicy(), latch );
                tasks.add( task );
            }
            else
            {
                result.setException( check.getException() );
                if ( metadataFile.isFile() )
                {
                    metadata.setFile( metadataFile );
                }
            }
        }

        if ( !tasks.isEmpty() )
        {
            Executor executor = getExecutor( Math.min( tasks.size(), 4 ) );
            try
            {
                for ( ResolveTask task : tasks )
                {
                    executor.execute( task );
                }
                for ( int i = requests.size() - tasks.size(); i > 0; i-- )
                {
                    latch.countDown();
                }
                latch.await();
                for ( ResolveTask task : tasks )
                {
                    task.result.setException( task.exception );
                }
            }
            catch ( InterruptedException e )
            {
                for ( ResolveTask task : tasks )
                {
                    MetadataResult result = task.result;
                    result.setException( new MetadataTransferException( result.getRequest().getMetadata(),
                                                                        result.getRequest().getRemoteRepository(), e ) );
                }
            }
            finally
            {
                shutdown( executor );
            }
            for ( ResolveTask task : tasks )
            {
                File metadataFile = task.check.getFile();
                if ( metadataFile.isFile() )
                {
                    task.result.getRequest().getMetadata().setFile( metadataFile );
                }
            }
        }

        return results;
    }

    private RepositoryPolicy getPolicy( RepositoryContext context, RemoteRepository repository, Metadata.Nature nature )
    {
        boolean releases = !Metadata.Nature.SNAPSHOT.equals( nature );
        boolean snapshots = !Metadata.Nature.RELEASE.equals( nature );
        return remoteRepositoryManager.getPolicy( context, repository, releases, snapshots );
    }

    private File getFile( RepositoryContext context, Metadata metadata, RemoteRepository repository )
    {
        LocalRepositoryManager lrm = context.getLocalRepositoryManager();
        String path;
        if ( repository != null )
        {
            path = lrm.getPathForRemoteMetadata( metadata, repository );
        }
        else
        {
            path = lrm.getPathForLocalMetadata( metadata );
        }
        return new File( lrm.getRepository().getBasedir(), path );
    }

    private Executor getExecutor( int threads )
    {
        if ( threads <= 1 )
        {
            return new Executor()
            {
                public void execute( Runnable command )
                {
                    command.run();
                }
            };
        }
        else
        {
            return new ThreadPoolExecutor( threads, threads, 3, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>() );
        }
    }

    private void shutdown( Executor executor )
    {
        if ( executor instanceof ExecutorService )
        {
            ( (ExecutorService) executor ).shutdown();
        }
    }

    class ResolveTask
        implements Runnable
    {

        final RepositoryContext context;

        final MetadataResult result;

        final String policy;

        final UpdateCheck<Metadata, MetadataTransferException> check;

        final CountDownLatch latch;

        volatile MetadataTransferException exception;

        public ResolveTask( RepositoryContext context, MetadataResult result,
                            UpdateCheck<Metadata, MetadataTransferException> check, String policy, CountDownLatch latch )
        {
            this.context = context;
            this.result = result;
            this.policy = policy;
            this.check = check;
            this.latch = latch;
        }

        public void run()
        {
            MetadataRequest request = result.getRequest();

            try
            {
                MetadataDownload download = new MetadataDownload( request.getMetadata(), check.getFile(), policy );

                RepositoryReader reader =
                    remoteRepositoryManager.getRepositoryReader( context, request.getRemoteRepository() );
                try
                {
                    reader.getMetadata( Arrays.asList( download ) );
                }
                finally
                {
                    reader.close();
                }

                exception = download.getException();

                if ( request.isDeleteLocalCopyIfMissing() && exception instanceof MetadataNotFoundException )
                {
                    download.getFile().delete();
                }
            }
            catch ( NoRepositoryReaderException e )
            {
                exception = new MetadataTransferException( request.getMetadata(), request.getRemoteRepository(), e );
            }
            finally
            {
                latch.countDown();
            }

            updateCheckManager.touchMetadata( context, check.setException( exception ) );
        }

    }

}
