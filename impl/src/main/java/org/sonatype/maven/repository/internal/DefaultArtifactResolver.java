package org.sonatype.maven.repository.internal;

/*
 * Copyright (c) 2010 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0, 
 * and you may not use this file except in compliance with the Apache License Version 2.0. 
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, 
 * software distributed under the Apache License Version 2.0 is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

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
import org.sonatype.maven.repository.LocalArtifactRequest;
import org.sonatype.maven.repository.LocalArtifactResult;
import org.sonatype.maven.repository.LocalRepositoryManager;
import org.sonatype.maven.repository.NoRepositoryConnectorException;
import org.sonatype.maven.repository.RemoteRepository;
import org.sonatype.maven.repository.RepositoryListener;
import org.sonatype.maven.repository.RepositoryPolicy;
import org.sonatype.maven.repository.RepositorySystemSession;
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

    public ArtifactResult resolveArtifact( RepositorySystemSession session, ArtifactRequest request )
        throws ArtifactResolutionException
    {
        return resolveArtifacts( session, Collections.singleton( request ) ).get( 0 );
    }

    public List<ArtifactResult> resolveArtifacts( RepositorySystemSession session,
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
                // pre-resolved (e.g. system scope), just validate file
                if ( !artifact.getFile().isFile() )
                {
                    failures = true;
                    result.addException( new ArtifactNotFoundException( artifact, null ) );
                }
                else
                {
                    result.setArtifact( artifact );
                }
                artifactResolved( session, artifact, null, result.getExceptions() );
                continue;
            }

            VersionResult versionResult;
            try
            {
                VersionRequest versionRequest = new VersionRequest( artifact, repos, request.getRequestContext() );
                versionResult = versionResolver.resolveVersion( session, versionRequest );
            }
            catch ( VersionResolutionException e )
            {
                result.addException( e );
                artifactResolved( session, artifact, null, result.getExceptions() );
                continue;
            }

            artifact = artifact.setVersion( versionResult.getVersion() );

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
                    artifact = artifact.setFile( file );
                    result.setArtifact( artifact );
                    result.setRepository( workspace.getRepository() );
                    artifactResolved( session, artifact, workspace.getRepository(), null );
                    continue;
                }
            }

            LocalArtifactResult local =
                lrm.find( new LocalArtifactRequest( artifact, repos, request.getRequestContext() ) );
            if ( local.isAvailable() )
            {
                result.setRepository( lrm.getRepository() );
                try
                {
                    artifact = artifact.setFile( getFile( artifact, local.getFile() ) );
                    result.setArtifact( artifact );
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

            AtomicBoolean resolved = new AtomicBoolean( false );
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
                group.items.add( new ResolutionItem( artifact, resolved, result, local, repo ) );
            }
        }

        for ( ResolutionGroup group : groups )
        {
            List<ArtifactDownload> downloads = new ArrayList<ArtifactDownload>();
            for ( ResolutionItem item : group.items )
            {
                Artifact artifact = item.artifact;

                if ( item.resolved.get() )
                {
                    // resolved in previous resolution group
                    continue;
                }

                ArtifactDownload download = new ArtifactDownload();
                download.setArtifact( artifact );
                download.setRequestContext( item.request.getRequestContext() );
                if ( item.local.getFile() != null )
                {
                    download.setFile( item.local.getFile() );
                    download.setExistenceCheck( true );
                }
                else
                {
                    String path =
                        lrm.getPathForRemoteArtifact( artifact, group.repository, item.request.getRequestContext() );
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
                    if ( !check.isRequired() && check.getException() != null )
                    {
                        item.result.addException( check.getException() );
                        continue;
                    }
                }

                download.setChecksumPolicy( policy.getChecksumPolicy() );
                download.setRepositories( item.repository.getMirroredRepositories() );
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
                    item.resolved.set( true );
                    item.result.setRepository( group.repository );
                    Artifact artifact = download.getArtifact();
                    try
                    {
                        artifact = artifact.setFile( getFile( artifact, download.getFile() ) );
                        item.result.setArtifact( artifact );
                    }
                    catch ( ArtifactTransferException e )
                    {
                        item.result.addException( e );
                        continue;
                    }
                    lrm.addRemoteArtifact( artifact, group.repository, item.request.getRequestContext() );
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
            Artifact artifact = result.getArtifact();
            if ( artifact == null || artifact.getFile() == null )
            {
                failures = true;
                if ( result.getExceptions().isEmpty() )
                {
                    Exception exception = new ArtifactNotFoundException( result.getRequest().getArtifact(), null );
                    result.addException( exception );
                }
                artifactResolved( session, result.getRequest().getArtifact(), null, result.getExceptions() );
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

    private void artifactResolving( RepositorySystemSession session, Artifact artifact )
    {
        RepositoryListener listener = session.getRepositoryListener();
        if ( listener != null )
        {
            DefaultRepositoryEvent event = new DefaultRepositoryEvent( session, artifact );
            listener.artifactResolving( event );
        }
    }

    private void artifactResolved( RepositorySystemSession session, Artifact artifact, ArtifactRepository repository,
                                   List<Exception> exceptions )
    {
        RepositoryListener listener = session.getRepositoryListener();
        if ( listener != null )
        {
            DefaultRepositoryEvent event = new DefaultRepositoryEvent( session, artifact );
            event.setRepository( repository );
            event.setExceptions( exceptions );
            if ( artifact != null )
            {
                event.setFile( artifact.getFile() );
            }
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
            return repository.getUrl().equals( repo.getUrl() )
                && repository.getContentType().equals( repo.getContentType() )
                && repository.isRepositoryManager() == repo.isRepositoryManager();
        }

    }

    static class ResolutionItem
    {

        final ArtifactRequest request;

        final ArtifactResult result;

        final LocalArtifactResult local;

        final RemoteRepository repository;

        final Artifact artifact;

        final AtomicBoolean resolved;

        ArtifactDownload download;

        UpdateCheck<Artifact, ArtifactTransferException> updateCheck;

        ResolutionItem( Artifact artifact, AtomicBoolean resolved, ArtifactResult result, LocalArtifactResult local,
                        RemoteRepository repository )
        {
            this.artifact = artifact;
            this.resolved = resolved;
            this.result = result;
            this.request = result.getRequest();
            this.local = local;
            this.repository = repository;
        }

    }

}
