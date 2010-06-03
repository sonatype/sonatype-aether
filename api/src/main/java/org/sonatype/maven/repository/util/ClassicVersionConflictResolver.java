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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.sonatype.maven.repository.DependencyGraphTransformer;
import org.sonatype.maven.repository.DependencyNode;
import org.sonatype.maven.repository.RepositoryException;
import org.sonatype.maven.repository.UnsolvableVersionConflictException;

/**
 * @author Benjamin Bentmann
 */
public class ClassicVersionConflictResolver
    implements DependencyGraphTransformer
{

    public DependencyNode transformGraph( DependencyNode node )
        throws RepositoryException
    {
        Map<Object, VersionGroup> groups = new HashMap<Object, VersionGroup>();
        analyze( node, groups );
        prune( node, groups );
        return node;
    }

    private void analyze( DependencyNode node, Map<Object, VersionGroup> groups )
        throws RepositoryException
    {
        Map<Object, VersionGroup> localGroups = new HashMap<Object, VersionGroup>();

        for ( DependencyNode child : node.getChildren() )
        {
            Object key = child.getConflictId();

            VersionGroup localGroup = localGroups.get( key );
            if ( localGroup == null )
            {
                localGroup = new VersionGroup( key, child );
                localGroups.put( key, localGroup );
            }

            localGroup.add( child.getDependency().getArtifact().getVersion() );
        }

        for ( Map.Entry<Object, VersionGroup> entry : localGroups.entrySet() )
        {
            VersionGroup group = groups.get( entry.getKey() );
            if ( group == null )
            {
                groups.put( entry.getKey(), entry.getValue() );
            }
            else
            {
                group.merge( entry.getValue() );
            }
        }

        localGroups = null;

        for ( DependencyNode child : node.getChildren() )
        {
            Object key = child.getConflictId();

            VersionGroup group = groups.get( key );

            if ( group.versions.contains( child.getDependency().getArtifact().getVersion() ) )
            {
                analyze( child, groups );
            }
        }
    }

    private void prune( DependencyNode node, Map<Object, VersionGroup> groups )
    {
        for ( Iterator<DependencyNode> it = node.getChildren().iterator(); it.hasNext(); )
        {
            DependencyNode child = it.next();

            Object key = child.getConflictId();

            VersionGroup group = groups.get( key );

            if ( !group.pruned && group.depth == child.getDepth()
                && group.version.equals( child.getDependency().getArtifact().getVersion() ) )
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

    static class VersionGroup
    {

        final Object key;

        int depth;

        boolean soft;

        String version;

        Set<String> versions;

        Set<String> ranges;

        boolean pruned;

        public VersionGroup( Object key, DependencyNode node )
        {
            this.key = key;
            depth = node.getDepth();
            ranges = new LinkedHashSet<String>();
            if ( isRange( node.getRequestedVersion() ) )
            {
                soft = false;
                ranges.add( node.getRequestedVersion() );
            }
            else
            {
                soft = true;
            }
            versions = new LinkedHashSet<String>();
        }

        private static boolean isRange( String version )
        {
            return version.startsWith( "[" ) || version.startsWith( "(" );
        }

        public void add( String version )
        {
            this.version = version;
            versions.add( version );
        }

        public void merge( VersionGroup that )
            throws UnsolvableVersionConflictException
        {
            VersionGroup winner, loser;
            if ( that.depth < this.depth )
            {
                winner = that;
                loser = this;
            }
            else
            {
                winner = this;
                loser = that;
            }

            if ( !this.soft && !that.soft )
            {
                this.versions.retainAll( that.versions );
            }
            else if ( !that.soft )
            {
                this.versions = that.versions;
            }
            else if ( this.soft )
            {
                this.versions = winner.versions;
            }

            this.ranges.addAll( that.ranges );

            this.version = selectVersion( versions, winner.version, loser.version );

            this.soft = this.soft && that.soft;
            this.depth = winner.depth;
        }

        private String selectVersion( Set<String> versions, String dominant, String recessive )
            throws UnsolvableVersionConflictException
        {
            if ( versions.isEmpty() )
            {
                throw new UnsolvableVersionConflictException( key, ranges );
            }

            String version = "";
            if ( versions.contains( dominant ) )
            {
                version = dominant;
            }
            else if ( versions.contains( recessive ) )
            {
                version = recessive;
            }
            else
            {
                for ( String v : versions )
                {
                    version = v;
                }
            }
            return version;
        }

    }

}
