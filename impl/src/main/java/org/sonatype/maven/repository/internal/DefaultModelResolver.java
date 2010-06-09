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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.model.Repository;
import org.apache.maven.model.building.FileModelSource;
import org.apache.maven.model.building.ModelSource;
import org.apache.maven.model.resolution.InvalidRepositoryException;
import org.apache.maven.model.resolution.ModelResolver;
import org.apache.maven.model.resolution.UnresolvableModelException;
import org.sonatype.maven.repository.Artifact;
import org.sonatype.maven.repository.ArtifactRequest;
import org.sonatype.maven.repository.ArtifactResolutionException;
import org.sonatype.maven.repository.DefaultArtifact;
import org.sonatype.maven.repository.RemoteRepository;
import org.sonatype.maven.repository.RepositorySession;
import org.sonatype.maven.repository.spi.ArtifactResolver;
import org.sonatype.maven.repository.spi.RemoteRepositoryManager;

/**
 * @author Benjamin Bentmann
 */
public class DefaultModelResolver
    implements ModelResolver
{

    private final RepositorySession session;

    private final String context;

    private List<RemoteRepository> repositories;

    private final ArtifactResolver resolver;

    private final RemoteRepositoryManager remoteRepositoryManager;

    private final Set<String> repositoryIds;

    private final WorkspaceModelPool modelPool;

    public DefaultModelResolver( RepositorySession session, String context, ArtifactResolver resolver,
                                 RemoteRepositoryManager remoteRepositoryManager, List<RemoteRepository> repositories,
                                 WorkspaceModelPool modelPool )
    {
        this.session = session;
        this.context = context;
        this.resolver = resolver;
        this.remoteRepositoryManager = remoteRepositoryManager;
        this.repositories = repositories;
        this.repositoryIds = new HashSet<String>();
        this.modelPool = modelPool;
    }

    private DefaultModelResolver( DefaultModelResolver original )
    {
        this.session = original.session;
        this.context = original.context;
        this.resolver = original.resolver;
        this.remoteRepositoryManager = original.remoteRepositoryManager;
        this.repositories = original.repositories;
        this.repositoryIds = new HashSet<String>( original.repositoryIds );
        this.modelPool = original.modelPool;
    }

    public void addRepository( Repository repository )
        throws InvalidRepositoryException
    {
        if ( !repositoryIds.add( repository.getId() ) )
        {
            return;
        }

        RemoteRepository remoteRepository = DefaultArtifactDescriptorReader.convert( repository );

        this.repositories =
            remoteRepositoryManager.aggregateRepositories( session, repositories,
                                                           Collections.singletonList( remoteRepository ) );
    }

    public ModelResolver newCopy()
    {
        return new DefaultModelResolver( this );
    }

    public ModelSource resolveModel( String groupId, String artifactId, String version )
        throws UnresolvableModelException
    {
        File pomFile = null;

        if ( modelPool != null )
        {
            pomFile = modelPool.get( groupId, artifactId, version );
        }

        if ( pomFile == null )
        {
            Artifact pomArtifact = new DefaultArtifact( groupId, artifactId, "", "pom", version );

            try
            {
                ArtifactRequest request = new ArtifactRequest( pomArtifact, repositories, context );
                resolver.resolveArtifact( session, request );
            }
            catch ( ArtifactResolutionException e )
            {
                throw new UnresolvableModelException( "Failed to resolve POM for " + groupId + ":" + artifactId + ":"
                    + version + " due to " + e.getMessage(), groupId, artifactId, version, e );
            }

            pomFile = pomArtifact.getFile();
        }

        return new FileModelSource( pomFile );
    }

}
