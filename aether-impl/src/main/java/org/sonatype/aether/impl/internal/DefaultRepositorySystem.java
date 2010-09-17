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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.collection.CollectRequest;
import org.sonatype.aether.collection.CollectResult;
import org.sonatype.aether.collection.DependencyCollectionException;
import org.sonatype.aether.deployment.DeployRequest;
import org.sonatype.aether.deployment.DeployResult;
import org.sonatype.aether.deployment.DeploymentException;
import org.sonatype.aether.graph.DependencyFilter;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.graph.DependencyVisitor;
import org.sonatype.aether.impl.ArtifactDescriptorReader;
import org.sonatype.aether.impl.ArtifactResolver;
import org.sonatype.aether.impl.DependencyCollector;
import org.sonatype.aether.impl.Deployer;
import org.sonatype.aether.impl.Installer;
import org.sonatype.aether.impl.MetadataResolver;
import org.sonatype.aether.impl.VersionRangeResolver;
import org.sonatype.aether.impl.VersionResolver;
import org.sonatype.aether.installation.InstallRequest;
import org.sonatype.aether.installation.InstallResult;
import org.sonatype.aether.installation.InstallationException;
import org.sonatype.aether.repository.LocalRepository;
import org.sonatype.aether.repository.LocalRepositoryManager;
import org.sonatype.aether.resolution.ArtifactDescriptorException;
import org.sonatype.aether.resolution.ArtifactDescriptorRequest;
import org.sonatype.aether.resolution.ArtifactDescriptorResult;
import org.sonatype.aether.resolution.ArtifactRequest;
import org.sonatype.aether.resolution.ArtifactResolutionException;
import org.sonatype.aether.resolution.ArtifactResult;
import org.sonatype.aether.resolution.MetadataRequest;
import org.sonatype.aether.resolution.MetadataResult;
import org.sonatype.aether.resolution.VersionRangeRequest;
import org.sonatype.aether.resolution.VersionRangeResolutionException;
import org.sonatype.aether.resolution.VersionRangeResult;
import org.sonatype.aether.resolution.VersionRequest;
import org.sonatype.aether.resolution.VersionResolutionException;
import org.sonatype.aether.resolution.VersionResult;
import org.sonatype.aether.spi.locator.Service;
import org.sonatype.aether.spi.locator.ServiceLocator;
import org.sonatype.aether.spi.log.Logger;
import org.sonatype.aether.spi.log.NullLogger;
import org.sonatype.aether.util.graph.FilteringDependencyVisitor;
import org.sonatype.aether.util.graph.TreeDependencyVisitor;

/**
 * @author Benjamin Bentmann
 */
@Component( role = RepositorySystem.class )
public class DefaultRepositorySystem
    implements RepositorySystem, Service
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

    private static Set<RepositorySystemSession> validated = new HashSet<RepositorySystemSession>();

    public DefaultRepositorySystem()
    {
        // enables default constructor
    }

    public DefaultRepositorySystem( Logger logger, VersionResolver versionResolver,
                                    VersionRangeResolver versionRangeResolver, ArtifactResolver artifactResolver,
                                    MetadataResolver metadataResolver,
                                    ArtifactDescriptorReader artifactDescriptorReader,
                                    DependencyCollector dependencyCollector, Installer installer, Deployer deployer )
    {
        setLogger( logger );
        setVersionResolver( versionResolver );
        setVersionRangeResolver( versionRangeResolver );
        setArtifactResolver( artifactResolver );
        setMetadataResolver( metadataResolver );
        setArtifactDescriptorReader( artifactDescriptorReader );
        setDependencyCollector( dependencyCollector );
        setInstaller( installer );
        setDeployer( deployer );
    }

    public void initService( ServiceLocator locator )
    {
        setLogger( locator.getService( Logger.class ) );
        setVersionResolver( locator.getService( VersionResolver.class ) );
        setVersionRangeResolver( locator.getService( VersionRangeResolver.class ) );
        setArtifactResolver( locator.getService( ArtifactResolver.class ) );
        setMetadataResolver( locator.getService( MetadataResolver.class ) );
        setArtifactDescriptorReader( locator.getService( ArtifactDescriptorReader.class ) );
        setDependencyCollector( locator.getService( DependencyCollector.class ) );
        setInstaller( locator.getService( Installer.class ) );
        setDeployer( locator.getService( Deployer.class ) );
    }

    public DefaultRepositorySystem setLogger( Logger logger )
    {
        this.logger = ( logger != null ) ? logger : NullLogger.INSTANCE;
        return this;
    }

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
        validateSession( session );
        return versionResolver.resolveVersion( session, request );
    }

    public VersionRangeResult resolveVersionRange( RepositorySystemSession session, VersionRangeRequest request )
        throws VersionRangeResolutionException
    {
        validateSession( session );
        return versionRangeResolver.resolveVersionRange( session, request );
    }

    public ArtifactDescriptorResult readArtifactDescriptor( RepositorySystemSession session,
                                                            ArtifactDescriptorRequest request )
        throws ArtifactDescriptorException
    {
        validateSession( session );
        return artifactDescriptorReader.readArtifactDescriptor( session, request );
    }

    public ArtifactResult resolveArtifact( RepositorySystemSession session, ArtifactRequest request )
        throws ArtifactResolutionException
    {
        validateSession( session );
        return artifactResolver.resolveArtifact( session, request );
    }

    public List<ArtifactResult> resolveArtifacts( RepositorySystemSession session,
                                                  Collection<? extends ArtifactRequest> requests )
        throws ArtifactResolutionException
    {
        validateSession( session );
        return artifactResolver.resolveArtifacts( session, requests );
    }

    public List<MetadataResult> resolveMetadata( RepositorySystemSession session,
                                                 Collection<? extends MetadataRequest> requests )
    {
        validateSession( session );
        return metadataResolver.resolveMetadata( session, requests );
    }

    public CollectResult collectDependencies( RepositorySystemSession session, CollectRequest request )
        throws DependencyCollectionException
    {
        validateSession( session );
        return dependencyCollector.collectDependencies( session, request );
    }

    public List<ArtifactResult> resolveDependencies( RepositorySystemSession session, DependencyNode node,
                                                     DependencyFilter filter )
        throws ArtifactResolutionException
    {
        validateSession( session );
        ArtifactRequestBuilder builder = new ArtifactRequestBuilder();
        DependencyVisitor visitor = ( filter != null ) ? new FilteringDependencyVisitor( builder, filter ) : builder;
        visitor = new TreeDependencyVisitor( visitor );
        node.accept( visitor );
        List<ArtifactRequest> requests = builder.getRequests();

        try
        {
            List<ArtifactResult> results = resolveArtifacts( session, requests );

            updateNodesWithResolvedArtifacts( results );

            return results;
        }
        catch ( ArtifactResolutionException e )
        {
            updateNodesWithResolvedArtifacts( e.getResults() );

            throw e;
        }
    }

    private void updateNodesWithResolvedArtifacts( List<ArtifactResult> results )
    {
        for ( ArtifactResult result : results )
        {
            Artifact artifact = result.getArtifact();
            if ( artifact != null )
            {
                result.getRequest().getDependencyNode().setArtifact( artifact );
            }
        }
    }

    public List<ArtifactResult> resolveDependencies( RepositorySystemSession session, CollectRequest request,
                                                     DependencyFilter filter )
        throws DependencyCollectionException, ArtifactResolutionException
    {
        validateSession( session );
        CollectResult result = collectDependencies( session, request );
        return resolveDependencies( session, result.getRoot(), filter );
    }

    public InstallResult install( RepositorySystemSession session, InstallRequest request )
        throws InstallationException
    {
        validateSession( session );
        return installer.install( session, request );
    }

    public DeployResult deploy( RepositorySystemSession session, DeployRequest request )
        throws DeploymentException
    {
        validateSession( session );
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

    private void validateSession( RepositorySystemSession session )
    {
        if ( validated.contains( session ) )
        {
            return;
        }

        if ( session.getLocalRepositoryManager() == null )
        {
            invalidSession( "LocalRepositoryManager" );
        }
        if ( session.getLocalRepository() == null )
        {
            invalidSession( "LocalRepository" );
        }
        if ( session.getSystemProperties() == null )
        {
            invalidSession( "SystemProperties" );
        }
        if ( session.getUserProperties() == null )
        {
            invalidSession( "UserProperties" );
        }
        if ( session.getConfigProperties() == null )
        {
            invalidSession( "ConfigProperties" );
        }
        if ( session.getMirrorSelector() == null )
        {
            invalidSession( "MirrorSelector" );
        }
        if ( session.getProxySelector() == null )
        {
            invalidSession( "ProxySelector" );
        }
        if ( session.getAuthenticationSelector() == null )
        {
            invalidSession( "AuthenticationSelector" );
        }
        if ( session.getArtifactTypeRegistry() == null )
        {
            invalidSession( "ArtifactTypeRegistry" );
        }
        if ( session.getDependencyTraverser() == null )
        {
            invalidSession( "DependencyTraverser" );
        }
        if ( session.getDependencyManager() == null )
        {
            invalidSession( "DependencyManager" );
        }
        if ( session.getDependencySelector() == null )
        {
            invalidSession( "DependencySelector" );
        }
        if ( session.getDependencyGraphTransformer() == null )
        {
            invalidSession( "DependencyGraphTransformer" );
        }
        if ( session.getData() == null )
        {
            invalidSession( "Data" );
        }

        validated.add( session );
    }

    private void invalidSession( String name )
    {
        throw new IllegalArgumentException( "Invalid Session: " + name + " was not set." );
    }

}
