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

import java.util.List;

import org.apache.maven.model.Repository;
import org.apache.maven.model.building.FileModelSource;
import org.apache.maven.model.building.ModelSource;
import org.apache.maven.model.resolution.InvalidRepositoryException;
import org.apache.maven.model.resolution.ModelResolver;
import org.apache.maven.model.resolution.UnresolvableModelException;
import org.apache.maven.repository.Artifact;
import org.apache.maven.repository.ArtifactResolutionException;
import org.apache.maven.repository.DefaultArtifact;
import org.apache.maven.repository.RemoteRepository;
import org.apache.maven.repository.RepositoryContext;
import org.apache.maven.repository.ArtifactRequest;
import org.apache.maven.repository.spi.ArtifactResolver;

/**
 * @author Benjamin Bentmann
 */
class DefaultModelResolver
    implements ModelResolver
{

    private final RepositoryContext context;

    private List<RemoteRepository> repositories;

    private final ArtifactResolver resolver;

    public DefaultModelResolver( RepositoryContext context, ArtifactResolver resolver, List<RemoteRepository> repositories )
    {
        this.context = context;
        this.resolver = resolver;
        this.repositories = repositories;
    }

    private DefaultModelResolver( DefaultModelResolver original )
    {
        this.context = original.context;
        this.resolver = original.resolver;
        this.repositories = original.repositories;
    }

    public void addRepository( Repository repository )
        throws InvalidRepositoryException
    {
        // TODO Auto-generated method stub

    }

    public ModelResolver newCopy()
    {
        return new DefaultModelResolver( this );
    }

    public ModelSource resolveModel( String groupId, String artifactId, String version )
        throws UnresolvableModelException
    {
        Artifact pomArtifact = new DefaultArtifact( groupId, artifactId, "", "pom", version );

        try
        {
            ArtifactRequest request = new ArtifactRequest( pomArtifact, repositories );
            resolver.resolveArtifact( context, request );
        }
        catch ( ArtifactResolutionException e )
        {
            throw new UnresolvableModelException( "Failed to resolve POM for " + groupId + ":" + artifactId + ":"
                + version + " due to " + e.getMessage(), groupId, artifactId, version, e );
        }

        return new FileModelSource( pomArtifact.getFile() );
    }

}
