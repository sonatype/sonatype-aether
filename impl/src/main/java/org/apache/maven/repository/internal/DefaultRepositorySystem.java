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

import org.apache.maven.repository.ArtifactResolutionException;
import org.apache.maven.repository.CollectRequest;
import org.apache.maven.repository.CollectResult;
import org.apache.maven.repository.DependencyRequest;
import org.apache.maven.repository.DependencyResult;
import org.apache.maven.repository.DeployRequest;
import org.apache.maven.repository.InstallRequest;
import org.apache.maven.repository.RemoteRepository;
import org.apache.maven.repository.RepositoryContext;
import org.apache.maven.repository.RepositorySystem;
import org.apache.maven.repository.ResolveRequest;
import org.apache.maven.repository.ResolveResult;
import org.apache.maven.repository.TransformRequest;
import org.apache.maven.repository.TransformResult;
import org.apache.maven.repository.VersionRangeRequest;
import org.apache.maven.repository.VersionRangeResult;
import org.apache.maven.repository.VersionRequest;
import org.apache.maven.repository.VersionResolutionException;
import org.apache.maven.repository.VersionResult;
import org.apache.maven.repository.spi.ArtifactResolver;
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
    private ArtifactResolver artifactResolver;

    public DefaultRepositorySystem setVersionResolver( VersionResolver versionResolver )
    {
        if ( versionResolver == null )
        {
            throw new IllegalArgumentException( "version resolver has not been specified" );
        }
        this.versionResolver = versionResolver;
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

    public VersionResult resolveVersion( RepositoryContext context, VersionRequest request )
        throws VersionResolutionException
    {
        return versionResolver.resolveVersion( context, request );
    }

    public List<ResolveResult> resolveArtifacts( RepositoryContext context,
                                                 Collection<? extends ResolveRequest> requests )
        throws ArtifactResolutionException
    {
        return artifactResolver.resolveArtifacts( context, requests );
    }

    public CollectResult collectDependencies( RepositoryContext context, CollectRequest request )
    {
        // TODO Auto-generated method stub
        return null;
    }

    public void deployArtifacts( RepositoryContext context, DeployRequest request )
    {
        // TODO Auto-generated method stub
        
    }

    public DependencyResult getDependencies( RepositoryContext context, DependencyRequest request )
    {
        // TODO Auto-generated method stub
        return null;
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

    public VersionRangeResult resolveVersionRange( RepositoryContext context, VersionRangeRequest request )
    {
        // TODO Auto-generated method stub
        return null;
    }

    public TransformResult transformDependencies( RepositoryContext context, TransformRequest request )
    {
        // TODO Auto-generated method stub
        return null;
    }

}
