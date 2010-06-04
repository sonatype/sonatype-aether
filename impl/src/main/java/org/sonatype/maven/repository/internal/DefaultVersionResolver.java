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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.maven.repository.ArtifactRepository;
import org.sonatype.maven.repository.DefaultMetadata;
import org.sonatype.maven.repository.LocalRepositoryManager;
import org.sonatype.maven.repository.Metadata;
import org.sonatype.maven.repository.MetadataRequest;
import org.sonatype.maven.repository.MetadataResult;
import org.sonatype.maven.repository.RemoteRepository;
import org.sonatype.maven.repository.RepositoryListener;
import org.sonatype.maven.repository.RepositorySession;
import org.sonatype.maven.repository.VersionRequest;
import org.sonatype.maven.repository.VersionResolutionException;
import org.sonatype.maven.repository.VersionResult;
import org.sonatype.maven.repository.WorkspaceReader;
import org.sonatype.maven.repository.internal.metadata.Snapshot;
import org.sonatype.maven.repository.internal.metadata.Versioning;
import org.sonatype.maven.repository.internal.metadata.io.xpp3.MetadataXpp3Reader;
import org.sonatype.maven.repository.spi.Logger;
import org.sonatype.maven.repository.spi.MetadataResolver;
import org.sonatype.maven.repository.spi.NullLogger;
import org.sonatype.maven.repository.spi.VersionResolver;
import org.sonatype.maven.repository.util.DefaultRepositoryEvent;

/**
 * @author Benjamin Bentmann
 */
@Component( role = VersionResolver.class )
public class DefaultVersionResolver
    implements VersionResolver
{

    private static final String MAVEN_METADATA_XML = "maven-metadata.xml";

    private static final String RELEASE = "RELEASE";

    private static final String LATEST = "LATEST";

    private static final String SNAPSHOT = "SNAPSHOT";

    @Requirement
    private Logger logger = NullLogger.INSTANCE;

    @Requirement
    private MetadataResolver metadataResolver;

    public DefaultVersionResolver setLogger( Logger logger )
    {
        this.logger = ( logger != null ) ? logger : NullLogger.INSTANCE;
        return this;
    }

    public DefaultVersionResolver setMetadataResolver( MetadataResolver metadataResolver )
    {
        if ( metadataResolver == null )
        {
            throw new IllegalArgumentException( "metadata resolver has not been specified" );
        }
        this.metadataResolver = metadataResolver;
        return this;
    }

    public VersionResult resolveVersion( RepositorySession session, VersionRequest request )
        throws VersionResolutionException
    {
        String version = request.getArtifact().getVersion();

        VersionResult result = new VersionResult( request );

        DefaultMetadata metadata;

        if ( RELEASE.equals( version ) )
        {
            metadata = new DefaultMetadata();
            metadata.setGroupId( request.getArtifact().getGroupId() );
            metadata.setArtifactId( request.getArtifact().getArtifactId() );
            metadata.setNature( Metadata.Nature.RELEASE );
        }
        else if ( LATEST.equals( version ) )
        {
            metadata = new DefaultMetadata();
            metadata.setGroupId( request.getArtifact().getGroupId() );
            metadata.setArtifactId( request.getArtifact().getArtifactId() );
            metadata.setNature( Metadata.Nature.RELEASE_OR_SNAPSHOT );
        }
        else if ( version.endsWith( SNAPSHOT ) )
        {
            WorkspaceReader workspace = session.getWorkspaceReader();
            if ( workspace != null && workspace.findVersions( request.getArtifact() ).contains( version ) )
            {
                metadata = null;
                result.setRepository( workspace.getRepository() );
            }
            else
            {
                metadata = new DefaultMetadata();
                metadata.setGroupId( request.getArtifact().getGroupId() );
                metadata.setArtifactId( request.getArtifact().getArtifactId() );
                metadata.setVersion( version );
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
            metadata.setType( MAVEN_METADATA_XML );

            List<MetadataRequest> metadataRequests = new ArrayList<MetadataRequest>( request.getRepositories().size() );
            for ( RemoteRepository repository : request.getRepositories() )
            {
                MetadataRequest metadataRequest =
                    new MetadataRequest( new DefaultMetadata( metadata ), repository, request.getContext() );
                metadataRequest.setDeleteLocalCopyIfMissing( true );
                metadataRequest.setFavorLocalRepository( true );
                metadataRequests.add( metadataRequest );
            }
            List<MetadataResult> metadataResults = metadataResolver.resolveMetadata( session, metadataRequests );

            LocalRepositoryManager lrm = session.getLocalRepositoryManager();
            File localMetadataFile =
                new File( lrm.getRepository().getBasedir(), lrm.getPathForLocalMetadata( metadata ) );
            if ( localMetadataFile.isFile() )
            {
                metadata.setFile( localMetadataFile );
            }

            Versioning versioning = readVersions( session, metadata, result );
            ArtifactRepository repo = session.getLocalRepositoryManager().getRepository();

            for ( MetadataResult metadataResult : metadataResults )
            {
                result.addException( metadataResult.getException() );
                Versioning v = readVersions( session, metadataResult.getRequest().getMetadata(), result );
                if ( mergeVersions( versioning, v ) )
                {
                    repo = metadataResult.getRequest().getRepository();
                }
            }

            if ( RELEASE.equals( version ) )
            {
                result.setVersion( versioning.getRelease() );
            }
            else if ( LATEST.equals( version ) )
            {
                result.setVersion( versioning.getLatest() );
                if ( StringUtils.isEmpty( versioning.getLatest() ) )
                {
                    result.setVersion( versioning.getRelease() );
                }

                if ( result.getVersion() != null && result.getVersion().endsWith( SNAPSHOT ) )
                {
                    request.getArtifact().setVersion( result.getVersion() );
                    VersionRequest subRequest = new VersionRequest();
                    subRequest.setArtifact( request.getArtifact() );
                    if ( repo instanceof RemoteRepository )
                    {
                        subRequest.setRepositories( Collections.singletonList( (RemoteRepository) repo ) );
                    }
                    else
                    {
                        subRequest.setRepositories( request.getRepositories() );
                    }
                    VersionResult subResult = resolveVersion( session, subRequest );
                    result.setVersion( subResult.getVersion() );
                    repo = subResult.getRepository();
                    for ( Exception exception : subResult.getExceptions() )
                    {
                        result.addException( exception );
                    }
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
                        result.setVersion( version.substring( 0, version.length() - SNAPSHOT.length() ) + qualifier );
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

            request.getArtifact().setVersion( result.getVersion() );
        }

        return result;
    }

    private Versioning readVersions( RepositorySession session, Metadata metadata, VersionResult result )
    {
        Versioning versioning = null;

        FileInputStream fis = null;
        try
        {
            if ( metadata.getFile() != null )
            {
                fis = new FileInputStream( metadata.getFile() );
                org.sonatype.maven.repository.internal.metadata.Metadata m = new MetadataXpp3Reader().read( fis, false );
                versioning = m.getVersioning();
            }
        }
        catch ( FileNotFoundException e )
        {
            // tolerable
        }
        catch ( Exception e )
        {
            invalidMetadata( session, metadata, e );
            result.addException( e );
        }
        finally
        {
            IOUtil.close( fis );
        }

        return ( versioning != null ) ? versioning : new Versioning();
    }

    private void invalidMetadata( RepositorySession session, Metadata metadata, Exception exception )
    {
        RepositoryListener listener = session.getRepositoryListener();
        if ( listener != null )
        {
            DefaultRepositoryEvent event = new DefaultRepositoryEvent( session, metadata );
            event.setException( exception );
            listener.metadataInvalid( event );
        }
    }

    private boolean mergeVersions( Versioning target, Versioning source )
    {
        String targetTimestamp = StringUtils.clean( target.getLastUpdated() );
        String sourceTimestamp = StringUtils.clean( source.getLastUpdated() );
        if ( targetTimestamp.compareTo( sourceTimestamp ) >= 0 )
        {
            return false;
        }

        target.setLastUpdated( sourceTimestamp );

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
