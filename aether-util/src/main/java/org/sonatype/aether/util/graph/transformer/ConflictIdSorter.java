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
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.sonatype.aether.RepositoryException;
import org.sonatype.aether.collection.DependencyGraphTransformationContext;
import org.sonatype.aether.collection.DependencyGraphTransformer;
import org.sonatype.aether.graph.DependencyNode;

/**
 * A dependency graph transformer that creates a topological sorting of the conflict ids which have been assigned to the
 * dependency nodes. Conflict ids are sorted according to the dependency relation induced by the dependency graph. This
 * transformer will query the key {@link TransformationContextKeys#CONFLICT_IDS} in the transformation context for an
 * existing mapping of nodes to their conflicts ids. In absence of this map, the transformer will automatically invoke
 * the {@link ConflictMarker} to calculate the conflict ids. When this transformer has executed, the transformation
 * context holds a {@code List<Object>} that denotes the topologically sorted conflict ids. The list will be stored
 * using the key {@link TransformationContextKeys#SORTED_CONFLICT_IDS}.
 * 
 * @author Benjamin Bentmann
 */
public class ConflictIdSorter
    implements DependencyGraphTransformer
{

    public DependencyNode transformGraph( DependencyNode node, DependencyGraphTransformationContext context )
        throws RepositoryException
    {
        Map<?, ?> conflictIds = (Map<?, ?>) context.get( TransformationContextKeys.CONFLICT_IDS );
        if ( conflictIds == null )
        {
            ConflictMarker marker = new ConflictMarker();
            marker.transformGraph( node, context );

            conflictIds = (Map<?, ?>) context.get( TransformationContextKeys.CONFLICT_IDS );
        }

        Map<Object, ConflictId> ids = new LinkedHashMap<Object, ConflictId>( 256 );

        {
            ConflictId id = null;
            Object key = conflictIds.get( node );
            if ( key != null )
            {
                id = new ConflictId( key );
                ids.put( key, id );
            }

            Map<DependencyNode, Object> visited = new IdentityHashMap<DependencyNode, Object>( conflictIds.size() );

            buildConflitIdDAG( ids, node, id, visited, conflictIds );
        }

        List<Object> sorted = topsortConflictIds( ids.values() );

        context.put( TransformationContextKeys.SORTED_CONFLICT_IDS, sorted );

        return node;
    }

    private void buildConflitIdDAG( Map<Object, ConflictId> ids, DependencyNode node, ConflictId id,
                                    Map<DependencyNode, Object> visited, Map<?, ?> conflictIds )
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

            if ( parentId != null )
            {
                parentId.add( childId );
            }

            buildConflitIdDAG( ids, child, childId, visited, conflictIds );
        }
    }

    private List<Object> topsortConflictIds( Collection<ConflictId> conflictIds )
    {
        List<Object> sorted = new ArrayList<Object>( conflictIds.size() );

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

            sorted.add( root.key );

            for ( ConflictId child : root.children )
            {
                child.inDegree--;
                if ( child.inDegree == 0 )
                {
                    roots.add( child );
                }
            }
        }

        while ( sorted.size() < conflictIds.size() )
        {
            // cycle -> deal gracefully with nodes still having positive in-degree
            roots.clear();

            ConflictId nearest = null;
            for ( ConflictId id : conflictIds )
            {
                if ( id.inDegree <= 0 )
                {
                    continue;
                }
                if ( nearest == null || id.inDegree < nearest.inDegree )
                {
                    nearest = id;
                }
            }

            nearest.inDegree = 0;
            roots.add( nearest );

            while ( !roots.isEmpty() )
            {
                ConflictId root = roots.remove();

                sorted.add( root.key );

                for ( ConflictId child : root.children )
                {
                    child.inDegree--;
                    if ( child.inDegree == 0 )
                    {
                        roots.add( child );
                    }
                }
            }
        }

        return sorted;
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

}
