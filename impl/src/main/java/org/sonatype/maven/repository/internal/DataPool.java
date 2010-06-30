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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.sonatype.maven.repository.Artifact;
import org.sonatype.maven.repository.ArtifactDescriptorRequest;
import org.sonatype.maven.repository.ArtifactDescriptorResult;
import org.sonatype.maven.repository.Dependency;
import org.sonatype.maven.repository.DependencyInfo;
import org.sonatype.maven.repository.DependencyNode;
import org.sonatype.maven.repository.RemoteRepository;
import org.sonatype.maven.repository.RepositoryCache;

/**
 * @author Benjamin Bentmann
 */
class DataPool
{

    private static final String ARTIFACT_POOL = DataPool.class.getName() + "$Artifact";

    private static final String DEPENDENCY_POOL = DataPool.class.getName() + "$Dependency";

    private ObjectPool<Artifact> artifacts;

    private ObjectPool<Dependency> dependencies;

    private Map<Object, Descriptor> descriptors = new WeakHashMap<Object, Descriptor>();

    private Map<Object, List<RemoteRepository>> repositories = new HashMap<Object, List<RemoteRepository>>();

    private Map<Object, DependencyNode> nodes = new HashMap<Object, DependencyNode>();

    private Map<Object, List<RemoteRepository>> repos = new HashMap<Object, List<RemoteRepository>>();

    public DataPool( RepositoryCache cache )
    {
        if ( cache != null )
        {
            artifacts = (ObjectPool<Artifact>) cache.get( ARTIFACT_POOL );
            dependencies = (ObjectPool<Dependency>) cache.get( DEPENDENCY_POOL );
        }

        if ( artifacts == null )
        {
            artifacts = new ObjectPool<Artifact>();
            if ( cache != null )
            {
                cache.put( ARTIFACT_POOL, artifacts );
            }
        }

        if ( dependencies == null )
        {
            dependencies = new ObjectPool<Dependency>();
            if ( cache != null )
            {
                cache.put( DEPENDENCY_POOL, dependencies );
            }
        }
    }

    public Artifact intern( Artifact artifact )
    {
        return artifacts.intern( artifact );
    }

    public Dependency intern( Dependency dependency )
    {
        return dependencies.intern( dependency );
    }

    public List<RemoteRepository> intern( List<RemoteRepository> repositories )
    {
        List<RemoteRepository> interned = this.repositories.get( repositories );
        if ( interned != null )
        {
            return interned;
        }
        this.repositories.put( repositories, repositories );
        return repositories;
    }

    public Object toKey( ArtifactDescriptorRequest request )
    {
        return request.getArtifact();
    }

    public ArtifactDescriptorResult getDescriptor( Object key, ArtifactDescriptorRequest request )
    {
        Descriptor descriptor = descriptors.get( key );
        if ( descriptor != null )
        {
            return descriptor.toResult( request );
        }
        return null;
    }

    public void putDescriptor( Object key, ArtifactDescriptorResult result )
    {
        descriptors.put( key, new Descriptor( result ) );
    }

    public Object getNodeKey( DependencyInfo info )
    {
        return new InfoKey( info );
    }

    public DependencyNode getNode( Object key )
    {
        return nodes.get( key );
    }

    public void putNode( Object key, DependencyNode node )
    {
        nodes.put( key, node );
    }

    public List<RemoteRepository> getRepositories( Object key )
    {
        return repos.get( key );
    }

    public void putRepositories( Object key, List<RemoteRepository> repositories )
    {
        if ( !this.repos.containsKey( key ) )
        {
            this.repos.put( key, repositories );
        }
    }

    static class InfoKey
    {
        private final DependencyInfo info;

        private final int hashCode;

        public InfoKey( DependencyInfo info )
        {
            this.info = info;

            int hash = info.getDependency().getArtifact().hashCode();
            hash = hash * 31 + info.getDependency().getScope().hashCode();
            hash = hash * 31 + info.getVersionConstraint().hashCode();
            hashCode = hash;
        }

        @Override
        public boolean equals( Object obj )
        {
            if ( obj == this )
            {
                return true;
            }
            else if ( obj == null || !getClass().equals( obj.getClass() ) )
            {
                return false;
            }

            InfoKey that = (InfoKey) obj;
            return eq( info.getDependency().getArtifact(), that.info.getDependency().getArtifact() )
                && info.getDependency().getScope().equals( that.info.getDependency().getScope() )
                && info.getDependency().isOptional() == that.info.getDependency().isOptional()
                && info.getDependency().getExclusions().equals( that.info.getDependency().getExclusions() )
                && eq( info.getVersionConstraint(), that.info.getVersionConstraint() )
                && eq( info.getPremanagedScope(), that.info.getPremanagedScope() )
                && eq( info.getPremanagedVersion(), that.info.getPremanagedVersion() )
                && info.getRelocations().equals( that.info.getRelocations() );
        }

        private static <T> boolean eq( T s1, T s2 )
        {
            return s1 != null ? s1.equals( s2 ) : s2 == null;
        }

        @Override
        public int hashCode()
        {
            return hashCode;
        }

    }

    static class Descriptor
    {

        final Artifact artifact;

        final Map<String, Object> properties;

        final List<Artifact> relocations;

        final List<RemoteRepository> repositories;

        final List<Dependency> dependencies;

        final List<Dependency> managedDependencies;

        public Descriptor( ArtifactDescriptorResult result )
        {
            artifact = result.getArtifact();
            properties = result.getProperties();
            relocations = result.getRelocations();
            dependencies = result.getDependencies();
            managedDependencies = result.getManagedDependencies();
            repositories = clone( result.getRepositories() );
        }

        public ArtifactDescriptorResult toResult( ArtifactDescriptorRequest request )
        {
            ArtifactDescriptorResult result = new ArtifactDescriptorResult( request );
            result.setArtifact( artifact );
            result.setProperties( properties );
            result.setRelocations( relocations );
            result.setDependencies( dependencies );
            result.setManagedDependencies( dependencies );
            result.setRepositories( clone( repositories ) );
            return result;
        }

        private static List<RemoteRepository> clone( List<RemoteRepository> repositories )
        {
            List<RemoteRepository> clones = new ArrayList<RemoteRepository>( repositories.size() );
            for ( RemoteRepository repository : repositories )
            {
                RemoteRepository clone = new RemoteRepository( repository );
                clone.setMirroredRepositories( new ArrayList<RemoteRepository>( repository.getMirroredRepositories() ) );
                clones.add( clone );
            }
            return clones;
        }
    }

}
