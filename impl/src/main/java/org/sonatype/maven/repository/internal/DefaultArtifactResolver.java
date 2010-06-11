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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.FileUtils;
import org.sonatype.maven.repository.Artifact;
import org.sonatype.maven.repository.ArtifactNotFoundException;
import org.sonatype.maven.repository.ArtifactRepository;
import org.sonatype.maven.repository.ArtifactRequest;
import org.sonatype.maven.repository.ArtifactResolutionException;
import org.sonatype.maven.repository.ArtifactResult;
import org.sonatype.maven.repository.ArtifactTransferException;
import org.sonatype.maven.repository.DerivedArtifact;
import org.sonatype.maven.repository.LocalArtifactQuery;
import org.sonatype.maven.repository.LocalRepositoryManager;
import org.sonatype.maven.repository.NoRepositoryConnectorException;
import org.sonatype.maven.repository.RemoteRepository;
import org.sonatype.maven.repository.RepositoryListener;
import org.sonatype.maven.repository.RepositoryPolicy;
import org.sonatype.maven.repository.RepositorySession;
import org.sonatype.maven.repository.VersionRequest;
import org.sonatype.maven.repository.VersionResolutionException;
import org.sonatype.maven.repository.VersionResult;
import org.sonatype.maven.repository.WorkspaceReader;
import org.sonatype.maven.repository.spi.ArtifactDownload;
import org.sonatype.maven.repository.spi.ArtifactResolver;
import org.sonatype.maven.repository.spi.LocalRepositoryMaintainer;
import org.sonatype.maven.repository.spi.Logger;
import org.sonatype.maven.repository.spi.NullLogger;
import org.sonatype.maven.repository.spi.RemoteRepositoryManager;
import org.sonatype.maven.repository.spi.RepositoryConnector;
import org.sonatype.maven.repository.spi.UpdateCheck;
import org.sonatype.maven.repository.spi.UpdateCheckManager;
import org.sonatype.maven.repository.spi.VersionResolver;
import org.sonatype.maven.repository.util.DefaultRepositoryEvent;

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

    @Requirement( optional = true )
    private LocalRepositoryMaintainer maintainer;

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

    public DefaultArtifactResolver setLocalRepositoryMaintainer( LocalRepositoryMaintainer maintainer )
    {
        this.maintainer = maintainer;
        return this;
    }

    public ArtifactResult resolveArtifact( RepositorySession session, ArtifactRequest request )
        throws ArtifactResolutionException
    {
        return resolveArtifacts( session, Collections.singleton( request ) ).get( 0 );
    }

    public List<ArtifactResult> resolveArtifacts( RepositorySession session,
                                                  Collection<? extends ArtifactRequest> requests )
        throws ArtifactResolutionException
    {
        List<ArtifactResult> results = new ArrayList<ArtifactResult>( requests.size() );
        boolean failures = false;

        LocalRepositoryManager lrm = session.getLocalRepositoryManager();
        WorkspaceReader workspace = session.getWorkspaceReader();

        List<ResolutionGroup> groups = new ArrayList<ResolutionGroup>();

        for ( ArtifactRequest request : requests )
        {
            ArtifactResult result = new ArtifactResult( request );
            results.add( result );

            Artifact artifact = request.getArtifact();
            List<RemoteRepository> repos = request.getRepositories();

            artifactResolving( session, artifact );

            if ( artifact.getFile() != null )
            {
                if ( !artifact.getFile().isFile() )
                {
                    failures = true;
                    result.addException( new ArtifactNotFoundException( artifact, null ) );
                }
                artifactResolved( session, artifact, null, result.getExceptions() );
                continue;
            }

            VersionResult versionResult;
            try
            {
                VersionRequest versionRequest = new VersionRequest( artifact, repos, request.getContext() );
                versionResult = versionResolver.resolveVersion( session, versionRequest );
            }
            catch ( VersionResolutionException e )
            {
                result.addException( e );
                artifactResolved( session, artifact, null, result.getExceptions() );
                continue;
            }

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
                    result.setRepository( workspace.getRepository() );
                    artifactResolved( session, artifact, workspace.getRepository(), null );
                    continue;
                }
            }

            LocalArtifactQuery query = new LocalArtifactQuery( artifact, repos, request.getContext() );
            lrm.find( query );
            if ( query.isAvailable() )
            {
                result.setRepository( lrm.getRepository() );
                try
                {
                    artifact.setFile( getFile( artifact, query.getFile() ) );
                    artifactResolved( session, artifact, lrm.getRepository(), null );
                }
                catch ( ArtifactTransferException e )
                {
                    result.addException( e );
                }
                continue;
            }

            if ( session.isOffline() )
            {
                Exception exception =
                    new ArtifactNotFoundException( artifact, null, "The repository system is offline but the artifact "
                        + artifact + " is not available in the local repository." );
                result.addException( exception );
                artifactResolved( session, artifact, null, result.getExceptions() );
                continue;
            }

            Iterator<ResolutionGroup> groupIt = groups.iterator();
            for ( RemoteRepository repo : repos )
            {
                if ( !repo.getPolicy( artifact.isSnapshot() ).isEnabled() )
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
                    groupIt = Collections.<ResolutionGroup> emptyList().iterator();
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
                    // resolved in previous resolution group
                    continue;
                }

                ArtifactDownload download = new ArtifactDownload();
                download.setArtifact( artifact );
                download.setContext( item.request.getContext() );
                if ( item.query.getFile() != null )
                {
                    download.setFile( item.query.getFile() );
                    download.setExistenceCheck( true );
                }
                else
                {
                    String path = lrm.getPathForRemoteArtifact( artifact, group.repository, item.request.getContext() );
                    download.setFile( new File( lrm.getRepository().getBasedir(), path ) );
                }

                boolean snapshot = artifact.isSnapshot();
                RepositoryPolicy policy =
                    remoteRepositoryManager.getPolicy( session, group.repository, !snapshot, snapshot );

                if ( session.isNotFoundCachingEnabled() || session.isTransferErrorCachingEnabled() )
                {
                    UpdateCheck<Artifact, ArtifactTransferException> check =
                        new UpdateCheck<Artifact, ArtifactTransferException>();
                    check.setItem( artifact );
                    check.setFile( download.getFile() );
                    check.setRepository( group.repository );
                    check.setPolicy( policy.getUpdatePolicy() );
                    item.updateCheck = check;
                    updateCheckManager.checkArtifact( session, check );
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
                RepositoryConnector connector =
                    remoteRepositoryManager.getRepositoryConnector( session, group.repository );
                try
                {
                    connector.get( downloads, null );
                }
                finally
                {
                    connector.close();
                }
            }
            catch ( NoRepositoryConnectorException e )
            {
                for ( ArtifactDownload download : downloads )
                {
                    download.setException( new ArtifactTransferException( download.getArtifact(), group.repository, e ) );
                }
            }
            for ( ResolutionItem item : group.items )
            {
                ArtifactDownload download = item.download;
                if ( download == null )
                {
                    continue;
                }

                if ( item.updateCheck != null )
                {
                    item.updateCheck.setException( download.getException() );
                    updateCheckManager.touchArtifact( session, item.updateCheck );
                }
                if ( download.getException() == null )
                {
                    item.result.setRepository( group.repository );
                    Artifact artifact = download.getArtifact();
                    try
                    {
                        artifact.setFile( getFile( artifact, download.getFile() ) );
                    }
                    catch ( ArtifactTransferException e )
                    {
                        item.result.addException( e );
                        continue;
                    }
                    lrm.addRemoteArtifact( artifact, group.repository, item.request.getContext() );
                    if ( maintainer != null )
                    {
                        maintainer.artifactDownloaded( new DefaultLocalRepositoryEvent( session, artifact ) );
                    }
                    artifactResolved( session, artifact, group.repository, null );
                }
                else
                {
                    item.result.addException( download.getException() );
                }
            }
        }

        for ( ArtifactResult result : results )
        {
            Artifact artifact = result.getRequest().getArtifact();
            if ( artifact.getFile() == null )
            {
                failures = true;
                if ( result.getExceptions().isEmpty() )
                {
                    Exception exception = new ArtifactNotFoundException( artifact, null );
                    result.addException( exception );
                }
                artifactResolved( session, artifact, null, result.getExceptions() );
            }
        }

        if ( failures )
        {
            throw new ArtifactResolutionException( results );
        }

        return results;
    }

    private File getFile( Artifact artifact, File file )
        throws ArtifactTransferException
    {
        if ( artifact.isSnapshot() && !artifact.getVersion().equals( artifact.getBaseVersion() ) )
        {
            String name = file.getName().replace( artifact.getVersion(), artifact.getBaseVersion() );
            File dst = new File( file.getParent(), name );

            boolean copy = dst.length() != file.length() || dst.lastModified() != file.lastModified();
            if ( copy )
            {
                try
                {
                    FileUtils.copyFile( file, dst );
                    dst.setLastModified( file.lastModified() );
                }
                catch ( IOException e )
                {
                    throw new ArtifactTransferException( artifact, null, e );
                }
            }

            file = dst;
        }

        return file;
    }

    private void artifactResolving( RepositorySession session, Artifact artifact )
    {
        RepositoryListener listener = session.getRepositoryListener();
        if ( listener != null )
        {
            DefaultRepositoryEvent event = new DefaultRepositoryEvent( session, artifact );
            listener.artifactResolving( event );
        }
    }

    private void artifactResolved( RepositorySession session, Artifact artifact, ArtifactRepository repository,
                                   List<Exception> exceptions )
    {
        RepositoryListener listener = session.getRepositoryListener();
        if ( listener != null )
        {
            DefaultRepositoryEvent event = new DefaultRepositoryEvent( session, artifact );
            event.setRepository( repository );
            event.setExceptions( exceptions );
            event.setFile( artifact.getFile() );
            listener.artifactResolved( event );
        }
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

        final ArtifactRequest request;

        final ArtifactResult result;

        final LocalArtifactQuery query;

        ArtifactDownload download;

        UpdateCheck<Artifact, ArtifactTransferException> updateCheck;

        ResolutionItem( ArtifactResult result, LocalArtifactQuery query )
        {
            this.result = result;
            this.request = result.getRequest();
            this.query = query;
        }

    }

    static class SnapshotArtifact
        extends DerivedArtifact
    {

        public SnapshotArtifact( Artifact artifact )
        {
            super( artifact );
        }

        @Override
        public String getVersion()
        {
            return getBaseVersion();
        }

    }

}
