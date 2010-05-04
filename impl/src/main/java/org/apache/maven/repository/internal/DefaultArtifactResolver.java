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
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.repository.Artifact;
import org.apache.maven.repository.ArtifactDownload;
import org.apache.maven.repository.ArtifactNotFoundException;
import org.apache.maven.repository.ArtifactResolutionException;
import org.apache.maven.repository.ArtifactTransferException;
import org.apache.maven.repository.LocalArtifactQuery;
import org.apache.maven.repository.LocalRepositoryManager;
import org.apache.maven.repository.NoRepositoryReaderException;
import org.apache.maven.repository.RemoteRepository;
import org.apache.maven.repository.RepositoryContext;
import org.apache.maven.repository.RepositoryPolicy;
import org.apache.maven.repository.ResolveRequest;
import org.apache.maven.repository.ResolveResult;
import org.apache.maven.repository.VersionRequest;
import org.apache.maven.repository.VersionResolutionException;
import org.apache.maven.repository.VersionResult;
import org.apache.maven.repository.WorkspaceReader;
import org.apache.maven.repository.spi.ArtifactResolver;
import org.apache.maven.repository.spi.Logger;
import org.apache.maven.repository.spi.NullLogger;
import org.apache.maven.repository.spi.RemoteRepositoryManager;
import org.apache.maven.repository.spi.RepositoryReader;
import org.apache.maven.repository.spi.UpdateCheck;
import org.apache.maven.repository.spi.UpdateCheckManager;
import org.apache.maven.repository.spi.VersionResolver;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

/**
 * @author Benjamin Bentmann
 */
@Component( role = ArtifactResolver.class )
public class DefaultArtifactResolver
    implements ArtifactResolver
{

    @Requirement
    private Logger logger = NullLogger.INSTANCE;

    @Requirement
    private VersionResolver versionResolver;

    @Requirement
    private UpdateCheckManager updateCheckManager;

    @Requirement
    private RemoteRepositoryManager remoteRepositoryManager;

    public DefaultArtifactResolver setLogger( Logger logger )
    {
        this.logger = ( logger != null ) ? logger : NullLogger.INSTANCE;
        return this;
    }

    public DefaultArtifactResolver setVersionResolver( VersionResolver versionResolver )
    {
        if ( versionResolver == null )
        {
            throw new IllegalArgumentException( "version resolver has not been specified" );
        }
        this.versionResolver = versionResolver;
        return this;
    }

    public DefaultArtifactResolver setUpdateCheckManager( UpdateCheckManager updateCheckManager )
    {
        if ( updateCheckManager == null )
        {
            throw new IllegalArgumentException( "update check manager has not been specified" );
        }
        this.updateCheckManager = updateCheckManager;
        return this;
    }

    public DefaultArtifactResolver setRemoteRepositoryManager( RemoteRepositoryManager remoteRepositoryManager )
    {
        if ( remoteRepositoryManager == null )
        {
            throw new IllegalArgumentException( "remote repository manager has not been specified" );
        }
        this.remoteRepositoryManager = remoteRepositoryManager;
        return this;
    }

    public List<ResolveResult> resolveArtifacts( RepositoryContext context,
                                                 Collection<? extends ResolveRequest> requests )
        throws ArtifactResolutionException
    {
        List<ResolveResult> results = new ArrayList<ResolveResult>( requests.size() );

        LocalRepositoryManager lrm = context.getLocalRepositoryManager();
        WorkspaceReader workspace = context.getWorkspaceReader();

        List<ResolutionGroup> groups = new ArrayList<ResolutionGroup>();

        for ( ResolveRequest request : requests )
        {
            ResolveResult result = new ResolveResult( request );
            results.add( result );

            Artifact artifact = request.getArtifact();
            List<? extends RemoteRepository> repos = request.getRemoteRepositories();

            if ( artifact.getFile() != null )
            {
                if ( !artifact.getFile().isFile() )
                {
                    result.addException( new ArtifactNotFoundException( artifact, null ) );
                }
                continue;
            }

            VersionRequest versionRequest = new VersionRequest();
            versionRequest.setArtifact( artifact );
            versionRequest.setRemoteRepositories( repos );
            VersionResult versionResult;
            try
            {
                versionResult = versionResolver.resolveVersion( context, versionRequest );
            }
            catch ( VersionResolutionException e )
            {
                result.addException( e );
                continue;
            }

            artifact.setVersion( versionResult.getVersion() );

            if ( versionResult.getRepository() != null )
            {
                if ( versionResult.getRepository() instanceof RemoteRepository )
                {
                    repos = Collections.singletonList( (RemoteRepository) versionResult.getRepository() );
                }
                else
                {
                    repos = Collections.emptyList();
                }
            }

            if ( workspace != null )
            {
                File file = workspace.findArtifact( artifact );
                if ( file != null )
                {
                    artifact.setFile( file );
                    result.setRepository( context.getWorkspaceRepository() );
                    continue;
                }
            }

            LocalArtifactQuery query = new LocalArtifactQuery( artifact, repos );
            lrm.find( query );
            if ( query.isAvailable() )
            {
                artifact.setFile( query.getFile() );
                result.setRepository( context.getLocalRepository() );
                continue;
            }

            Iterator<ResolutionGroup> groupIt = groups.iterator();
            for ( RemoteRepository repo : repos )
            {
                if ( !repo.getPolicy( isSnapshot( artifact ) ).isEnabled() )
                {
                    continue;
                }
                ResolutionGroup group = null;
                while ( groupIt.hasNext() )
                {
                    ResolutionGroup t = groupIt.next();
                    if ( t.matches( repo ) )
                    {
                        group = t;
                        break;
                    }
                }
                if ( group == null )
                {
                    group = new ResolutionGroup( repo );
                    groups.add( group );
                }
                group.items.add( new ResolutionItem( result, query ) );
            }
        }

        for ( ResolutionGroup group : groups )
        {
            List<ArtifactDownload> downloads = new ArrayList<ArtifactDownload>();
            for ( ResolutionItem item : group.items )
            {
                Artifact artifact = item.request.getArtifact();
                if ( artifact.getFile() != null )
                {
                    continue;
                }

                ArtifactDownload download = new ArtifactDownload();
                download.setArtifact( artifact );
                if ( item.query.getFile() != null )
                {
                    download.setFile( item.query.getFile() );
                    download.setExistenceCheck( true );
                }
                else
                {
                    File file = new File( lrm.getBasedir(), lrm.getPathForRemoteArtifact( artifact, group.repository ) );
                    download.setFile( file );
                }

                boolean snapshot = isSnapshot( artifact );
                RepositoryPolicy policy =
                    remoteRepositoryManager.getPolicy( context, group.repository, !snapshot, snapshot );

                if ( context.isNotFoundCachingEnabled() || context.isTransferErrorCachingEnabled() )
                {
                    UpdateCheck<Artifact, ArtifactTransferException> check =
                        new UpdateCheck<Artifact, ArtifactTransferException>();
                    check.setItem( artifact );
                    check.setFile( download.getFile() );
                    check.setRepository( group.repository );
                    check.setPolicy( policy.getUpdatePolicy() );
                    item.updateCheck = check;
                    updateCheckManager.checkArtifact( context, check );
                    if ( !check.isRequired() )
                    {
                        item.result.addException( check.getException() );
                        continue;
                    }
                }

                download.setChecksumPolicy( policy.getChecksumPolicy() );
                downloads.add( download );
                item.download = download;
            }
            if ( downloads.isEmpty() )
            {
                continue;
            }
            try
            {
                RepositoryReader reader = remoteRepositoryManager.getRepositoryReader( context, group.repository );
                try
                {
                    reader.getArtifacts( downloads );
                }
                finally
                {
                    reader.close();
                }
            }
            catch ( NoRepositoryReaderException e )
            {
                for ( ArtifactDownload download : downloads )
                {
                    download.setException( new ArtifactTransferException( download.getArtifact(), group.repository, e ) );
                }
            }
            for ( ResolutionItem item : group.items )
            {
                if ( item.updateCheck != null )
                {
                    updateCheckManager.touchArtifact( context, item.updateCheck );
                }
                ArtifactDownload download = item.download;
                if ( download == null )
                {
                    continue;
                }
                if ( download.getException() == null )
                {
                    download.getArtifact().setFile( download.getFile() );
                    item.result.setRepository( group.repository );
                    lrm.addRemoteArtifact( download.getArtifact(), group.repository );
                }
                else
                {
                    item.result.addException( download.getException() );
                }
            }
        }

        for ( ResolveResult result : results )
        {
            Artifact artifact = result.getRequest().getArtifact();
            if ( artifact.getFile() == null )
            {
                throw new ArtifactResolutionException( results );
            }
        }

        return results;
    }

    private boolean isSnapshot( Artifact artifact )
    {
        String version = artifact.getVersion();
        return version.endsWith( "SNAPSHOT" ) || version.matches( "^(.*)-([0-9]{8}.[0-9]{6})-([0-9]+)$" );
    }

    static class ResolutionGroup
    {

        final RemoteRepository repository;

        final List<ResolutionItem> items = new ArrayList<ResolutionItem>();

        ResolutionGroup( RemoteRepository repository )
        {
            this.repository = repository;
        }

        boolean matches( RemoteRepository repo )
        {
            return repository.getUrl().equals( repo.getUrl() ) && repository.getId().equals( repo.getId() );
        }

    }

    static class ResolutionItem
    {

        final ResolveRequest request;

        final ResolveResult result;

        final LocalArtifactQuery query;

        ArtifactDownload download;

        UpdateCheck<Artifact, ArtifactTransferException> updateCheck;

        ResolutionItem( ResolveResult result, LocalArtifactQuery query )
        {
            this.result = result;
            this.request = result.getRequest();
            this.query = query;
        }

    }

}
