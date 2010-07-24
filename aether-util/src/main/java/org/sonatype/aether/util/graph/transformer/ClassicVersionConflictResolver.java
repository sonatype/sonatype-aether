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
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;

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
public class ClassicVersionConflictResolver
    implements DependencyGraphTransformer
{

    public DependencyNode transformGraph( DependencyNode node )
        throws RepositoryException
    {
        Map<Object, ConflictGroup> groups = new HashMap<Object, ConflictGroup>( 1024 );
        analyze( node, groups );
        prune( node, groups );
        return node;
    }

    private void analyze( DependencyNode node, Map<Object, ConflictGroup> groups )
        throws RepositoryException
    {
        if ( node.getDependency() != null )
        {
            Object key = node.getConflictId();

            ConflictGroup group = groups.get( key );
            if ( group == null )
            {
                group = new ConflictGroup( key, node.getVersionConstraint().getPreferredVersion(), node.getDepth() );
                groups.put( key, group );
            }
            else if ( !group.isAcceptable( node.getVersion() ) )
            {
                return;
            }

            group.versions.add( node.getVersion() );
            if ( !node.getVersionConstraint().getRanges().isEmpty() )
            {
                group.constraints.add( node.getVersionConstraint() );
            }

            if ( node.getDepth() < group.depth )
            {
                group.depth = node.getDepth();

                Version recommendedVersion = node.getVersionConstraint().getPreferredVersion();
                if ( group.isAcceptable( recommendedVersion ) )
                {
                    group.version = recommendedVersion;
                }
            }

            if ( !group.isAcceptable( group.version ) )
            {
                group.version = null;
                for ( Iterator<Version> it = group.versions.iterator(); it.hasNext(); )
                {
                    Version version = it.next();
                    if ( !group.isAcceptable( version ) )
                    {
                        it.remove();
                    }
                    else if ( group.version == null || version.compareTo( group.version ) > 0 )
                    {
                        group.version = version;
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

        for ( DependencyNode child : node.getChildren() )
        {
            analyze( child, groups );
        }
    }

    private void prune( DependencyNode node, Map<Object, ConflictGroup> groups )
    {
        for ( Iterator<DependencyNode> it = node.getChildren().iterator(); it.hasNext(); )
        {
            DependencyNode child = it.next();

            Object key = child.getConflictId();

            ConflictGroup group = groups.get( key );

            if ( !group.pruned && group.depth == child.getDepth() && group.version.equals( child.getVersion() ) )
            {
                group.pruned = true;
                prune( child, groups );
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

        Collection<Version> versions = new LinkedList<Version>();

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
