package org.sonatype.aether.util.graph.transformer;

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
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.sonatype.aether.DependencyGraphTransformationContext;
import org.sonatype.aether.DependencyGraphTransformer;
import org.sonatype.aether.DependencyNode;
import org.sonatype.aether.RepositoryException;
import org.sonatype.aether.UnsolvableVersionConflictException;
import org.sonatype.aether.Version;
import org.sonatype.aether.VersionConstraint;

/**
 * A dependency graph transformer that resolves version conflicts using the nearest-wins strategy. For a given set of
 * conflicting nodes, one node will be chosen as the winner and the other nodes are removed from the dependency graph.
 * Note: This transformer assumes conflict groups have already been marked by a previous graph transformer like
 * {@link ConflictMarker}.
 * 
 * @author Benjamin Bentmann
 */
public class NearestVersionConflictResolver
    implements DependencyGraphTransformer
{

    public DependencyNode transformGraph( DependencyNode node, DependencyGraphTransformationContext context )
        throws RepositoryException
    {
        Map<?, ?> conflictIds = (Map<?, ?>) context.get( TransformationContextKeys.CONFLICT_IDS );
        if ( conflictIds == null )
        {
            throw new RepositoryException( "conflict groups have not been identified" );
        }

        Map<Object, ConflictId> ids = new LinkedHashMap<Object, ConflictId>();

        {
            Set<ConflictId> parents = new HashSet<ConflictId>( 128 );

            ConflictId id = null;
            Object key = conflictIds.get( node );
            if ( key != null )
            {
                id = new ConflictId( key );
                ids.put( key, id );
                parents.add( id );
            }

            Map<DependencyNode, Object> visited = new IdentityHashMap<DependencyNode, Object>( conflictIds.size() );

            buildConflitIdDAG( ids, node, id, visited, parents, conflictIds );
        }

        List<ConflictId> sorted = topsortConflictIds( ids.values() );

        Map<DependencyNode, Integer> depths = new IdentityHashMap<DependencyNode, Integer>( conflictIds.size() );
        for ( ConflictId id : sorted )
        {
            ConflictGroup group = new ConflictGroup( id );
            depths.clear();
            selectVersion( node, null, 0, depths, group, conflictIds, ids );
            pruneNonSelectedVersions( group, conflictIds );
        }

        return node;
    }

    private void buildConflitIdDAG( Map<Object, ConflictId> ids, DependencyNode node, ConflictId id,
                                    Map<DependencyNode, Object> visited, Set<ConflictId> parents, Map<?, ?> conflictIds )
    {
        if ( visited.put( node, Boolean.TRUE ) != null )
        {
            return;
        }

        ConflictId parentId = id;

        for ( DependencyNode child : node.getChildren() )
        {
            Object key = conflictIds.get( child );
            ConflictId childId = ids.get( key );
            if ( childId == null )
            {
                childId = new ConflictId( key );
                ids.put( key, childId );
            }

            boolean noCircle = parents.add( childId );
            if ( noCircle && parentId != null )
            {
                parentId.add( childId );
            }

            buildConflitIdDAG( ids, child, childId, visited, parents, conflictIds );

            if ( noCircle )
            {
                parents.remove( childId );
            }
        }
    }

    private List<ConflictId> topsortConflictIds( Collection<ConflictId> conflictIds )
    {
        List<ConflictId> sorted = new ArrayList<ConflictId>( conflictIds.size() );

        Queue<ConflictId> roots = new LinkedList<ConflictId>();
        for ( ConflictId id : conflictIds )
        {
            if ( id.inDegree <= 0 )
            {
                roots.add( id );
            }
        }

        while ( !roots.isEmpty() )
        {
            ConflictId root = roots.remove();

            sorted.add( root );

            for ( ConflictId child : root.children )
            {
                child.inDegree--;
                if ( child.inDegree <= 0 )
                {
                    roots.add( child );
                }
            }
        }

        return sorted;
    }

    private void selectVersion( DependencyNode node, DependencyNode parent, int depth,
                                Map<DependencyNode, Integer> depths, ConflictGroup group, Map<?, ?> conflictIds,
                                Map<?, ConflictId> ids )
        throws RepositoryException
    {
        Integer smallestDepth = depths.get( node );
        if ( smallestDepth == null || smallestDepth.intValue() > depth )
        {
            depths.put( node, Integer.valueOf( depth ) );
        }
        else
        {
            return;
        }

        Object key = conflictIds.get( node );
        if ( group.id.key.equals( key ) )
        {
            if ( parent != null )
            {
                group.positions.add( new Position( parent, depth ) );
            }

            if ( !group.isAcceptable( node.getVersion() ) )
            {
                return;
            }

            smallestDepth = group.versions.get( node.getVersion() );
            if ( smallestDepth == null || smallestDepth.intValue() > depth )
            {
                group.versions.put( node.getVersion(), Integer.valueOf( depth ) );
            }

            if ( !node.getVersionConstraint().getRanges().isEmpty() )
            {
                group.constraints.add( node.getVersionConstraint() );
            }

            if ( depth < group.depth )
            {
                Version recommendedVersion = node.getVersionConstraint().getPreferredVersion();
                if ( group.isAcceptable( recommendedVersion ) )
                {
                    group.version = recommendedVersion;
                    group.depth = depth;
                }
            }

            if ( group.version == null || !group.isAcceptable( group.version ) )
            {
                group.version = null;
                for ( Iterator<Map.Entry<Version, Integer>> it = group.versions.entrySet().iterator(); it.hasNext(); )
                {
                    Map.Entry<Version, Integer> entry = it.next();
                    Version version = entry.getKey();
                    if ( !group.isAcceptable( version ) )
                    {
                        it.remove();
                    }
                    else if ( group.version == null || version.compareTo( group.version ) > 0 )
                    {
                        group.version = version;
                        group.depth = entry.getValue().intValue();
                    }
                }
                if ( group.version == null )
                {
                    Collection<String> versions = new LinkedHashSet<String>();
                    for ( VersionConstraint constraint : group.constraints )
                    {
                        versions.add( constraint.toString() );
                    }
                    throw new UnsolvableVersionConflictException( group.id.key, versions );
                }
            }
        }

        depth++;

        for ( DependencyNode child : node.getChildren() )
        {
            selectVersion( child, node, depth, depths, group, conflictIds, ids );
        }
    }

    private void pruneNonSelectedVersions( ConflictGroup group, Map<?, ?> conflictIds )
    {
        for ( Position pos : group.positions )
        {
            for ( Iterator<DependencyNode> it = pos.parent.getChildren().iterator(); it.hasNext(); )
            {
                DependencyNode child = it.next();

                Object key = conflictIds.get( child );

                if ( group.id.key.equals( key ) )
                {
                    if ( !group.pruned && group.depth == pos.depth && group.version.equals( child.getVersion() ) )
                    {
                        group.pruned = true;
                    }
                    else
                    {
                        it.remove();
                    }
                }
            }
        }
    }

    static final class ConflictId
    {

        final Object key;

        Collection<ConflictId> children = Collections.emptySet();

        int inDegree;

        public ConflictId( Object key )
        {
            this.key = key;
        }

        public void add( ConflictId child )
        {
            if ( children.isEmpty() )
            {
                children = new HashSet<ConflictId>();
            }
            if ( children.add( child ) )
            {
                child.inDegree++;
            }
        }

        @Override
        public boolean equals( Object obj )
        {
            if ( this == obj )
            {
                return true;
            }
            else if ( !( obj instanceof ConflictId ) )
            {
                return false;
            }
            ConflictId that = (ConflictId) obj;
            return this.key.equals( that.key );
        }

        @Override
        public int hashCode()
        {
            return key.hashCode();
        }

        @Override
        public String toString()
        {
            return key + " <" + inDegree;
        }

    }

    static final class ConflictGroup
    {

        final ConflictId id;

        Collection<VersionConstraint> constraints = new HashSet<VersionConstraint>();

        Map<Version, Integer> versions = new LinkedHashMap<Version, Integer>();

        Version version;

        int depth;

        Collection<Position> positions = new LinkedHashSet<Position>();

        boolean pruned;

        public ConflictGroup( ConflictId id )
        {
            this.id = id;
            this.depth = Integer.MAX_VALUE;
        }

        boolean isAcceptable( Version version )
        {
            for ( VersionConstraint constraint : constraints )
            {
                if ( !constraint.containsVersion( version ) )
                {
                    return false;
                }
            }
            return true;
        }

        @Override
        public String toString()
        {
            return id + " > " + version;
        }

    }

    static final class Position
    {

        final DependencyNode parent;

        final int depth;

        final int hash;

        public Position( DependencyNode parent, int depth )
        {
            this.parent = parent;
            this.depth = depth;
            hash = 31 * parent.hashCode() + depth;
        }

        @Override
        public boolean equals( Object obj )
        {
            if ( this == obj )
            {
                return true;
            }
            else if ( !( obj instanceof Position ) )
            {
                return false;
            }
            Position that = (Position) obj;
            return this.parent == that.parent && this.depth == that.depth;
        }

        @Override
        public int hashCode()
        {
            return hash;
        }

        @Override
        public String toString()
        {
            return depth + " > " + parent;
        }

    }

}
