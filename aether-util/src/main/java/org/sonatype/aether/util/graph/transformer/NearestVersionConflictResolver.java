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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;

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

        Map<Object, ConflictGroup> groups = new HashMap<Object, ConflictGroup>( 1024 );
        Map<DependencyNode, Integer> depths = new IdentityHashMap<DependencyNode, Integer>( 1024 );

        analyze( node, 0, depths, groups, conflictIds );

        prune( node, 0, groups, conflictIds );

        return node;
    }

    private void analyze( DependencyNode node, int depth, Map<DependencyNode, Integer> depths,
                          Map<Object, ConflictGroup> groups, Map<?, ?> conflictIds )
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

        if ( node.getDependency() != null )
        {
            Object key = conflictIds.get( node );

            ConflictGroup group = groups.get( key );
            if ( group == null )
            {
                group = new ConflictGroup( key, node.getVersionConstraint().getPreferredVersion(), depth );
                groups.put( key, group );
            }
            else if ( !group.isAcceptable( node.getVersion() ) )
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

            if ( !group.isAcceptable( group.version ) )
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
                    throw new UnsolvableVersionConflictException( key, versions );
                }
            }
        }

        depth++;

        for ( DependencyNode child : node.getChildren() )
        {
            analyze( child, depth, depths, groups, conflictIds );
        }
    }

    private void prune( DependencyNode node, int depth, Map<Object, ConflictGroup> groups, Map<?, ?> conflictIds )
    {
        depth++;

        for ( Iterator<DependencyNode> it = node.getChildren().iterator(); it.hasNext(); )
        {
            DependencyNode child = it.next();

            Object key = conflictIds.get( child );

            ConflictGroup group = groups.get( key );

            if ( !group.pruned && group.depth == depth && group.version.equals( child.getVersion() ) )
            {
                group.pruned = true;
                prune( child, depth, groups, conflictIds );
            }
            else
            {
                it.remove();
            }
        }
    }

    static class ConflictGroup
    {

        final Object key;

        Collection<VersionConstraint> constraints = new HashSet<VersionConstraint>();

        Map<Version, Integer> versions = new HashMap<Version, Integer>();

        Version version;

        int depth;

        boolean pruned;

        public ConflictGroup( Object key, Version version, int depth )
        {
            this.key = key;
            this.version = version;
            this.depth = depth;
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
    }

}
