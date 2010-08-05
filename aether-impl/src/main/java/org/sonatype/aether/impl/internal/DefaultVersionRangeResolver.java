package org.sonatype.aether.impl.internal;

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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.aether.ArtifactRepository;
import org.sonatype.aether.DefaultMetadata;
import org.sonatype.aether.InvalidVersionException;
import org.sonatype.aether.InvalidVersionRangeException;
import org.sonatype.aether.LocalRepositoryManager;
import org.sonatype.aether.Metadata;
import org.sonatype.aether.MetadataRequest;
import org.sonatype.aether.MetadataResult;
import org.sonatype.aether.RemoteRepository;
import org.sonatype.aether.RepositoryListener;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.Version;
import org.sonatype.aether.VersionConstraint;
import org.sonatype.aether.VersionRange;
import org.sonatype.aether.VersionRangeRequest;
import org.sonatype.aether.VersionRangeResolutionException;
import org.sonatype.aether.VersionRangeResult;
import org.sonatype.aether.VersionScheme;
import org.sonatype.aether.WorkspaceReader;
import org.sonatype.aether.util.listener.DefaultRepositoryEvent;
import org.sonatype.aether.util.version.MavenVersionScheme;
import org.sonatype.aether.impl.MetadataResolver;
import org.sonatype.aether.impl.VersionRangeResolver;
import org.sonatype.aether.impl.metadata.Versioning;
import org.sonatype.aether.impl.metadata.io.xpp3.MetadataXpp3Reader;
import org.sonatype.aether.spi.locator.Service;
import org.sonatype.aether.spi.locator.ServiceLocator;
import org.sonatype.aether.spi.log.Logger;
import org.sonatype.aether.spi.log.NullLogger;

/**
 * @author Benjamin Bentmann
 */
@Component( role = VersionRangeResolver.class )
public class DefaultVersionRangeResolver
    implements VersionRangeResolver, Service
{

    private static final String MAVEN_METADATA_XML = "maven-metadata.xml";

    @Requirement
    private Logger logger = NullLogger.INSTANCE;

    @Requirement
    private MetadataResolver metadataResolver;

    public void initService( ServiceLocator locator )
    {
        setLogger( locator.getService( Logger.class ) );
        setMetadataResolver( locator.getService( MetadataResolver.class ) );
    }

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

    public VersionRangeResult resolveVersionRange( RepositorySystemSession session, VersionRangeRequest request )
        throws VersionRangeResolutionException
    {
        VersionRangeResult result = new VersionRangeResult( request );

        VersionScheme versionScheme = new MavenVersionScheme();

        VersionConstraint versionConstraint;
        try
        {
            versionConstraint = parseConstraint( request.getArtifact().getVersion(), versionScheme );
        }
        catch ( InvalidVersionException e )
        {
            result.addException( e );
            throw new VersionRangeResolutionException( result );
        }
        catch ( InvalidVersionRangeException e )
        {
            result.addException( e );
            throw new VersionRangeResolutionException( result );
        }        

        result.setVersionConstraint( versionConstraint );

        if ( versionConstraint.getRanges().isEmpty() )
        {
            result.addVersion( versionConstraint.getPreferredVersion() );
        }
        else
        {
            Map<String, ArtifactRepository> versionIndex =
                getVersions( session, result, request, getNature( session, versionConstraint.getRanges() ) );

            List<Version> versions = new ArrayList<Version>();
            for ( Map.Entry<String, ArtifactRepository> v : versionIndex.entrySet() )
            {
                try
                {
                    Version ver = versionScheme.parseVersion( v.getKey() );
                    if ( versionConstraint.containsVersion( ver ) )
                    {
                        versions.add( ver );
                        result.setRepository( ver, v.getValue() );
                    }
                }
                catch ( InvalidVersionException e )
                {
                    result.addException( e );
                }
            }

            Collections.sort( versions );
            for ( Version ver : versions )
            {
                result.addVersion( ver );
                versionConstraint.setPreferredVersion( ver );
            }
        }

        return result;
    }

    private Map<String, ArtifactRepository> getVersions( RepositorySystemSession session, VersionRangeResult result,
                                                         VersionRangeRequest request, Metadata.Nature nature )
    {
        Map<String, ArtifactRepository> versionIndex = new HashMap<String, ArtifactRepository>();

        Metadata metadata =
            new DefaultMetadata( request.getArtifact().getGroupId(), request.getArtifact().getArtifactId(),
                                 MAVEN_METADATA_XML, nature );

        List<MetadataRequest> metadataRequests = new ArrayList<MetadataRequest>( request.getRepositories().size() );
        for ( RemoteRepository repository : request.getRepositories() )
        {
            MetadataRequest metadataRequest = new MetadataRequest( metadata, repository, request.getRequestContext() );
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
            metadata = metadata.setFile( localMetadataFile );
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
            Versioning versioning = readVersions( session, metadataResult.getMetadata(), result );
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

    private Metadata.Nature getNature( RepositorySystemSession session, Collection<VersionRange> ranges )
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

    private Versioning readVersions( RepositorySystemSession session, Metadata metadata, VersionRangeResult result )
    {
        Versioning versioning = null;

        FileInputStream fis = null;
        try
        {
            if ( metadata != null && metadata.getFile() != null )
            {
                fis = new FileInputStream( metadata.getFile() );
                org.sonatype.aether.impl.metadata.Metadata m = new MetadataXpp3Reader().read( fis, false );
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

    private void invalidMetadata( RepositorySystemSession session, Metadata metadata, Exception exception )
    {
        RepositoryListener listener = session.getRepositoryListener();
        if ( listener != null )
        {
            DefaultRepositoryEvent event = new DefaultRepositoryEvent( session, metadata );
            event.setException( exception );
            listener.metadataInvalid( event );
        }
    }

    private VersionConstraint parseConstraint( String version, VersionScheme scheme )
        throws InvalidVersionException, InvalidVersionRangeException
    {
        VersionConstraint constraint = new VersionConstraint();

        String process = version;

        while ( process.startsWith( "[" ) || process.startsWith( "(" ) )
        {
            int index1 = process.indexOf( ")" );
            int index2 = process.indexOf( "]" );

            int index = index2;
            if ( index2 < 0 || ( index1 >= 0 && index1 < index2 ) )
            {
                index = index1;
            }

            if ( index < 0 )
            {
                throw new InvalidVersionException( version, "Unbounded version range " + version );
            }

            VersionRange range = scheme.parseVersionRange( process.substring( 0, index + 1 ) );
            constraint.addRange( range );

            process = process.substring( index + 1 ).trim();

            if ( process.length() > 0 && process.startsWith( "," ) )
            {
                process = process.substring( 1 ).trim();
            }
        }

        if ( process.length() > 0 && !constraint.getRanges().isEmpty() )
        {
            throw new InvalidVersionException( version, "Invalid version range " + version
                + ", expected [ or ( but got " + process );
        }

        if ( constraint.getRanges().isEmpty() )
        {
            constraint.setPreferredVersion( scheme.parseVersion( version ) );
        }

        return constraint;
    }

}
