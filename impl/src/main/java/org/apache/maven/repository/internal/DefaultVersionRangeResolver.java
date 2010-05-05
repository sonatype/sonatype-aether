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

import java.util.ArrayList;
import java.util.List;

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

        List<String> versions = new ArrayList<String>();

        WorkspaceReader workspace = context.getWorkspaceReader();
        if ( workspace != null )
        {
            versions.addAll( workspace.findVersions( request.getArtifact() ) );
            for ( String version : versions )
            {
                result.setRepository( version, workspace.getRepository() );
            }
        }

        Metadata metadata = new Metadata();
        metadata.setGroupId( request.getArtifact().getGroupId() );
        metadata.setArtifactId( request.getArtifact().getArtifactId() );
        metadata.setType( "maven-metadata.xml" );
        // FIXME: calculate the correct nature
        metadata.setNature( Metadata.Nature.RELEASE );

        List<MetadataRequest> metadataRequests =
            new ArrayList<MetadataRequest>( request.getRemoteRepositories().size() );
        for ( RemoteRepository repository : request.getRemoteRepositories() )
        {
            MetadataRequest metadataRequest = new MetadataRequest( new Metadata( metadata ), repository );
            metadataRequests.add( metadataRequest );
        }
        List<MetadataResult> metadataResults = metadataResolver.resolveMetadata( context, metadataRequests );

        // TODO Auto-generated method stub
        return result;
    }

}
