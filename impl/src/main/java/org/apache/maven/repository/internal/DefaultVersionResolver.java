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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.maven.artifact.repository.metadata.Snapshot;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Reader;
import org.apache.maven.repository.Artifact;
import org.apache.maven.repository.ArtifactRepository;
import org.apache.maven.repository.DefaultArtifact;
import org.apache.maven.repository.LocalRepositoryManager;
import org.apache.maven.repository.Metadata;
import org.apache.maven.repository.MetadataRequest;
import org.apache.maven.repository.MetadataResult;
import org.apache.maven.repository.RemoteRepository;
import org.apache.maven.repository.RepositoryContext;
import org.apache.maven.repository.VersionRequest;
import org.apache.maven.repository.VersionResolutionException;
import org.apache.maven.repository.VersionResult;
import org.apache.maven.repository.WorkspaceReader;
import org.apache.maven.repository.spi.Logger;
import org.apache.maven.repository.spi.MetadataResolver;
import org.apache.maven.repository.spi.NullLogger;
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
            List<MetadataRequest> metadataRequests =
                new ArrayList<MetadataRequest>( request.getRemoteRepositories().size() );
            for ( RemoteRepository repository : request.getRemoteRepositories() )
            {
                MetadataRequest metadataRequest = new MetadataRequest( new Metadata( metadata ), repository );
                metadataRequest.setDeleteLocalCopyIfMissing( true );
                metadataRequests.add( metadataRequest );
            }
            List<MetadataResult> metadataResults = metadataResolver.resolveMetadata( context, metadataRequests );

            LocalRepositoryManager lrm = context.getLocalRepositoryManager();
            File localMetadataFile =
                new File( lrm.getRepository().getBasedir(), lrm.getPathForLocalMetadata( metadata ) );
            if ( localMetadataFile.isFile() )
            {
                metadata.setFile( localMetadataFile );
            }

            Versioning versioning = readVersions( context, metadata, result );
            ArtifactRepository repo = context.getLocalRepositoryManager().getRepository();

            for ( MetadataResult metadataResult : metadataResults )
            {
                result.addException( metadataResult.getException() );
                Versioning v = readVersions( context, metadataResult.getRequest().getMetadata(), result );
                if ( mergeVersions( versioning, v ) )
                {
                    repo = metadataResult.getRequest().getRemoteRepository();
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

                if ( result.getVersion() != null && result.getVersion().endsWith( "SNAPSHOT" ) )
                {
                    Artifact artifact = new DefaultArtifact( request.getArtifact() );
                    artifact.setVersion( result.getVersion() );
                    VersionRequest subRequest = new VersionRequest();
                    subRequest.setArtifact( artifact );
                    if ( repo instanceof RemoteRepository )
                    {
                        subRequest.setRemoteRepositories( Collections.singletonList( (RemoteRepository) repo ) );
                    }
                    else
                    {
                        subRequest.setRemoteRepositories( request.getRemoteRepositories() );
                    }
                    VersionResult subResult = resolveVersion( context, subRequest );
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

    private Versioning readVersions( RepositoryContext context, Metadata metadata, VersionResult result )
    {
        Versioning versioning = null;

        FileInputStream fis = null;
        try
        {
            if ( metadata.getFile() != null )
            {
                fis = new FileInputStream( metadata.getFile() );
                org.apache.maven.artifact.repository.metadata.Metadata m = new MetadataXpp3Reader().read( fis );
                versioning = m.getVersioning();
            }
        }
        catch ( FileNotFoundException e )
        {
            // tolerable
        }
        catch ( Exception e )
        {
            result.addException( e );
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
