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

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.sonatype.maven.repository.Artifact;
import org.sonatype.maven.repository.Dependency;
import org.sonatype.maven.repository.DependencyGraphTransformer;
import org.sonatype.maven.repository.DependencyNode;
import org.sonatype.maven.repository.RepositoryException;

/**
 * @author Benjamin Bentmann
 */
public class ConflictMarker
    implements DependencyGraphTransformer
{

    public DependencyNode transformGraph( DependencyNode node )
        throws RepositoryException
    {
        Map<Object, ConflictGroup> groups = new HashMap<Object, ConflictGroup>( 1024 );
        analyze( node, groups );
        mark( node, groups );
        return node;
    }

    private void analyze( DependencyNode node, Map<Object, ConflictGroup> groups )
    {
        Set<Object> keys = getKeys( node );
        if ( !keys.isEmpty() )
        {
            ConflictGroup group = null;

            for ( Object key : keys )
            {
                ConflictGroup g = groups.get( key );

                if ( group == null )
                {
                    group = g;
                }
                else if ( group != g )
                {
                    ConflictGroup bigger, smaller;

                    if ( group.keys.size() < g.keys.size() )
                    {
                        bigger = g;
                        smaller = group;
                    }
                    else
                    {
                        bigger = group;
                        smaller = g;
                    }

                    for ( Object k : smaller.keys )
                    {
                        groups.put( k, bigger );
                    }
                    bigger.keys.addAll( smaller.keys );
                }
            }

            if ( group == null )
            {
                group = new ConflictGroup( keys );
                for ( Object key : keys )
                {
                    groups.put( key, group );
                }
            }
        }

        for ( DependencyNode child : node.getChildren() )
        {
            analyze( child, groups );
        }
    }

    private Set<Object> getKeys( DependencyNode node )
    {
        Set<Object> keys;

        Dependency dependency = node.getDependency();
        if ( dependency == null )
        {
            keys = Collections.emptySet();
        }
        else
        {
            Object key = toKey( dependency.getArtifact() );

            if ( node.getRelocations().isEmpty() && node.getAliases().isEmpty() )
            {
                keys = Collections.singleton( key );
            }
            else
            {
                keys = new LinkedHashSet<Object>();
                keys.add( key );

                for ( Artifact relocation : node.getRelocations() )
                {
                    key = toKey( relocation );
                    keys.add( key );
                }

                for ( Artifact alias : node.getAliases() )
                {
                    key = toKey( alias );
                    keys.add( key );
                }
            }
        }

        return keys;
    }

    private void mark( DependencyNode node, Map<Object, ConflictGroup> groups )
    {
        Dependency dependency = node.getDependency();
        if ( dependency != null )
        {
            Object key = toKey( dependency.getArtifact() );
            node.setConflictId( groups.get( key ).keys );
        }

        for ( DependencyNode child : node.getChildren() )
        {
            mark( child, groups );
        }
    }

    private static Object toKey( Artifact artifact )
    {
        return new Key( artifact );
    }

    static class ConflictGroup
    {

        final Set<Object> keys;

        public ConflictGroup( Set<Object> keys )
        {
            this.keys = keys;
        }

        @Override
        public String toString()
        {
            return String.valueOf( keys );
        }

    }

    static class Key
    {

        private final Artifact artifact;

        public Key( Artifact artifact )
        {
            this.artifact = artifact;
        }

        @Override
        public boolean equals( Object obj )
        {
            if ( obj == this )
            {
                return true;
            }
            else if ( !( obj instanceof Key ) )
            {
                return false;
            }
            Key that = (Key) obj;
            return artifact.getArtifactId().equals( that.artifact.getArtifactId() )
                && artifact.getGroupId().equals( that.artifact.getGroupId() )
                && artifact.getExtension().equals( that.artifact.getExtension() )
                && artifact.getClassifier().equals( that.artifact.getClassifier() );
        }

        @Override
        public int hashCode()
        {
            int hash = 17;
            hash = hash * 31 + artifact.getArtifactId().hashCode();
            hash = hash * 31 + artifact.getGroupId().hashCode();
            hash = hash * 31 + artifact.getClassifier().hashCode();
            hash = hash * 31 + artifact.getExtension().hashCode();
            return hash;
        }

        @Override
        public String toString()
        {
            return artifact.getGroupId() + ':' + artifact.getArtifactId() + ':' + artifact.getClassifier() + ':'
                + artifact.getExtension();
        }

    }

}
