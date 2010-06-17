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
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

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
        Map<String, ConflictGroup> groups = new HashMap<String, ConflictGroup>();
        analyze( node, groups );
        mark( node, groups );
        return node;
    }

    private void analyze( DependencyNode node, Map<String, ConflictGroup> groups )
    {
        Set<String> keys = getKeys( node );
        if ( !keys.isEmpty() )
        {
            ConflictGroup group = null;

            for ( String key : keys )
            {
                ConflictGroup g = groups.get( key );

                if ( group == null )
                {
                    group = g;
                }
                else if ( group != g )
                {
                    ConflictGroup bigger, smaller;

                    if ( group.getKeys().size() < g.getKeys().size() )
                    {
                        bigger = g;
                        smaller = group;
                    }
                    else
                    {
                        bigger = group;
                        smaller = g;
                    }

                    for ( String k : smaller.getKeys() )
                    {
                        groups.put( k, bigger );
                    }
                    bigger.getKeys().addAll( smaller.getKeys() );
                }
            }

            if ( group == null )
            {
                group = new ConflictGroup( keys );
                for ( String key : keys )
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

    private Set<String> getKeys( DependencyNode node )
    {
        Set<String> keys;

        Dependency dependency = node.getDependency();
        if ( dependency == null )
        {
            keys = Collections.emptySet();
        }
        else
        {
            String key = toKey( dependency.getArtifact() );

            if ( node.getRelocations().isEmpty() && node.getAliases().isEmpty() )
            {
                keys = Collections.singleton( key );
            }
            else
            {
                keys = new TreeSet<String>();
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

    private void mark( DependencyNode node, Map<String, ConflictGroup> groups )
    {
        Dependency dependency = node.getDependency();
        if ( dependency != null )
        {
            String key = toKey( dependency.getArtifact() );
            node.setConflictId( groups.get( key ) );
        }

        for ( DependencyNode child : node.getChildren() )
        {
            mark( child, groups );
        }
    }

    private static String toKey( Artifact artifact )
    {
        return artifact.getGroupId() + ':' + artifact.getArtifactId() + ':' + artifact.getClassifier() + ':'
            + artifact.getExtension();
    }

    static class ConflictGroup
    {

        private final Set<String> keys;

        public ConflictGroup( Set<String> keys )
        {
            this.keys = keys;
        }

        public Set<String> getKeys()
        {
            return keys;
        }

        @Override
        public String toString()
        {
            return String.valueOf( keys );
        }

    }

}
