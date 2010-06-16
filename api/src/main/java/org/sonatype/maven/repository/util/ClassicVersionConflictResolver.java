package org.sonatype.maven.repository.util;

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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.TreeSet;

import org.sonatype.maven.repository.DependencyGraphTransformer;
import org.sonatype.maven.repository.DependencyNode;
import org.sonatype.maven.repository.RepositoryException;
import org.sonatype.maven.repository.UnsolvableVersionConflictException;
import org.sonatype.maven.repository.Version;
import org.sonatype.maven.repository.VersionConstraint;

/**
 * @author Benjamin Bentmann
 */
public class ClassicVersionConflictResolver
    implements DependencyGraphTransformer
{

    public DependencyNode transformGraph( DependencyNode node )
        throws RepositoryException
    {
        Map<Object, ConflictGroup> groups = new HashMap<Object, ConflictGroup>();
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

            if ( !node.getVersionConstraint().getRanges().isEmpty() )
            {
                group.constraints.add( node.getVersionConstraint() );
            }
            group.versions.add( node.getVersion() );

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
                for ( Version version : group.versions )
                {
                    if ( group.isAcceptable( version ) )
                    {
                        group.version = version;
                        break;
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

        Collection<Version> versions = new TreeSet<Version>( Collections.reverseOrder() );

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
