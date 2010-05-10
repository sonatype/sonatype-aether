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

import java.util.Collection;
import java.util.List;

import org.apache.maven.repository.ArtifactDescriptorException;
import org.apache.maven.repository.ArtifactResolutionException;
import org.apache.maven.repository.CollectRequest;
import org.apache.maven.repository.CollectResult;
import org.apache.maven.repository.ArtifactDescriptorRequest;
import org.apache.maven.repository.ArtifactDescriptorResult;
import org.apache.maven.repository.DependencyCollectionException;
import org.apache.maven.repository.DeployRequest;
import org.apache.maven.repository.InstallRequest;
import org.apache.maven.repository.RemoteRepository;
import org.apache.maven.repository.RepositoryContext;
import org.apache.maven.repository.RepositorySystem;
import org.apache.maven.repository.ArtifactRequest;
import org.apache.maven.repository.ArtifactResult;
import org.apache.maven.repository.TransformRequest;
import org.apache.maven.repository.TransformResult;
import org.apache.maven.repository.VersionRangeRequest;
import org.apache.maven.repository.VersionRangeResolutionException;
import org.apache.maven.repository.VersionRangeResult;
import org.apache.maven.repository.VersionRequest;
import org.apache.maven.repository.VersionResolutionException;
import org.apache.maven.repository.VersionResult;
import org.apache.maven.repository.spi.ArtifactDescriptorReader;
import org.apache.maven.repository.spi.ArtifactResolver;
import org.apache.maven.repository.spi.DependencyCollector;
import org.apache.maven.repository.spi.VersionRangeResolver;
import org.apache.maven.repository.spi.VersionResolver;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

/**
 * @author Benjamin Bentmann
 */
@Component( role = RepositorySystem.class )
public class DefaultRepositorySystem
    implements RepositorySystem
{

    @Requirement
    private VersionResolver versionResolver;

    @Requirement
    private VersionRangeResolver versionRangeResolver;

    @Requirement
    private ArtifactResolver artifactResolver;

    @Requirement
    private ArtifactDescriptorReader artifactDescriptorReader;

    @Requirement
    private DependencyCollector dependencyCollector;

    public DefaultRepositorySystem setVersionResolver( VersionResolver versionResolver )
    {
        if ( versionResolver == null )
        {
            throw new IllegalArgumentException( "version resolver has not been specified" );
        }
        this.versionResolver = versionResolver;
        return this;
    }

    public DefaultRepositorySystem setVersionRangeResolver( VersionRangeResolver versionRangeResolver )
    {
        if ( versionRangeResolver == null )
        {
            throw new IllegalArgumentException( "version range resolver has not been specified" );
        }
        this.versionRangeResolver = versionRangeResolver;
        return this;
    }

    public DefaultRepositorySystem setArtifactResolver( ArtifactResolver artifactResolver )
    {
        if ( artifactResolver == null )
        {
            throw new IllegalArgumentException( "artifact resolver has not been specified" );
        }
        this.artifactResolver = artifactResolver;
        return this;
    }

    public DefaultRepositorySystem setArtifactDescriptorReader( ArtifactDescriptorReader artifactDescriptorReader )
    {
        if ( artifactDescriptorReader == null )
        {
            throw new IllegalArgumentException( "artifact descriptor reader has not been specified" );
        }
        this.artifactDescriptorReader = artifactDescriptorReader;
        return this;
    }

    public VersionResult resolveVersion( RepositoryContext context, VersionRequest request )
        throws VersionResolutionException
    {
        return versionResolver.resolveVersion( context, request );
    }

    public VersionRangeResult resolveVersionRange( RepositoryContext context, VersionRangeRequest request )
        throws VersionRangeResolutionException
    {
        return versionRangeResolver.resolveVersionRange( context, request );
    }

    public List<ArtifactResult> resolveArtifacts( RepositoryContext context,
                                                  Collection<? extends ArtifactRequest> requests )
        throws ArtifactResolutionException
    {
        return artifactResolver.resolveArtifacts( context, requests );
    }

    public CollectResult collectDependencies( RepositoryContext context, CollectRequest request )
        throws DependencyCollectionException
    {
        return dependencyCollector.collectDependencies( context, request );
    }

    public void deployArtifacts( RepositoryContext context, DeployRequest request )
    {
        // TODO Auto-generated method stub

    }

    public ArtifactDescriptorResult readArtifactDescriptor( RepositoryContext context, ArtifactDescriptorRequest request )
        throws ArtifactDescriptorException
    {
        return artifactDescriptorReader.readArtifactDescriptor( context, request );
    }

    public List<RemoteRepository> getEffectiveRepositories( RepositoryContext context,
                                                            Collection<? extends RemoteRepository> repositories )
    {
        // TODO Auto-generated method stub
        return null;
    }

    public void installArtifacts( RepositoryContext context, InstallRequest request )
    {
        // TODO Auto-generated method stub

    }

    public TransformResult transformDependencies( RepositoryContext context, TransformRequest request )
    {
        // TODO Auto-generated method stub
        return null;
    }

}
