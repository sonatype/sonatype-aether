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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;

import org.apache.maven.artifact.repository.metadata.Snapshot;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Reader;
import org.apache.maven.repository.ArtifactRepository;
import org.apache.maven.repository.LocalRepositoryManager;
import org.apache.maven.repository.Metadata;
import org.apache.maven.repository.MetadataDownload;
import org.apache.maven.repository.MetadataNotFoundException;
import org.apache.maven.repository.MetadataTransferException;
import org.apache.maven.repository.NoRepositoryReaderException;
import org.apache.maven.repository.RemoteRepository;
import org.apache.maven.repository.RepositoryContext;
import org.apache.maven.repository.RepositoryPolicy;
import org.apache.maven.repository.VersionRequest;
import org.apache.maven.repository.VersionResolutionException;
import org.apache.maven.repository.VersionResult;
import org.apache.maven.repository.WorkspaceReader;
import org.apache.maven.repository.spi.Logger;
import org.apache.maven.repository.spi.NullLogger;
import org.apache.maven.repository.spi.RemoteRepositoryManager;
import org.apache.maven.repository.spi.RepositoryReader;
import org.apache.maven.repository.spi.UpdateCheck;
import org.apache.maven.repository.spi.UpdateCheckManager;
import org.apache.maven.repository.spi.VersionResolver;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;

/**
 * @author Benjamin Bentmann
 */
@Component( role = VersionResolver.class )
public class DefaultVersionResolver
    implements VersionResolver
{

    @Requirement
    private Logger logger = NullLogger.INSTANCE;

    @Requirement
    private UpdateCheckManager updateCheckManager;

    @Requirement
    private RemoteRepositoryManager remoteRepositoryManager;

    public DefaultVersionResolver setLogger( Logger logger )
    {
        this.logger = ( logger != null ) ? logger : NullLogger.INSTANCE;
        return this;
    }

    public DefaultVersionResolver setUpdateCheckManager( UpdateCheckManager updateCheckManager )
    {
        if ( updateCheckManager == null )
        {
            throw new IllegalArgumentException( "update check manager has not been specified" );
        }
        this.updateCheckManager = updateCheckManager;
        return this;
    }

    public DefaultVersionResolver setRemoteRepositoryManager( RemoteRepositoryManager remoteRepositoryManager )
    {
        if ( remoteRepositoryManager == null )
        {
            throw new IllegalArgumentException( "remote repository manager has not been specified" );
        }
        this.remoteRepositoryManager = remoteRepositoryManager;
        return this;
    }

    public VersionResult resolveVersion( RepositoryContext context, VersionRequest request )
        throws VersionResolutionException
    {
        String version = request.getArtifact().getVersion();

        VersionResult result = new VersionResult( request );

        Metadata metadata;

        if ( "RELEASE".equals( version ) || "LATEST".equals( version ) )
        {
            metadata = new Metadata();
            metadata.setGroupId( request.getArtifact().getGroupId() );
            metadata.setArtifactId( request.getArtifact().getArtifactId() );
            metadata.setType( "maven-metadata.xml" );
            metadata.setNature( "LATEST".equals( version ) ? Metadata.Nature.RELEASE_OR_SNAPSHOT
                            : Metadata.Nature.RELEASE );
        }
        else if ( version.endsWith( "SNAPSHOT" ) )
        {
            WorkspaceReader workspace = context.getWorkspaceReader();
            if ( workspace != null && workspace.findVersions( request.getArtifact() ).contains( version ) )
            {
                metadata = null;
                result.setRepository( workspace.getRepository() );
            }
            else
            {
                metadata = new Metadata();
                metadata.setGroupId( request.getArtifact().getGroupId() );
                metadata.setArtifactId( request.getArtifact().getArtifactId() );
                metadata.setVersion( version );
                metadata.setType( "maven-metadata.xml" );
                metadata.setNature( Metadata.Nature.SNAPSHOT );
            }
        }
        else
        {
            metadata = null;
        }

        if ( metadata == null )
        {
            result.setVersion( version );
        }
        else
        {
            if ( !context.isOffline() )
            {
                long localLastUpdated = getFile( context, metadata, null ).lastModified();

                for ( RemoteRepository repository : request.getRemoteRepositories() )
                {
                    RepositoryPolicy policy = getPolicy( context, repository, metadata.getNature() );

                    if ( !policy.isEnabled() )
                    {
                        continue;
                    }

                    File metadataFile = getFile( context, metadata, repository );

                    MetadataDownload download =
                        new MetadataDownload( metadata, metadataFile, policy.getChecksumPolicy() );

                    UpdateCheck<Metadata, MetadataTransferException> check =
                        new UpdateCheck<Metadata, MetadataTransferException>();
                    check.setLocalLastUpdated( localLastUpdated );
                    check.setItem( metadata );
                    check.setFile( metadataFile );
                    check.setRepository( repository );
                    check.setPolicy( policy.getUpdatePolicy() );
                    updateCheckManager.checkMetadata( context, check );

                    if ( check.isRequired() )
                    {
                        try
                        {
                            RepositoryReader reader = remoteRepositoryManager.getRepositoryReader( context, repository );
                            try
                            {
                                reader.getMetadata( Arrays.asList( download ) );
                            }
                            finally
                            {
                                reader.close();
                            }
                        }
                        catch ( NoRepositoryReaderException e )
                        {
                            download.setException( new MetadataTransferException( metadata, repository, e ) );
                        }
                        updateCheckManager.touchMetadata( context, check.setException( download.getException() ) );
                    }
                    else
                    {
                        download.setException( check.getException() );
                    }

                    if ( download.getException() != null )
                    {
                        result.addException( download.getException() );
                        if ( download.getException() instanceof MetadataNotFoundException )
                        {
                            metadataFile.delete();
                        }
                    }
                }
            }

            Versioning versioning = readVersions( context, metadata, null );
            ArtifactRepository repo = context.getLocalRepositoryManager().getRepository();

            for ( RemoteRepository repository : request.getRemoteRepositories() )
            {
                RepositoryPolicy policy = getPolicy( context, repository, metadata.getNature() );

                if ( !policy.isEnabled() )
                {
                    continue;
                }

                Versioning v = readVersions( context, metadata, repository );
                if ( mergeVersions( versioning, v ) )
                {
                    repo = repository;
                }
            }

            if ( "RELEASE".equals( version ) )
            {
                result.setVersion( versioning.getRelease() );
            }
            else if ( "LATEST".equals( version ) )
            {
                result.setVersion( versioning.getLatest() );
                if ( StringUtils.isEmpty( versioning.getLatest() ) )
                {
                    result.setVersion( versioning.getRelease() );
                }
            }
            else
            {
                Snapshot snapshot = versioning.getSnapshot();
                if ( snapshot != null )
                {
                    if ( snapshot.getTimestamp() != null && snapshot.getBuildNumber() > 0 )
                    {
                        String qualifier = snapshot.getTimestamp() + "-" + snapshot.getBuildNumber();
                        result.setVersion( version.substring( 0, version.length() - "SNAPSHOT".length() ) + qualifier );
                    }
                    else
                    {
                        result.setVersion( version );
                    }
                }
            }

            if ( StringUtils.isNotEmpty( result.getVersion() ) )
            {
                result.setRepository( repo );
            }
            else
            {
                throw new VersionResolutionException( result );
            }
        }

        return result;
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

    private RepositoryPolicy getPolicy( RepositoryContext context, RemoteRepository repository, Metadata.Nature nature )
    {
        boolean releases = !Metadata.Nature.SNAPSHOT.equals( nature );
        boolean snapshots = !Metadata.Nature.RELEASE.equals( nature );
        return remoteRepositoryManager.getPolicy( context, repository, releases, snapshots );
    }

    private Versioning readVersions( RepositoryContext context, Metadata metadata, RemoteRepository repository )
    {
        Versioning versioning = null;

        File metadataFile = getFile( context, metadata, repository );

        FileInputStream fis = null;
        try
        {
            fis = new FileInputStream( metadataFile );
            org.apache.maven.artifact.repository.metadata.Metadata m = new MetadataXpp3Reader().read( fis );
            versioning = m.getVersioning();
        }
        catch ( FileNotFoundException e )
        {
            // tolerable
        }
        catch ( Exception e )
        {
            // TODO: notify somebody
        }
        finally
        {
            IOUtil.close( fis );
        }

        return ( versioning != null ) ? versioning : new Versioning();
    }

    private boolean mergeVersions( Versioning target, Versioning source )
    {
        String targetTimestamp = StringUtils.clean( target.getLastUpdated() );
        String sourceTimestamp = StringUtils.clean( source.getLastUpdated() );
        if ( targetTimestamp.compareTo( sourceTimestamp ) >= 0 )
        {
            return false;
        }

        if ( source.getRelease() != null )
        {
            target.setRelease( source.getRelease() );
        }
        if ( source.getLatest() != null )
        {
            target.setLatest( source.getLatest() );
        }
        if ( source.getSnapshot() != null )
        {
            target.setSnapshot( source.getSnapshot() );
        }

        return true;
    }

}
