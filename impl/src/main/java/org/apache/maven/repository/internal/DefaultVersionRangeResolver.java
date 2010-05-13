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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.artifact.repository.metadata.Versioning;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Reader;
import org.apache.maven.repository.ArtifactRepository;
import org.apache.maven.repository.InvalidVersionException;
import org.apache.maven.repository.LocalRepositoryManager;
import org.apache.maven.repository.Metadata;
import org.apache.maven.repository.MetadataRequest;
import org.apache.maven.repository.MetadataResult;
import org.apache.maven.repository.RemoteRepository;
import org.apache.maven.repository.RepositoryContext;
import org.apache.maven.repository.VersionRangeRequest;
import org.apache.maven.repository.VersionRangeResolutionException;
import org.apache.maven.repository.VersionRangeResult;
import org.apache.maven.repository.WorkspaceReader;
import org.apache.maven.repository.spi.Logger;
import org.apache.maven.repository.spi.MetadataResolver;
import org.apache.maven.repository.spi.NullLogger;
import org.apache.maven.repository.spi.VersionRangeResolver;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.IOUtil;

/**
 * @author Benjamin Bentmann
 */
@Component( role = VersionRangeResolver.class )
public class DefaultVersionRangeResolver
    implements VersionRangeResolver
{

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

    public VersionRangeResult resolveVersionRange( RepositoryContext context, VersionRangeRequest request )
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
                getVersions( context, result, request, getNature( context, ranges ) );

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

    private Map<String, ArtifactRepository> getVersions( RepositoryContext context, VersionRangeResult result,
                                                         VersionRangeRequest request, Metadata.Nature nature )
    {
        Map<String, ArtifactRepository> versionIndex = new HashMap<String, ArtifactRepository>();

        Metadata metadata = new Metadata();
        metadata.setGroupId( request.getArtifact().getGroupId() );
        metadata.setArtifactId( request.getArtifact().getArtifactId() );
        metadata.setType( "maven-metadata.xml" );
        metadata.setNature( nature );

        List<MetadataRequest> metadataRequests =
            new ArrayList<MetadataRequest>( request.getRepositories().size() );
        for ( RemoteRepository repository : request.getRepositories() )
        {
            MetadataRequest metadataRequest = new MetadataRequest( new Metadata( metadata ), repository );
            metadataRequest.setDeleteLocalCopyIfMissing( true );
            metadataRequests.add( metadataRequest );
        }
        List<MetadataResult> metadataResults = metadataResolver.resolveMetadata( context, metadataRequests );

        WorkspaceReader workspace = context.getWorkspaceReader();
        if ( workspace != null )
        {
            List<String> versions = workspace.findVersions( request.getArtifact() );
            for ( String version : versions )
            {
                versionIndex.put( version, workspace.getRepository() );
            }
        }

        LocalRepositoryManager lrm = context.getLocalRepositoryManager();
        File localMetadataFile = new File( lrm.getRepository().getBasedir(), lrm.getPathForLocalMetadata( metadata ) );
        if ( localMetadataFile.isFile() )
        {
            metadata.setFile( localMetadataFile );
            Versioning versioning = readVersions( context, metadata, result );
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
            Versioning versioning = readVersions( context, metadataResult.getRequest().getMetadata(), result );
            for ( String version : versioning.getVersions() )
            {
                if ( !versionIndex.containsKey( version ) )
                {
                    versionIndex.put( version, metadataResult.getRequest().getRemoteRepository() );
                }
            }
        }

        return versionIndex;
    }

    private Metadata.Nature getNature( RepositoryContext context, List<VersionRange> ranges )
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

    private Versioning readVersions( RepositoryContext context, Metadata metadata, VersionRangeResult result )
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

}
