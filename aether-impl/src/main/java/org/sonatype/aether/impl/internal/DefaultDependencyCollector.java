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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.aether.Artifact;
import org.sonatype.aether.ArtifactDescriptorException;
import org.sonatype.aether.ArtifactDescriptorRequest;
import org.sonatype.aether.ArtifactDescriptorResult;
import org.sonatype.aether.ArtifactRepository;
import org.sonatype.aether.CollectRequest;
import org.sonatype.aether.CollectResult;
import org.sonatype.aether.Dependency;
import org.sonatype.aether.DependencyCollectionException;
import org.sonatype.aether.DependencyGraphTransformer;
import org.sonatype.aether.DependencyManagement;
import org.sonatype.aether.DependencyManager;
import org.sonatype.aether.DependencySelector;
import org.sonatype.aether.DependencyTraverser;
import org.sonatype.aether.RemoteRepository;
import org.sonatype.aether.RepositoryException;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.Version;
import org.sonatype.aether.VersionRangeRequest;
import org.sonatype.aether.VersionRangeResolutionException;
import org.sonatype.aether.VersionRangeResult;
import org.sonatype.aether.impl.ArtifactDescriptorReader;
import org.sonatype.aether.impl.DependencyCollector;
import org.sonatype.aether.impl.RemoteRepositoryManager;
import org.sonatype.aether.impl.VersionRangeResolver;
import org.sonatype.aether.util.ArtifactProperties;
import org.sonatype.aether.util.DefaultRepositorySystemSession;
import org.sonatype.aether.spi.locator.Service;
import org.sonatype.aether.spi.locator.ServiceLocator;
import org.sonatype.aether.spi.log.Logger;
import org.sonatype.aether.spi.log.NullLogger;

/**
 * @author Benjamin Bentmann
 */
@Component( role = DependencyCollector.class )
public class DefaultDependencyCollector
    implements DependencyCollector, Service
{

    @Requirement
    private Logger logger = NullLogger.INSTANCE;

    @Requirement
    private RemoteRepositoryManager remoteRepositoryManager;

    @Requirement
    private ArtifactDescriptorReader descriptorReader;

    @Requirement
    private VersionRangeResolver versionRangeResolver;

    public void initService( ServiceLocator locator )
    {
        setLogger( locator.getService( Logger.class ) );
        setRemoteRepositoryManager( locator.getService( RemoteRepositoryManager.class ) );
        setArtifactDescriptorReader( locator.getService( ArtifactDescriptorReader.class ) );
        setVersionRangeResolver( locator.getService( VersionRangeResolver.class ) );
    }

    public DefaultDependencyCollector setLogger( Logger logger )
    {
        this.logger = ( logger != null ) ? logger : NullLogger.INSTANCE;
        return this;
    }

    public DefaultDependencyCollector setRemoteRepositoryManager( RemoteRepositoryManager remoteRepositoryManager )
    {
        if ( remoteRepositoryManager == null )
        {
            throw new IllegalArgumentException( "remote repository manager has not been specified" );
        }
        this.remoteRepositoryManager = remoteRepositoryManager;
        return this;
    }

    public DefaultDependencyCollector setArtifactDescriptorReader( ArtifactDescriptorReader artifactDescriptorReader )
    {
        if ( artifactDescriptorReader == null )
        {
            throw new IllegalArgumentException( "artifact descriptor reader has not been specified" );
        }
        this.descriptorReader = artifactDescriptorReader;
        return this;
    }

    public DefaultDependencyCollector setVersionRangeResolver( VersionRangeResolver versionRangeResolver )
    {
        if ( versionRangeResolver == null )
        {
            throw new IllegalArgumentException( "version range resolver has not been specified" );
        }
        this.versionRangeResolver = versionRangeResolver;
        return this;
    }

    public CollectResult collectDependencies( RepositorySystemSession session, CollectRequest request )
        throws DependencyCollectionException
    {
        session = optimizeSession( session );

        CollectResult result = new CollectResult( request );

        DependencySelector depSelector = session.getDependencySelector();
        DependencyManager depManager = session.getDependencyManager();
        DependencyTraverser depTraverser = session.getDependencyTraverser();

        Dependency root = request.getRoot();
        List<RemoteRepository> repositories = request.getRepositories();
        List<Dependency> dependencies = request.getDependencies();
        List<Dependency> managedDependencies = request.getManagedDependencies();

        GraphEdge edge = null;
        if ( root != null )
        {
            ArtifactDescriptorResult descriptorResult;
            try
            {
                ArtifactDescriptorRequest descriptorRequest = new ArtifactDescriptorRequest();
                descriptorRequest.setArtifact( root.getArtifact() );
                descriptorRequest.setRepositories( request.getRepositories() );
                descriptorRequest.setRequestContext( request.getRequestContext() );
                if ( isLackingDescriptor( root.getArtifact() ) )
                {
                    descriptorResult = new ArtifactDescriptorResult( descriptorRequest );
                }
                else
                {
                    descriptorResult = descriptorReader.readArtifactDescriptor( session, descriptorRequest );
                }
            }
            catch ( ArtifactDescriptorException e )
            {
                result.addException( e );
                throw new DependencyCollectionException( result );
            }

            root = root.setArtifact( descriptorResult.getArtifact() );

            repositories =
                remoteRepositoryManager.aggregateRepositories( session, repositories,
                                                               descriptorResult.getRepositories(), true );
            dependencies = mergeDeps( dependencies, descriptorResult.getDependencies() );
            managedDependencies = mergeDeps( managedDependencies, descriptorResult.getManagedDependencies() );

            GraphNode node = new GraphNode();
            node.setAliases( descriptorResult.getAliases() );
            node.setRepositories( request.getRepositories() );

            edge = new GraphEdge( null, node );
            edge.setDependency( root );
            edge.setRequestContext( request.getRequestContext() );
            edge.setRelocations( descriptorResult.getRelocations() );

            VersionRangeRequest versionRequest =
                new VersionRangeRequest( root.getArtifact(), request.getRepositories(), request.getRequestContext() );
            try
            {
                VersionRangeResult versionResult = versionRangeResolver.resolveVersionRange( session, versionRequest );
                edge.setVersionConstraint( versionResult.getVersionConstraint() );
                edge.setVersion( versionResult.getVersions().get( 0 ) );
            }
            catch ( VersionRangeResolutionException e )
            {
                result.addException( e );
                throw new DependencyCollectionException( result );
            }
        }
        else
        {
            edge = new GraphEdge( null, new GraphNode() );
        }

        result.setRoot( edge );

        boolean traverse = ( root == null ) || depTraverser.traverseDependency( root );

        if ( traverse && !dependencies.isEmpty() )
        {
            DataPool pool = new DataPool( session );

            LinkedList<GraphEdge> edges = new LinkedList<GraphEdge>();
            edges.addFirst( edge );

            DefaultDependencyCollectionContext context =
                new DefaultDependencyCollectionContext( session, root, managedDependencies );

            process( session, result, edges, dependencies, repositories, depSelector.deriveChildSelector( context ),
                     depManager.deriveChildManager( context ), depTraverser.deriveChildTraverser( context ), pool );
        }

        DependencyGraphTransformer transformer = session.getDependencyGraphTransformer();
        try
        {
            DefaultDependencyGraphTransformationContext context =
                new DefaultDependencyGraphTransformationContext( session );
            result.setRoot( transformer.transformGraph( edge, context ) );
        }
        catch ( RepositoryException e )
        {
            result.addException( e );
        }

        if ( !result.getExceptions().isEmpty() )
        {
            throw new DependencyCollectionException( result );
        }

        return result;
    }

    private RepositorySystemSession optimizeSession( RepositorySystemSession session )
    {
        DefaultRepositorySystemSession optimized = new DefaultRepositorySystemSession( session );
        optimized.setArtifactTypeRegistry( CachingArtifactTypeRegistry.newInstance( session ) );
        return optimized;
    }

    private List<Dependency> mergeDeps( List<Dependency> dominant, List<Dependency> recessive )
    {
        List<Dependency> result;
        if ( dominant == null || dominant.isEmpty() )
        {
            result = recessive;
        }
        else if ( recessive == null || recessive.isEmpty() )
        {
            result = dominant;
        }
        else
        {
            result = new ArrayList<Dependency>( dominant.size() + recessive.size() );
            Collection<String> ids = new HashSet<String>();
            for ( Dependency dependency : dominant )
            {
                ids.add( getId( dependency.getArtifact() ) );
                result.add( dependency );
            }
            for ( Dependency dependency : recessive )
            {
                if ( !ids.contains( getId( dependency.getArtifact() ) ) )
                {
                    result.add( dependency );
                }
            }
        }
        return result;
    }

    private String getId( Artifact a )
    {
        return a.getGroupId() + ':' + a.getArtifactId() + ':' + a.getClassifier() + ':' + a.getExtension();
    }

    private void process( RepositorySystemSession session, CollectResult result, LinkedList<GraphEdge> edges,
                          List<Dependency> dependencies, List<RemoteRepository> repositories,
                          DependencySelector depSelector, DependencyManager depManager,
                          DependencyTraverser depTraverser, DataPool pool )
        throws DependencyCollectionException
    {
        nextDependency: for ( Dependency dependency : dependencies )
        {
            boolean disableVersionManagement = false;

            List<Artifact> relocations = Collections.emptyList();

            thisDependency: while ( true )
            {
                if ( !depSelector.selectDependency( dependency ) )
                {
                    continue nextDependency;
                }

                DependencyManagement depMngt = depManager.manageDependency( dependency );
                String premanagedVersion = null;
                String premanagedScope = null;

                if ( depMngt != null )
                {
                    if ( depMngt.getVersion() != null && !disableVersionManagement )
                    {
                        Artifact artifact = dependency.getArtifact();
                        premanagedVersion = artifact.getVersion();
                        dependency = dependency.setArtifact( artifact.setVersion( depMngt.getVersion() ) );
                    }
                    if ( depMngt.getScope() != null )
                    {
                        premanagedScope = dependency.getScope();
                        dependency = dependency.setScope( depMngt.getScope() );
                    }
                    if ( depMngt.getExclusions() != null )
                    {
                        dependency = dependency.setExclusions( depMngt.getExclusions() );
                    }
                }
                disableVersionManagement = false;

                boolean noDescriptor = isLackingDescriptor( dependency.getArtifact() );

                boolean traverse = !noDescriptor && depTraverser.traverseDependency( dependency );

                VersionRangeResult rangeResult;
                try
                {
                    VersionRangeRequest rangeRequest = new VersionRangeRequest();
                    rangeRequest.setArtifact( dependency.getArtifact() );
                    rangeRequest.setRepositories( repositories );
                    rangeRequest.setRequestContext( result.getRequest().getRequestContext() );

                    Object key = pool.toKey( rangeRequest );
                    rangeResult = pool.getConstraint( key, rangeRequest );
                    if ( rangeResult == null )
                    {
                        rangeResult = versionRangeResolver.resolveVersionRange( session, rangeRequest );
                        pool.putConstraint( key, rangeResult );
                    }

                    if ( rangeResult.getVersions().isEmpty() )
                    {
                        throw new VersionRangeResolutionException( rangeResult, "No versions available for "
                            + dependency.getArtifact() + " within specified range" );
                    }
                }
                catch ( VersionRangeResolutionException e )
                {
                    result.addException( e );
                    continue nextDependency;
                }

                List<Version> versions = rangeResult.getVersions();
                for ( Version version : versions )
                {
                    Artifact originalArtifact = dependency.getArtifact().setVersion( version.toString() );
                    Dependency originalDependency = dependency.setArtifact( originalArtifact );
                    Dependency d = originalDependency;

                    List<RemoteRepository> repos = null;
                    ArtifactRepository repo = rangeResult.getRepository( version );
                    if ( repo instanceof RemoteRepository )
                    {
                        repos = Collections.singletonList( (RemoteRepository) repo );
                    }
                    else if ( repo == null )
                    {
                        repos = repositories;
                    }

                    ArtifactDescriptorResult descriptorResult;
                    try
                    {
                        ArtifactDescriptorRequest descriptorRequest = new ArtifactDescriptorRequest();
                        descriptorRequest.setArtifact( d.getArtifact() );
                        descriptorRequest.setRepositories( repos );
                        descriptorRequest.setRequestContext( result.getRequest().getRequestContext() );

                        if ( noDescriptor )
                        {
                            descriptorResult = new ArtifactDescriptorResult( descriptorRequest );
                        }
                        else
                        {
                            Object key = pool.toKey( descriptorRequest );
                            descriptorResult = pool.getDescriptor( key, descriptorRequest );
                            if ( descriptorResult == null )
                            {
                                descriptorResult = descriptorReader.readArtifactDescriptor( session, descriptorRequest );
                                pool.putDescriptor( key, descriptorResult );
                            }
                        }
                    }
                    catch ( ArtifactDescriptorException e )
                    {
                        result.addException( e );
                        continue;
                    }

                    d = d.setArtifact( descriptorResult.getArtifact() );

                    if ( findDuplicate( edges, d.getArtifact() ) != null )
                    {
                        continue;
                    }

                    if ( !descriptorResult.getRelocations().isEmpty() )
                    {
                        relocations = descriptorResult.getRelocations();

                        disableVersionManagement =
                            originalArtifact.getGroupId().equals( d.getArtifact().getGroupId() )
                                && originalArtifact.getArtifactId().equals( d.getArtifact().getArtifactId() );

                        dependency = d;
                        continue thisDependency;
                    }

                    d = pool.intern( d.setArtifact( pool.intern( d.getArtifact() ) ) );

                    DependencySelector childSelector = null;
                    DependencyManager childManager = null;
                    DependencyTraverser childTraverser = null;
                    List<RemoteRepository> childRepos = null;
                    Object key = null;

                    boolean recurse = traverse && !descriptorResult.getDependencies().isEmpty();
                    if ( recurse )
                    {
                        DefaultDependencyCollectionContext context =
                            new DefaultDependencyCollectionContext( session, d,
                                                                    descriptorResult.getManagedDependencies() );

                        childSelector = depSelector.deriveChildSelector( context );
                        childManager = depManager.deriveChildManager( context );
                        childTraverser = depTraverser.deriveChildTraverser( context );

                        childRepos =
                            remoteRepositoryManager.aggregateRepositories( session, repositories,
                                                                           descriptorResult.getRepositories(), true );

                        key = pool.toKey( d.getArtifact(), childRepos, childSelector, childManager, childTraverser );
                    }
                    else
                    {
                        key = pool.toKey( d.getArtifact(), repositories );
                    }

                    GraphNode child = pool.getNode( key );
                    if ( child == null )
                    {
                        child = new GraphNode();
                        child.setAliases( descriptorResult.getAliases() );
                        child.setRepositories( repos );
                        pool.putNode( key, child );
                    }
                    else
                    {
                        recurse = false;

                        if ( repos.size() < child.getRepositories().size() )
                        {
                            child.setRepositories( repos );
                        }
                    }

                    GraphNode node = edges.getFirst().getTarget();

                    GraphEdge edge = new GraphEdge( node, child );
                    edge.setDependency( originalDependency );
                    edge.setScope( originalDependency.getScope() );
                    edge.setPremanagedScope( premanagedScope );
                    edge.setPremanagedVersion( premanagedVersion );
                    edge.setRelocations( relocations );
                    edge.setVersionConstraint( rangeResult.getVersionConstraint() );
                    edge.setVersion( version );
                    edge.setRequestContext( result.getRequest().getRequestContext() );

                    node.getOutgoingEdges().add( edge );

                    if ( recurse )
                    {
                        edges.addFirst( edge );

                        process( session, result, edges, descriptorResult.getDependencies(), childRepos, childSelector,
                                 childManager, childTraverser, pool );

                        edges.removeFirst();
                    }
                }

                break;
            }
        }
    }

    private GraphEdge findDuplicate( List<GraphEdge> edges, Artifact artifact )
    {
        for ( GraphEdge edge : edges )
        {
            Dependency dependency = edge.getDependency();
            if ( dependency == null )
            {
                break;
            }

            Artifact a = dependency.getArtifact();
            if ( !a.getArtifactId().equals( artifact.getArtifactId() ) )
            {
                continue;
            }
            if ( !a.getGroupId().equals( artifact.getGroupId() ) )
            {
                continue;
            }
            if ( !a.getBaseVersion().equals( artifact.getBaseVersion() ) )
            {
                continue;
            }
            if ( !a.getExtension().equals( artifact.getExtension() ) )
            {
                continue;
            }
            if ( !a.getClassifier().equals( artifact.getClassifier() ) )
            {
                continue;
            }

            return edge;
        }

        return null;
    }

    private boolean isLackingDescriptor( Artifact artifact )
    {
        return Boolean.parseBoolean( artifact.getProperty( ArtifactProperties.LACKS_DESCRIPTOR, "" ) );
    }

}
