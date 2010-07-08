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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.maven.repository.Artifact;
import org.sonatype.maven.repository.ArtifactDescriptorException;
import org.sonatype.maven.repository.ArtifactDescriptorRequest;
import org.sonatype.maven.repository.ArtifactDescriptorResult;
import org.sonatype.maven.repository.ArtifactRequest;
import org.sonatype.maven.repository.ArtifactResolutionException;
import org.sonatype.maven.repository.ArtifactResult;
import org.sonatype.maven.repository.CollectRequest;
import org.sonatype.maven.repository.CollectResult;
import org.sonatype.maven.repository.Dependency;
import org.sonatype.maven.repository.DependencyCollectionException;
import org.sonatype.maven.repository.DependencyFilter;
import org.sonatype.maven.repository.DependencyNode;
import org.sonatype.maven.repository.DeployRequest;
import org.sonatype.maven.repository.DeployResult;
import org.sonatype.maven.repository.DeploymentException;
import org.sonatype.maven.repository.InstallRequest;
import org.sonatype.maven.repository.InstallResult;
import org.sonatype.maven.repository.InstallationException;
import org.sonatype.maven.repository.LocalRepository;
import org.sonatype.maven.repository.LocalRepositoryManager;
import org.sonatype.maven.repository.MetadataRequest;
import org.sonatype.maven.repository.MetadataResult;
import org.sonatype.maven.repository.RepositorySystemSession;
import org.sonatype.maven.repository.RepositorySystem;
import org.sonatype.maven.repository.VersionRangeRequest;
import org.sonatype.maven.repository.VersionRangeResolutionException;
import org.sonatype.maven.repository.VersionRangeResult;
import org.sonatype.maven.repository.VersionRequest;
import org.sonatype.maven.repository.VersionResolutionException;
import org.sonatype.maven.repository.VersionResult;
import org.sonatype.maven.repository.spi.ArtifactDescriptorReader;
import org.sonatype.maven.repository.spi.ArtifactResolver;
import org.sonatype.maven.repository.spi.DependencyCollector;
import org.sonatype.maven.repository.spi.Deployer;
import org.sonatype.maven.repository.spi.Installer;
import org.sonatype.maven.repository.spi.Logger;
import org.sonatype.maven.repository.spi.MetadataResolver;
import org.sonatype.maven.repository.spi.NullLogger;
import org.sonatype.maven.repository.spi.VersionRangeResolver;
import org.sonatype.maven.repository.spi.VersionResolver;

/**
 * @author Benjamin Bentmann
 */
@Component( role = RepositorySystem.class )
public class DefaultRepositorySystem
    implements RepositorySystem
{

    @Requirement
    private Logger logger = NullLogger.INSTANCE;

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

    public DefaultRepositorySystem setDependencyCollector( DependencyCollector dependencyCollector )
    {
        if ( dependencyCollector == null )
        {
            throw new IllegalArgumentException( "dependency collector has not been specified" );
        }
        this.dependencyCollector = dependencyCollector;
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

    public VersionResult resolveVersion( RepositorySystemSession session, VersionRequest request )
        throws VersionResolutionException
    {
        return versionResolver.resolveVersion( session, request );
    }

    public VersionRangeResult resolveVersionRange( RepositorySystemSession session, VersionRangeRequest request )
        throws VersionRangeResolutionException
    {
        return versionRangeResolver.resolveVersionRange( session, request );
    }

    public ArtifactDescriptorResult readArtifactDescriptor( RepositorySystemSession session,
                                                            ArtifactDescriptorRequest request )
        throws ArtifactDescriptorException
    {
        return artifactDescriptorReader.readArtifactDescriptor( session, request );
    }

    public ArtifactResult resolveArtifact( RepositorySystemSession session, ArtifactRequest request )
        throws ArtifactResolutionException
    {
        return artifactResolver.resolveArtifact( session, request );
    }

    public List<ArtifactResult> resolveArtifacts( RepositorySystemSession session,
                                                  Collection<? extends ArtifactRequest> requests )
        throws ArtifactResolutionException
    {
        return artifactResolver.resolveArtifacts( session, requests );
    }

    public List<MetadataResult> resolveMetadata( RepositorySystemSession session,
                                                 Collection<? extends MetadataRequest> requests )
    {
        return metadataResolver.resolveMetadata( session, requests );
    }

    public CollectResult collectDependencies( RepositorySystemSession session, CollectRequest request )
        throws DependencyCollectionException
    {
        return dependencyCollector.collectDependencies( session, request );
    }

    public List<ArtifactResult> resolveDependencies( RepositorySystemSession session, DependencyNode node,
                                                     DependencyFilter filter )
        throws ArtifactResolutionException
    {
        List<ArtifactRequest> requests = new ArrayList<ArtifactRequest>();
        toArtifactRequest( requests, node, filter );

        List<ArtifactResult> results = resolveArtifacts( session, requests );

        for ( ArtifactResult result : results )
        {
            Artifact artifact = result.getArtifact();
            if ( artifact != null && artifact.getFile() != null )
            {
                result.getRequest().getDependencyNode().setArtifact( artifact );
            }
        }

        return results;
    }

    private void toArtifactRequest( List<ArtifactRequest> requests, DependencyNode node, DependencyFilter filter )
    {
        Dependency dependency = node.getDependency();
        if ( dependency != null && ( filter == null || filter.filterDependency( node ) ) )
        {
            ArtifactRequest request = new ArtifactRequest( node, node.getRepositories(), node.getContext() );
            requests.add( request );
        }

        for ( DependencyNode child : node.getChildren() )
        {
            toArtifactRequest( requests, child, filter );
        }
    }

    public List<ArtifactResult> resolveDependencies( RepositorySystemSession session, CollectRequest request,
                                                     DependencyFilter filter )
        throws DependencyCollectionException, ArtifactResolutionException
    {
        CollectResult result = collectDependencies( session, request );
        return resolveDependencies( session, result.getRoot(), filter );
    }

    public InstallResult install( RepositorySystemSession session, InstallRequest request )
        throws InstallationException
    {
        return installer.install( session, request );
    }

    public DeployResult deploy( RepositorySystemSession session, DeployRequest request )
        throws DeploymentException
    {
        return deployer.deploy( session, request );
    }

    public LocalRepositoryManager newLocalRepositoryManager( LocalRepository localRepository )
    {
        String type = localRepository.getContentType();
        File basedir = localRepository.getBasedir();

        if ( "".equals( type ) || "enhanced".equals( type ) )
        {
            return new EnhancedLocalRepositoryManager( basedir ).setLogger( logger );
        }
        else if ( "simple".equals( type ) )
        {
            return new SimpleLocalRepositoryManager( basedir ).setLogger( logger );
        }
        else
        {
            throw new IllegalArgumentException( "Invalid repository type: " + type );
        }
    }

}
