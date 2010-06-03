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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.artifact.repository.metadata.Versioning;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Reader;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.maven.repository.ArtifactRepository;
import org.sonatype.maven.repository.DefaultMetadata;
import org.sonatype.maven.repository.InvalidVersionException;
import org.sonatype.maven.repository.LocalRepositoryManager;
import org.sonatype.maven.repository.Metadata;
import org.sonatype.maven.repository.MetadataRequest;
import org.sonatype.maven.repository.MetadataResult;
import org.sonatype.maven.repository.RemoteRepository;
import org.sonatype.maven.repository.RepositoryListener;
import org.sonatype.maven.repository.RepositorySession;
import org.sonatype.maven.repository.VersionRangeRequest;
import org.sonatype.maven.repository.VersionRangeResolutionException;
import org.sonatype.maven.repository.VersionRangeResult;
import org.sonatype.maven.repository.WorkspaceReader;
import org.sonatype.maven.repository.spi.Logger;
import org.sonatype.maven.repository.spi.MetadataResolver;
import org.sonatype.maven.repository.spi.NullLogger;
import org.sonatype.maven.repository.spi.VersionRangeResolver;
import org.sonatype.maven.repository.util.DefaultRepositoryEvent;

/**
 * @author Benjamin Bentmann
 */
@Component( role = VersionRangeResolver.class )
public class DefaultVersionRangeResolver
    implements VersionRangeResolver
{

    private static final String MAVEN_METADATA_XML = "maven-metadata.xml";

    @Requirement
    private Logger logger = NullLogger.INSTANCE;

    @Requirement
    private MetadataResolver metadataResolver;

    public DefaultVersionRangeResolver setLogger( Logger logger )
    {
        this.logger = ( logger != null ) ? logger : NullLogger.INSTANCE;
        return this;
    }

    public DefaultVersionRangeResolver setMetadataResolver( MetadataResolver metadataResolver )
    {
        if ( metadataResolver == null )
        {
            throw new IllegalArgumentException( "metadata resolver has not been specified" );
        }
        this.metadataResolver = metadataResolver;
        return this;
    }

    public VersionRangeResult resolveVersionRange( RepositorySession session, VersionRangeRequest request )
        throws VersionRangeResolutionException
    {
        VersionRangeResult result = new VersionRangeResult( request );

        String version = request.getArtifact().getVersion();

        VersionScheme versionScheme = new MavenVersionScheme();

        List<VersionRange> ranges;
        try
        {
            ranges = VersionRange.parseRanges( version, versionScheme );
        }
        catch ( InvalidVersionException e )
        {
            result.addException( e );
            throw new VersionRangeResolutionException( result );
        }

        result.setRange( !ranges.isEmpty() );

        if ( ranges.isEmpty() )
        {
            result.addVersion( version );
        }
        else
        {
            Map<String, ArtifactRepository> versionIndex =
                getVersions( session, result, request, getNature( session, ranges ) );

            List<Comparable<Object>> versions = new ArrayList<Comparable<Object>>();
            for ( String v : versionIndex.keySet() )
            {
                try
                {
                    Comparable<Object> ver = versionScheme.parseVersion( v );
                    if ( contained( ranges, ver ) )
                    {
                        versions.add( ver );
                        result.setRepository( v, versionIndex.get( v ) );
                    }
                }
                catch ( InvalidVersionException e )
                {
                    result.addException( e );
                }
            }

            Collections.sort( versions );
            for ( Comparable<Object> ver : versions )
            {
                result.addVersion( ver.toString() );
            }
        }

        return result;
    }

    private boolean contained( List<VersionRange> ranges, Comparable<Object> version )
    {
        for ( VersionRange range : ranges )
        {
            if ( range.containsVersion( version ) )
            {
                return true;
            }
        }
        return false;
    }

    private Map<String, ArtifactRepository> getVersions( RepositorySession session, VersionRangeResult result,
                                                         VersionRangeRequest request, Metadata.Nature nature )
    {
        Map<String, ArtifactRepository> versionIndex = new HashMap<String, ArtifactRepository>();

        DefaultMetadata metadata = new DefaultMetadata();
        metadata.setGroupId( request.getArtifact().getGroupId() );
        metadata.setArtifactId( request.getArtifact().getArtifactId() );
        metadata.setType( MAVEN_METADATA_XML );
        metadata.setNature( nature );

        List<MetadataRequest> metadataRequests = new ArrayList<MetadataRequest>( request.getRepositories().size() );
        for ( RemoteRepository repository : request.getRepositories() )
        {
            MetadataRequest metadataRequest =
                new MetadataRequest( new DefaultMetadata( metadata ), repository, request.getContext() );
            metadataRequest.setDeleteLocalCopyIfMissing( true );
            metadataRequests.add( metadataRequest );
        }
        List<MetadataResult> metadataResults = metadataResolver.resolveMetadata( session, metadataRequests );

        WorkspaceReader workspace = session.getWorkspaceReader();
        if ( workspace != null )
        {
            List<String> versions = workspace.findVersions( request.getArtifact() );
            for ( String version : versions )
            {
                versionIndex.put( version, workspace.getRepository() );
            }
        }

        LocalRepositoryManager lrm = session.getLocalRepositoryManager();
        File localMetadataFile = new File( lrm.getRepository().getBasedir(), lrm.getPathForLocalMetadata( metadata ) );
        if ( localMetadataFile.isFile() )
        {
            metadata.setFile( localMetadataFile );
            Versioning versioning = readVersions( session, metadata, result );
            for ( String version : versioning.getVersions() )
            {
                if ( !versionIndex.containsKey( version ) )
                {
                    versionIndex.put( version, lrm.getRepository() );
                }
            }
        }

        for ( MetadataResult metadataResult : metadataResults )
        {
            result.addException( metadataResult.getException() );
            Versioning versioning = readVersions( session, metadataResult.getRequest().getMetadata(), result );
            for ( String version : versioning.getVersions() )
            {
                if ( !versionIndex.containsKey( version ) )
                {
                    versionIndex.put( version, metadataResult.getRequest().getRepository() );
                }
            }
        }

        return versionIndex;
    }

    private Metadata.Nature getNature( RepositorySession session, List<VersionRange> ranges )
    {
        for ( VersionRange range : ranges )
        {
            if ( range.containsSnapshots() )
            {
                return Metadata.Nature.RELEASE_OR_SNAPSHOT;
            }
        }
        return Metadata.Nature.RELEASE;
    }

    private Versioning readVersions( RepositorySession session, Metadata metadata, VersionRangeResult result )
    {
        Versioning versioning = null;

        FileInputStream fis = null;
        try
        {
            if ( metadata.getFile() != null )
            {
                fis = new FileInputStream( metadata.getFile() );
                org.apache.maven.artifact.repository.metadata.Metadata m = new MetadataXpp3Reader().read( fis, false );
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

}
