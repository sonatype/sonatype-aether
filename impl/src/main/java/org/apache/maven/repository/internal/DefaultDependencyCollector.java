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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.apache.maven.repository.Artifact;
import org.apache.maven.repository.ArtifactDescriptorException;
import org.apache.maven.repository.ArtifactDescriptorRequest;
import org.apache.maven.repository.ArtifactDescriptorResult;
import org.apache.maven.repository.ArtifactRepository;
import org.apache.maven.repository.CollectRequest;
import org.apache.maven.repository.CollectResult;
import org.apache.maven.repository.Dependency;
import org.apache.maven.repository.DependencyCollectionException;
import org.apache.maven.repository.DependencyFilter;
import org.apache.maven.repository.DependencyGraphTransformer;
import org.apache.maven.repository.DependencyManager;
import org.apache.maven.repository.DependencyNode;
import org.apache.maven.repository.DependencyTraverser;
import org.apache.maven.repository.RemoteRepository;
import org.apache.maven.repository.RepositorySession;
import org.apache.maven.repository.RepositoryException;
import org.apache.maven.repository.VersionRangeRequest;
import org.apache.maven.repository.VersionRangeResolutionException;
import org.apache.maven.repository.VersionRangeResult;
import org.apache.maven.repository.spi.ArtifactDescriptorReader;
import org.apache.maven.repository.spi.DependencyCollector;
import org.apache.maven.repository.spi.Logger;
import org.apache.maven.repository.spi.NullLogger;
import org.apache.maven.repository.spi.RemoteRepositoryManager;
import org.apache.maven.repository.spi.VersionRangeResolver;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

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

        DependencyFilter depFilter = session.getDependencyFilter();
        DependencyManager depManager = session.getDependencyManager();
        DependencyTraverser depTraverser = session.getDependencyTraverser();

        Dependency root = request.getRoot();
        List<RemoteRepository> repositories = request.getRepositories();
        List<Dependency> dependencies = request.getDependencies();
        List<Dependency> managedDependencies = request.getManagedDependencies();

        DependencyNode node = new DependencyNode( null, null );
        if ( root != null )
        {
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
                                                               descriptorResult.getRepositories() );
            dependencies = mergeDeps( dependencies, descriptorResult.getDependencies() );
            managedDependencies = mergeDeps( managedDependencies, descriptorResult.getManagedDependencies() );

            node = new DependencyNode( root, null );
            node.setRelocations( descriptorResult.getRelocations() );
            node.setRequestedVersion( root.getArtifact().getVersion() );
            node.setRepositories( request.getRepositories() );
            node.setPropertes( descriptorResult.getProperties() );
            node.setContext( request.getContext() );
        }
        else
        {
            node = new DependencyNode( null, null );
        }

        result.setRoot( node );

        boolean traverse = ( root == null ) || depTraverser.accept( null, root );

        if ( traverse )
        {
            process( session, result, node, dependencies, managedDependencies, repositories,
                     depFilter.deriveChildFilter( node ), depManager, depTraverser.deriveChildTraverser( node ) );
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
                throw new DependencyCollectionException( result );
            }
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
                          List<RemoteRepository> repositories, DependencyFilter depFilter,
                          DependencyManager depManager, DependencyTraverser depTraverser )
        throws DependencyCollectionException
    {
        // TODO: many dirty trees will have repetitive sub trees, optimize and don't reprocess what we already did

        for ( Dependency dependency : dependencies )
        {
            if ( !depFilter.accept( node, dependency ) )
            {
                continue;
            }

            depManager.manageDependency( node, dependency );

            boolean system = dependency.getArtifact().getFile() != null;

            boolean traverse = !system && depTraverser.accept( node, dependency );

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
                continue;
            }

            for ( String version : rangeResult.getVersions() )
            {
                Dependency d = new Dependency( dependency );
                d.setArtifact( d.getArtifact().clone() );
                d.getArtifact().setVersion( version );

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

                if ( !descriptorResult.getRelocations().isEmpty() && !depFilter.accept( node, d ) )
                {
                    continue;
                }

                if ( findDuplicate( node, d.getArtifact() ) != null )
                {
                    continue;
                }

                DependencyNode child = node.addChild( d );
                child.setRelocations( descriptorResult.getRelocations() );
                child.setRequestedVersion( dependency.getArtifact().getVersion() );
                child.setRepositories( repos );
                child.setPropertes( descriptorResult.getProperties() );
                child.setContext( result.getRequest().getContext() );

                if ( traverse )
                {
                    process( session, result, child, descriptorResult.getDependencies(),
                             descriptorResult.getManagedDependencies(),
                             remoteRepositoryManager.aggregateRepositories( session, repositories,
                                                                            descriptorResult.getRepositories() ),
                             depFilter.deriveChildFilter( child ),
                             depManager.deriveChildManager( child, managedDependencies ),
                             depTraverser.deriveChildTraverser( child ) );
                }
            }
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

}
