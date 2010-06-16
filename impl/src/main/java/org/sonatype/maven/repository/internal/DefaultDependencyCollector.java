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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.maven.repository.Artifact;
import org.sonatype.maven.repository.ArtifactDescriptorException;
import org.sonatype.maven.repository.ArtifactDescriptorRequest;
import org.sonatype.maven.repository.ArtifactDescriptorResult;
import org.sonatype.maven.repository.ArtifactRepository;
import org.sonatype.maven.repository.CollectRequest;
import org.sonatype.maven.repository.CollectResult;
import org.sonatype.maven.repository.Dependency;
import org.sonatype.maven.repository.DependencyCollectionException;
import org.sonatype.maven.repository.DependencyManagement;
import org.sonatype.maven.repository.DependencySelector;
import org.sonatype.maven.repository.DependencyGraphTransformer;
import org.sonatype.maven.repository.DependencyManager;
import org.sonatype.maven.repository.DependencyNode;
import org.sonatype.maven.repository.DependencyTraverser;
import org.sonatype.maven.repository.RemoteRepository;
import org.sonatype.maven.repository.RepositoryException;
import org.sonatype.maven.repository.RepositoryListener;
import org.sonatype.maven.repository.RepositorySession;
import org.sonatype.maven.repository.Version;
import org.sonatype.maven.repository.VersionRangeRequest;
import org.sonatype.maven.repository.VersionRangeResolutionException;
import org.sonatype.maven.repository.VersionRangeResult;
import org.sonatype.maven.repository.spi.ArtifactDescriptorReader;
import org.sonatype.maven.repository.spi.DependencyCollector;
import org.sonatype.maven.repository.spi.Logger;
import org.sonatype.maven.repository.spi.NullLogger;
import org.sonatype.maven.repository.spi.RemoteRepositoryManager;
import org.sonatype.maven.repository.spi.VersionRangeResolver;
import org.sonatype.maven.repository.util.DefaultRepositoryEvent;

/**
 * @author Benjamin Bentmann
 */
@Component( role = DependencyCollector.class )
public class DefaultDependencyCollector
    implements DependencyCollector
{

    @Requirement
    private Logger logger = NullLogger.INSTANCE;

    @Requirement
    private RemoteRepositoryManager remoteRepositoryManager;

    @Requirement
    private ArtifactDescriptorReader descriptorReader;

    @Requirement
    private VersionRangeResolver versionRangeResolver;

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

    public CollectResult collectDependencies( RepositorySession session, CollectRequest request )
        throws DependencyCollectionException
    {
        CollectResult result = new CollectResult( request );

        DependencySelector depSelector = session.getDependencySelector();
        DependencyManager depManager = session.getDependencyManager();
        DependencyTraverser depTraverser = session.getDependencyTraverser();

        Dependency root = request.getRoot();
        List<RemoteRepository> repositories = request.getRepositories();
        List<Dependency> dependencies = request.getDependencies();
        List<Dependency> managedDependencies = request.getManagedDependencies();

        DependencyNode node = null;
        if ( root != null )
        {
            node = new DependencyNode( root );
            node.setRequestedVersion( root.getArtifact().getVersion() );
            node.setRepositories( request.getRepositories() );
            node.setContext( request.getContext() );

            ArtifactDescriptorResult descriptorResult;
            try
            {
                ArtifactDescriptorRequest descriptorRequest = new ArtifactDescriptorRequest();
                descriptorRequest.setArtifact( root.getArtifact() );
                descriptorRequest.setRepositories( request.getRepositories() );
                descriptorRequest.setContext( request.getContext() );
                descriptorResult = descriptorReader.readArtifactDescriptor( session, descriptorRequest );
            }
            catch ( ArtifactDescriptorException e )
            {
                result.addException( e );
                throw new DependencyCollectionException( result );
            }

            repositories =
                remoteRepositoryManager.aggregateRepositories( session, repositories,
                                                               descriptorResult.getRepositories(), true );
            dependencies = mergeDeps( dependencies, descriptorResult.getDependencies() );
            managedDependencies = mergeDeps( managedDependencies, descriptorResult.getManagedDependencies() );

            node.setRelocations( descriptorResult.getRelocations() );
            node.setPropertes( descriptorResult.getProperties() );

            VersionRangeRequest versionRequest =
                new VersionRangeRequest( root.getArtifact(), request.getRepositories(), request.getContext() );
            try
            {
                VersionRangeResult versionResult = versionRangeResolver.resolveVersionRange( session, versionRequest );
                node.setVersionConstraint( versionResult.getVersionConstraint() );
                node.setVersion( versionResult.getVersions().get( 0 ) );
            }
            catch ( VersionRangeResolutionException e )
            {
                result.addException( e );
                throw new DependencyCollectionException( result );
            }

            artifactRelocated( session, descriptorResult );
        }
        else
        {
            node = new DependencyNode( null );
        }

        result.setRoot( node );

        boolean traverse = ( root == null ) || depTraverser.traverseDependency( null, root );

        if ( traverse )
        {
            process( session, result, node, dependencies, managedDependencies, repositories,
                     depSelector.deriveChildSelector( node ), depManager, depTraverser.deriveChildTraverser( node ) );
        }

        DependencyGraphTransformer transformer = session.getDependencyGraphTransformer();
        if ( transformer != null )
        {
            try
            {
                result.setRoot( transformer.transformGraph( node ) );
            }
            catch ( RepositoryException e )
            {
                result.addException( e );
            }
        }

        if ( !result.getExceptions().isEmpty() )
        {
            throw new DependencyCollectionException( result );
        }

        return result;
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
        return a.getGroupId() + ':' + a.getArtifactId() + ':' + a.getClassifier() + ':' + a.getType();
    }

    private void process( RepositorySession session, CollectResult result, DependencyNode node,
                          List<Dependency> dependencies, List<Dependency> managedDependencies,
                          List<RemoteRepository> repositories, DependencySelector depSelector,
                          DependencyManager depManager, DependencyTraverser depTraverser )
        throws DependencyCollectionException
    {
        // TODO: many dirty trees will have repetitive sub trees, optimize and don't reprocess what we already did

        for ( Dependency dependency : dependencies )
        {
            process( session, result, node, dependency, managedDependencies, repositories, depSelector, depManager,
                     depTraverser );
        }
    }

    private void process( RepositorySession session, CollectResult result, DependencyNode node, Dependency dependency,
                          List<Dependency> managedDependencies, List<RemoteRepository> repositories,
                          DependencySelector depSelector, DependencyManager depManager, DependencyTraverser depTraverser )
        throws DependencyCollectionException
    {
        process: while ( true )
        {
            if ( !depSelector.selectDependency( node, dependency ) )
            {
                return;
            }

            DependencyManagement depMngt = depManager.manageDependency( node, dependency );
            String premanagedVersion = null;
            String premanagedScope = null;

            if ( depMngt != null )
            {
                if ( depMngt.getVersion() != null )
                {
                    premanagedVersion = dependency.getArtifact().getVersion();
                    dependency.getArtifact().setVersion( depMngt.getVersion() );
                }
                if ( depMngt.getScope() != null )
                {
                    premanagedScope = dependency.getScope();
                    dependency.setScope( depMngt.getScope() );
                }
                if ( depMngt.getExclusions() != null )
                {
                    dependency.getExclusions().addAll( depMngt.getExclusions() );
                }
            }

            boolean system = dependency.getArtifact().getFile() != null;

            boolean traverse = !system && depTraverser.traverseDependency( node, dependency );

            VersionRangeResult rangeResult;
            try
            {
                VersionRangeRequest rangeRequest = new VersionRangeRequest();
                rangeRequest.setArtifact( dependency.getArtifact() );
                rangeRequest.setRepositories( repositories );
                rangeRequest.setContext( result.getRequest().getContext() );
                rangeResult = versionRangeResolver.resolveVersionRange( session, rangeRequest );

                if ( rangeResult.getVersions().isEmpty() )
                {
                    throw new VersionRangeResolutionException( rangeResult, "No versions available for "
                        + dependency.getArtifact() + " within specified range" );
                }
            }
            catch ( VersionRangeResolutionException e )
            {
                result.addException( e );
                return;
            }

            for ( Version version : rangeResult.getVersions() )
            {
                Dependency d = new Dependency();
                d.setArtifact( dependency.getArtifact().clone() );
                d.getArtifact().setVersion( version.toString() );
                d.setScope( dependency.getScope() );
                d.setOptional( dependency.isOptional() );
                d.getExclusions().addAll( dependency.getExclusions() );

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
                    descriptorRequest.setContext( result.getRequest().getContext() );
                    if ( system )
                    {
                        descriptorResult = new ArtifactDescriptorResult( descriptorRequest );
                        descriptorResult.setArtifact( d.getArtifact() );
                    }
                    else
                    {
                        descriptorResult = descriptorReader.readArtifactDescriptor( session, descriptorRequest );
                        d.setArtifact( descriptorResult.getArtifact() );
                    }
                }
                catch ( ArtifactDescriptorException e )
                {
                    result.addException( e );
                    continue;
                }

                if ( node.getDependency() == null )
                {
                    artifactRelocated( session, descriptorResult );
                }

                if ( findDuplicate( node, d.getArtifact() ) != null )
                {
                    continue;
                }

                if ( !descriptorResult.getRelocations().isEmpty() )
                {
                    dependency = d;
                    continue process;
                }

                DependencyNode child = node.addChild( d );
                child.setRelocations( descriptorResult.getRelocations() );
                child.setVersionConstraint( rangeResult.getVersionConstraint() );
                child.setVersion( version );
                child.setRequestedVersion( dependency.getArtifact().getVersion() );
                child.setPremanagedVersion( premanagedVersion );
                child.setPremanagedScope( premanagedScope );
                child.setRepositories( repos );
                child.setPropertes( descriptorResult.getProperties() );
                child.setContext( result.getRequest().getContext() );

                if ( traverse )
                {
                    process( session, result, child, descriptorResult.getDependencies(),
                             descriptorResult.getManagedDependencies(),
                             remoteRepositoryManager.aggregateRepositories( session, repositories,
                                                                            descriptorResult.getRepositories(), true ),
                             depSelector.deriveChildSelector( child ),
                             depManager.deriveChildManager( child, managedDependencies ),
                             depTraverser.deriveChildTraverser( child ) );
                }
            }
            
            break;
        }
    }

    private DependencyNode findDuplicate( DependencyNode node, Artifact artifact )
    {
        for ( DependencyNode n = node; n != null; n = n.getParent() )
        {
            Dependency dependency = n.getDependency();
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
            if ( !a.getType().equals( artifact.getType() ) )
            {
                continue;
            }
            if ( !a.getClassifier().equals( artifact.getClassifier() ) )
            {
                continue;
            }

            return n;
        }

        return null;
    }

    private void artifactRelocated( RepositorySession session, ArtifactDescriptorResult result )
    {
        RepositoryListener listener = session.getRepositoryListener();
        if ( listener != null && !result.getRelocations().isEmpty() )
        {
            DefaultRepositoryEvent event =
                new DefaultRepositoryEvent( session, result.getArtifact(), result.getRelocations().get( 0 ) );
            listener.artifactRelocated( event );
        }
    }

}
