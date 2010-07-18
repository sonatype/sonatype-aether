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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.sonatype.aether.Artifact;
import org.sonatype.aether.Dependency;
import org.sonatype.aether.DependencyGraphTransformer;
import org.sonatype.aether.DependencyNode;
import org.sonatype.aether.RepositoryException;

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
            boolean fixMappings = false;

            for ( Object key : keys )
            {
                ConflictGroup g = groups.get( key );

                if ( group != g )
                {
                    if ( group == null )
                    {
                        Set<Object> newKeys = merge( g.keys, keys );
                        if ( newKeys == g.keys )
                        {
                            group = g;
                            break;
                        }
                        else
                        {
                            group = new ConflictGroup( newKeys );
                            fixMappings = true;
                        }
                    }
                    else if ( g == null )
                    {
                        fixMappings = true;
                    }
                    else
                    {
                        Set<Object> newKeys = merge( g.keys, group.keys );
                        if ( newKeys == g.keys )
                        {
                            group = g;
                            fixMappings = false;
                            break;
                        }
                        else if ( newKeys != group.keys )
                        {
                            group = new ConflictGroup( newKeys );
                            fixMappings = true;
                        }
                    }
                }
            }

            if ( group == null )
            {
                group = new ConflictGroup( keys );
                fixMappings = true;
            }
            if ( fixMappings )
            {
                for ( Object key : group.keys )
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

    private Set<Object> merge( Set<Object> keys1, Set<Object> keys2 )
    {
        int size1 = keys1.size();
        int size2 = keys2.size();

        if ( size1 < size2 )
        {
            if ( keys2.containsAll( keys1 ) )
            {
                return keys2;
            }
        }
        else
        {
            if ( keys1.containsAll( keys2 ) )
            {
                return keys1;
            }
        }

        Set<Object> keys = new HashSet<Object>();
        keys.addAll( keys1 );
        keys.addAll( keys2 );
        return keys;
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
                keys = new HashSet<Object>();
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
