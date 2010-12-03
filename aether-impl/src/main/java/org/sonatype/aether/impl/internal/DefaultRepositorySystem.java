package org.sonatype.aether.impl.internal;

/*******************************************************************************
 * Copyright (c) 2010 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

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
import org.sonatype.aether.repository.NoLocalRepositoryManagerException;
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
import org.sonatype.aether.spi.localrepo.LocalRepositoryManagerFactory;
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

    private static final Comparator<LocalRepositoryManagerFactory> COMPARATOR =
        new Comparator<LocalRepositoryManagerFactory>()
        {

            public int compare( LocalRepositoryManagerFactory o1, LocalRepositoryManagerFactory o2 )
            {
                return o2.getPriority() - o1.getPriority();
            }

        };

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

    @Requirement( role = LocalRepositoryManagerFactory.class )
    private List<LocalRepositoryManagerFactory> managerFactories = new ArrayList<LocalRepositoryManagerFactory>();

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
        setLocalRepositoryManagerFactories( locator.getServices( LocalRepositoryManagerFactory.class ) );
    }

    /**
     * Set the factories to search for a {@link LocalRepositoryManager}.
     * 
     * @param factories The factories to use. If {@code null} or empty, use default implementation. See
     *            {@link #newLocalRepositoryManager(LocalRepository)}.
     * @return This instance, for chaining.
     * @see #newLocalRepositoryManager(LocalRepository)
     */
    public DefaultRepositorySystem setLocalRepositoryManagerFactories( List<LocalRepositoryManagerFactory> factories )
    {
        if ( factories == null )
        {
            managerFactories = new ArrayList<LocalRepositoryManagerFactory>( 2 );
        }
        else
        {
            managerFactories = factories;
        }
        return this;
    }

    public DefaultRepositorySystem addLocalRepositoryManagerFactory( LocalRepositoryManagerFactory factory )
    {
        if ( factory == null )
        {
            throw new IllegalArgumentException( "Local repository manager factory has not been specified." );
        }
        managerFactories.add( factory );
        return this;
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

    /**
     * Return a new local repository manager for the specified local repository. This method will either use
     * {@link LocalRepositoryManagerFactory}s to dynamically load the appropriate managers, or fallback to the default
     * implementations if no factories are configured.
     * 
     * @see LocalRepositoryManagerFactory#newInstance(LocalRepository)
     * @see #addLocalRepositoryManagerFactory(LocalRepositoryManagerFactory)
     * @see #setLocalRepositoryManagerFactories(List)
     * @see #initService(ServiceLocator)
     */
    public LocalRepositoryManager newLocalRepositoryManager( LocalRepository localRepository )
    {
        List<LocalRepositoryManagerFactory> factories = new ArrayList<LocalRepositoryManagerFactory>( managerFactories );
        Collections.sort( factories, COMPARATOR );
        
        for ( LocalRepositoryManagerFactory factory : factories )
        {
            try
            {
                LocalRepositoryManager manager = factory.newInstance( localRepository );
        
                if ( logger.isDebugEnabled() )
                {
                    StringBuilder buffer = new StringBuilder( 256 );
                    buffer.append( "Using manager " ).append( manager.getClass().getSimpleName() );
                    buffer.append( " with priority " ).append( factory.getPriority() );
                    buffer.append( " for " ).append( localRepository.getBasedir() );
        
                    logger.debug( buffer.toString() );
                }
        
                return manager;
            }
            catch ( NoLocalRepositoryManagerException e )
            {
                // continue and try next factory
            }
        }
        
        StringBuilder buffer = new StringBuilder( 256 );
        buffer.append( "No manager available for local repository " );
        buffer.append( " (" ).append( localRepository.getBasedir() );
        buffer.append( ") of type " ).append( localRepository.getContentType() );
        buffer.append( " using the available factories " );
        for ( ListIterator<LocalRepositoryManagerFactory> it = factories.listIterator(); it.hasNext(); )
        {
            LocalRepositoryManagerFactory factory = it.next();
            buffer.append( factory.getClass().getSimpleName() );
            if ( it.hasNext() )
            {
                buffer.append( ", " );
            }
        }
        
        throw new IllegalArgumentException( buffer.toString(), new NoLocalRepositoryManagerException( localRepository ) );
    }

    private LocalRepositoryManager lookupManager( LocalRepository localRepository )
    {
        List<LocalRepositoryManagerFactory> factories = new ArrayList<LocalRepositoryManagerFactory>( managerFactories );
        Collections.sort( factories, COMPARATOR );

        for ( LocalRepositoryManagerFactory factory : factories )
        {
            try
            {
                LocalRepositoryManager manager = factory.newInstance( localRepository );

                if ( logger.isDebugEnabled() )
                {
                    StringBuilder buffer = new StringBuilder( 256 );
                    buffer.append( "Using manager " ).append( manager.getClass().getSimpleName() );
                    buffer.append( " with priority " ).append( factory.getPriority() );
                    buffer.append( " for " ).append( localRepository.getBasedir() );

                    logger.debug( buffer.toString() );
                }

                return manager;
            }
            catch ( NoLocalRepositoryManagerException e )
            {
                // continue and try next factory
            }
        }

        StringBuilder buffer = new StringBuilder( 256 );
        buffer.append( "No manager available for local repository " );
        buffer.append( " (" ).append( localRepository.getBasedir() );
        buffer.append( ") of type " ).append( localRepository.getContentType() );
        buffer.append( " using the available factories " );
        for ( ListIterator<LocalRepositoryManagerFactory> it = factories.listIterator(); it.hasNext(); )
        {
            LocalRepositoryManagerFactory factory = it.next();
            buffer.append( factory.getClass().getSimpleName() );
            if ( it.hasNext() )
            {
                buffer.append( ", " );
            }
        }

        throw new IllegalArgumentException( buffer.toString(), new NoLocalRepositoryManagerException( localRepository ) );
    }

    private void validateSession( RepositorySystemSession session )
    {
        if ( session == null )
        {
            throw new IllegalArgumentException( "Invalid repository system session: the session may not be null." );
        }
        if ( session.getLocalRepositoryManager() == null )
        {
            invalidSession( "LocalRepositoryManager" );
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
    }

    private void invalidSession( String name )
    {
        throw new IllegalArgumentException( "Invalid repository system session: " + name + " is not set." );
    }

}
