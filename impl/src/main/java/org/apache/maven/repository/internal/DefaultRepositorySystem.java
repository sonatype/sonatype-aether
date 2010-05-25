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
import java.util.Collection;
import java.util.List;

import org.apache.maven.repository.ArtifactDescriptorException;
import org.apache.maven.repository.ArtifactResolutionException;
import org.apache.maven.repository.CollectRequest;
import org.apache.maven.repository.CollectResult;
import org.apache.maven.repository.ArtifactDescriptorRequest;
import org.apache.maven.repository.ArtifactDescriptorResult;
import org.apache.maven.repository.Dependency;
import org.apache.maven.repository.DependencyCollectionException;
import org.apache.maven.repository.DependencyNode;
import org.apache.maven.repository.DeployRequest;
import org.apache.maven.repository.DeploymentException;
import org.apache.maven.repository.InstallRequest;
import org.apache.maven.repository.InstallationException;
import org.apache.maven.repository.MetadataRequest;
import org.apache.maven.repository.MetadataResult;
import org.apache.maven.repository.RepositorySession;
import org.apache.maven.repository.RepositorySystem;
import org.apache.maven.repository.ArtifactRequest;
import org.apache.maven.repository.ArtifactResult;
import org.apache.maven.repository.VersionRangeRequest;
import org.apache.maven.repository.VersionRangeResolutionException;
import org.apache.maven.repository.VersionRangeResult;
import org.apache.maven.repository.VersionRequest;
import org.apache.maven.repository.VersionResolutionException;
import org.apache.maven.repository.VersionResult;
import org.apache.maven.repository.spi.ArtifactDescriptorReader;
import org.apache.maven.repository.spi.ArtifactResolver;
import org.apache.maven.repository.spi.DependencyCollector;
import org.apache.maven.repository.spi.Deployer;
import org.apache.maven.repository.spi.Installer;
import org.apache.maven.repository.spi.MetadataResolver;
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
    private MetadataResolver metadataResolver;

    @Requirement
    private ArtifactDescriptorReader artifactDescriptorReader;

    @Requirement
    private DependencyCollector dependencyCollector;

    @Requirement
    private Installer installer;

    @Requirement
    private Deployer deployer;

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

    public DefaultRepositorySystem setMetadataResolver( MetadataResolver metadataResolver )
    {
        if ( metadataResolver == null )
        {
            throw new IllegalArgumentException( "metadata resolver has not been specified" );
        }
        this.metadataResolver = metadataResolver;
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

    public DefaultRepositorySystem setInstaller( Installer installer )
    {
        if ( installer == null )
        {
            throw new IllegalArgumentException( "installer has not been specified" );
        }
        this.installer = installer;
        return this;
    }

    public DefaultRepositorySystem setDeployer( Deployer deployer )
    {
        if ( deployer == null )
        {
            throw new IllegalArgumentException( "deployer has not been specified" );
        }
        this.deployer = deployer;
        return this;
    }

    public VersionResult resolveVersion( RepositorySession session, VersionRequest request )
        throws VersionResolutionException
    {
        return versionResolver.resolveVersion( session, request );
    }

    public VersionRangeResult resolveVersionRange( RepositorySession session, VersionRangeRequest request )
        throws VersionRangeResolutionException
    {
        return versionRangeResolver.resolveVersionRange( session, request );
    }

    public ArtifactDescriptorResult readArtifactDescriptor( RepositorySession session, ArtifactDescriptorRequest request )
        throws ArtifactDescriptorException
    {
        return artifactDescriptorReader.readArtifactDescriptor( session, request );
    }

    public List<ArtifactResult> resolveArtifacts( RepositorySession session,
                                                  Collection<? extends ArtifactRequest> requests )
        throws ArtifactResolutionException
    {
        return artifactResolver.resolveArtifacts( session, requests );
    }

    public List<MetadataResult> resolveMetadata( RepositorySession session,
                                                 Collection<? extends MetadataRequest> requests )
    {
        return metadataResolver.resolveMetadata( session, requests );
    }

    public CollectResult collectDependencies( RepositorySession session, CollectRequest request )
        throws DependencyCollectionException
    {
        return dependencyCollector.collectDependencies( session, request );
    }

    public void resolveDependencies( RepositorySession session, DependencyNode node )
        throws ArtifactResolutionException
    {
        List<ArtifactRequest> requests = new ArrayList<ArtifactRequest>();
        toArtifactRequest( requests, node );

        resolveArtifacts( session, requests );
    }

    private void toArtifactRequest( List<ArtifactRequest> requests, DependencyNode node )
    {
        Dependency dependency = node.getDependency();
        if ( dependency != null )
        {
            ArtifactRequest request =
                new ArtifactRequest( dependency.getArtifact(), node.getRepositories(), node.getContext() );
            requests.add( request );
        }

        for ( DependencyNode child : node.getChildren() )
        {
            toArtifactRequest( requests, child );
        }
    }

    public void install( RepositorySession session, InstallRequest request )
        throws InstallationException
    {
        installer.install( session, request );
    }

    public void deploy( RepositorySession session, DeployRequest request )
        throws DeploymentException
    {
        deployer.deploy( session, request );
    }

}
