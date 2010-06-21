package org.sonatype.maven.repository.internal;

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

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.maven.repository.ArtifactRepository;
import org.sonatype.maven.repository.LocalRepositoryManager;
import org.sonatype.maven.repository.Metadata;
import org.sonatype.maven.repository.MetadataNotFoundException;
import org.sonatype.maven.repository.MetadataRequest;
import org.sonatype.maven.repository.MetadataResult;
import org.sonatype.maven.repository.MetadataTransferException;
import org.sonatype.maven.repository.NoRepositoryConnectorException;
import org.sonatype.maven.repository.RemoteRepository;
import org.sonatype.maven.repository.RepositoryListener;
import org.sonatype.maven.repository.RepositoryPolicy;
import org.sonatype.maven.repository.RepositorySystemSession;
import org.sonatype.maven.repository.spi.Logger;
import org.sonatype.maven.repository.spi.MetadataDownload;
import org.sonatype.maven.repository.spi.MetadataResolver;
import org.sonatype.maven.repository.spi.NullLogger;
import org.sonatype.maven.repository.spi.RemoteRepositoryManager;
import org.sonatype.maven.repository.spi.RepositoryConnector;
import org.sonatype.maven.repository.spi.UpdateCheck;
import org.sonatype.maven.repository.spi.UpdateCheckManager;
import org.sonatype.maven.repository.util.DefaultRepositoryEvent;

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

    public List<MetadataResult> resolveMetadata( RepositorySystemSession session,
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
            RemoteRepository repository = request.getRepository();

            if ( repository == null )
            {
                metadataResolving( session, metadata, session.getLocalRepositoryManager().getRepository() );

                File localFile = getFile( session, metadata, null, null );
                if ( localFile.isFile() )
                {
                    metadata.setFile( localFile );
                }
                else
                {
                    result.setException( new MetadataNotFoundException( metadata, null ) );
                }

                metadataResolved( session, metadata, session.getLocalRepositoryManager().getRepository(),
                                  result.getException() );
                continue;
            }

            RepositoryPolicy policy = getPolicy( session, repository, metadata.getNature() );

            if ( !policy.isEnabled() )
            {
                continue;
            }

            metadataResolving( session, metadata, repository );

            File metadataFile = getFile( session, metadata, repository, request.getRequestContext() );

            if ( session.isOffline() )
            {
                if ( metadataFile.isFile() )
                {
                    metadata.setFile( metadataFile );
                    metadataResolved( session, metadata, repository, null );
                }
                else
                {
                    String msg =
                        "The repository system is offline but the metadata " + metadata + " from " + repository
                            + " is not available in the local repository.";
                    result.setException( new MetadataNotFoundException( metadata, repository, msg ) );
                    metadataResolved( session, metadata, repository, result.getException() );
                }
                continue;
            }

            Long localLastUpdate = null;
            if ( request.isFavorLocalRepository() )
            {
                File localFile = getFile( session, metadata, null, null );
                localLastUpdate = localLastUpdates.get( localFile );
                if ( localLastUpdate == null )
                {
                    localLastUpdate = Long.valueOf( localFile.lastModified() );
                    localLastUpdates.put( localFile, localLastUpdate );
                }
            }

            UpdateCheck<Metadata, MetadataTransferException> check =
                new UpdateCheck<Metadata, MetadataTransferException>();
            check.setLocalLastUpdated( ( localLastUpdate != null ) ? localLastUpdate.longValue() : 0 );
            check.setItem( metadata );
            check.setFile( metadataFile );
            check.setRepository( repository );
            check.setPolicy( policy.getUpdatePolicy() );
            updateCheckManager.checkMetadata( session, check );

            if ( check.isRequired() )
            {
                ResolveTask task = new ResolveTask( session, result, check, policy.getChecksumPolicy(), latch );
                tasks.add( task );
            }
            else
            {
                result.setException( check.getException() );
                if ( metadataFile.isFile() )
                {
                    metadata.setFile( metadataFile );
                }
                metadataResolved( session, metadata, repository, result.getException() );
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
                                                                        result.getRequest().getRepository(), e ) );
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
                    task.request.getMetadata().setFile( metadataFile );
                }
                if ( task.result.getException() == null )
                {
                    task.result.setUpdated( true );
                }
                metadataResolved( session, task.request.getMetadata(), task.request.getRepository(),
                                  task.result.getException() );
            }
        }

        return results;
    }

    private RepositoryPolicy getPolicy( RepositorySystemSession session, RemoteRepository repository, Metadata.Nature nature )
    {
        boolean releases = !Metadata.Nature.SNAPSHOT.equals( nature );
        boolean snapshots = !Metadata.Nature.RELEASE.equals( nature );
        return remoteRepositoryManager.getPolicy( session, repository, releases, snapshots );
    }

    private File getFile( RepositorySystemSession session, Metadata metadata, RemoteRepository repository, String context )
    {
        LocalRepositoryManager lrm = session.getLocalRepositoryManager();
        String path;
        if ( repository != null )
        {
            path = lrm.getPathForRemoteMetadata( metadata, repository, context );
        }
        else
        {
            path = lrm.getPathForLocalMetadata( metadata );
        }
        return new File( lrm.getRepository().getBasedir(), path );
    }

    private void metadataResolving( RepositorySystemSession session, Metadata metadata, ArtifactRepository repository )
    {
        RepositoryListener listener = session.getRepositoryListener();
        if ( listener != null )
        {
            DefaultRepositoryEvent event = new DefaultRepositoryEvent( session, metadata );
            event.setRepository( repository );
            listener.metadataResolving( event );
        }
    }

    private void metadataResolved( RepositorySystemSession session, Metadata metadata, ArtifactRepository repository,
                                   Exception exception )
    {
        RepositoryListener listener = session.getRepositoryListener();
        if ( listener != null )
        {
            DefaultRepositoryEvent event = new DefaultRepositoryEvent( session, metadata );
            event.setRepository( repository );
            event.setException( exception );
            event.setFile( metadata.getFile() );
            listener.metadataResolved( event );
        }
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

        final RepositorySystemSession session;

        final MetadataResult result;

        final MetadataRequest request;

        final String policy;

        final UpdateCheck<Metadata, MetadataTransferException> check;

        final CountDownLatch latch;

        volatile MetadataTransferException exception;

        public ResolveTask( RepositorySystemSession session, MetadataResult result,
                            UpdateCheck<Metadata, MetadataTransferException> check, String policy, CountDownLatch latch )
        {
            this.session = session;
            this.result = result;
            this.request = result.getRequest();
            this.policy = policy;
            this.check = check;
            this.latch = latch;
        }

        public void run()
        {
            try
            {
                MetadataDownload download = new MetadataDownload();
                download.setMetadata( request.getMetadata() );
                download.setRequestContext( request.getRequestContext() );
                download.setFile( check.getFile() );
                download.setChecksumPolicy( policy );
                download.setRepositories( request.getRepository().getMirroredRepositories() );

                RepositoryConnector connector =
                    remoteRepositoryManager.getRepositoryConnector( session, request.getRepository() );
                try
                {
                    connector.get( null, Arrays.asList( download ) );
                }
                finally
                {
                    connector.close();
                }

                exception = download.getException();

                if ( request.isDeleteLocalCopyIfMissing() && exception instanceof MetadataNotFoundException )
                {
                    download.getFile().delete();
                }
            }
            catch ( NoRepositoryConnectorException e )
            {
                exception = new MetadataTransferException( request.getMetadata(), request.getRepository(), e );
            }
            finally
            {
                latch.countDown();
            }

            updateCheckManager.touchMetadata( session, check.setException( exception ) );
        }

    }

}
