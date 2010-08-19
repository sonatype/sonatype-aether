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

import static org.junit.Assert.*;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.sonatype.aether.DependencyNode;

/**
 * @author Benjamin Bentmann
 */
public class NearestVersionConflictResolverTest
    extends AbstractDependencyGraphTransformerTest
{

    @Test
    public void testMultipleVersionsAtSameLevel()
        throws Exception
    {
        // root
        // +- a:2
        // +- a:1

        DependencyNode a1 = builder.artifactId( "a" ).version( "1" ).build();
        DependencyNode a2 = builder.artifactId( "a" ).version( "2" ).build();

        DependencyNode root = builder.artifactId( null ).build();
        root.getChildren().add( a2 );
        root.getChildren().add( a1 );

        Map<DependencyNode, Object> conflictIds = new IdentityHashMap<DependencyNode, Object>();
        conflictIds.put( a1, "a" );
        conflictIds.put( a2, "a" );
        context.put( TransformationContextKeys.CONFLICT_IDS, conflictIds );

        NearestVersionConflictResolver transformer = new NearestVersionConflictResolver();
        root = transformer.transformGraph( root, context );

        assertEquals( 1, root.getChildren().size() );
        assertSame( a2, root.getChildren().iterator().next() );
    }

    @Test
    public void testSelectedVersionAtDeeperLevelThanOriginallySeen()
        throws Exception
    {
        // root
        // +- a
        // |  \- b:1
        // |     \- j:1
        // +- c
        // |  \- d
        // |     \- e
        // |        \- j:1
        // \- b:2
        
        DependencyNode j = builder.artifactId( "j" ).build();

        DependencyNode b1 = builder.artifactId( "b" ).version( "1" ).build();
        b1.getChildren().add( j );
        DependencyNode a = builder.artifactId( "a" ).build();
        a.getChildren().add( b1 );

        DependencyNode e = builder.artifactId( "e" ).build();
        e.getChildren().add( j );
        DependencyNode d = builder.artifactId( "d" ).build();
        d.getChildren().add( e );
        DependencyNode c = builder.artifactId( "c" ).build();
        c.getChildren().add( d );

        DependencyNode b2 = builder.artifactId( "b" ).version( "2" ).build();

        DependencyNode root = builder.artifactId( null ).build();
        root.getChildren().add( a );
        root.getChildren().add( c );
        root.getChildren().add( b2 );

        Map<DependencyNode, Object> conflictIds = new IdentityHashMap<DependencyNode, Object>();
        conflictIds.put( j, "j" );
        conflictIds.put( a, "a" );
        conflictIds.put( b1, "b" );
        conflictIds.put( b2, "b" );
        conflictIds.put( c, "c" );
        conflictIds.put( d, "d" );
        conflictIds.put( e, "e" );
        context.put( TransformationContextKeys.CONFLICT_IDS, conflictIds );

        NearestVersionConflictResolver transformer = new NearestVersionConflictResolver();
        root = transformer.transformGraph( root, context );

        List<DependencyNode> trail = find( root, "j" );
        assertEquals( 5, trail.size() );
    }

    @Test
    public void testNearestVersionUnderneathRemovedNode()
        throws Exception
    {
        // root
        // +- a
        // |  \- b:1
        // |     \- j:1
        // +- c
        // |  \- d
        // |     \- e
        // |        \- j:2
        // \- b:2

        DependencyNode j1 = builder.artifactId( "j" ).version( "1" ).build();
        DependencyNode j2 = builder.artifactId( "j" ).version( "2" ).build();

        DependencyNode b1 = builder.artifactId( "b" ).version( "1" ).build();
        b1.getChildren().add( j1 );
        DependencyNode a = builder.artifactId( "a" ).build();
        a.getChildren().add( b1 );

        DependencyNode e = builder.artifactId( "e" ).build();
        e.getChildren().add( j2 );
        DependencyNode d = builder.artifactId( "d" ).build();
        d.getChildren().add( e );
        DependencyNode c = builder.artifactId( "c" ).build();
        c.getChildren().add( d );

        DependencyNode b2 = builder.artifactId( "b" ).version( "2" ).build();

        DependencyNode root = builder.artifactId( null ).build();
        root.getChildren().add( a );
        root.getChildren().add( c );
        root.getChildren().add( b2 );

        Map<DependencyNode, Object> conflictIds = new IdentityHashMap<DependencyNode, Object>();
        conflictIds.put( j1, "j" );
        conflictIds.put( j2, "j" );
        conflictIds.put( a, "a" );
        conflictIds.put( b1, "b" );
        conflictIds.put( b2, "b" );
        conflictIds.put( c, "c" );
        conflictIds.put( d, "d" );
        conflictIds.put( e, "e" );
        context.put( TransformationContextKeys.CONFLICT_IDS, conflictIds );

        NearestVersionConflictResolver transformer = new NearestVersionConflictResolver();
        root = transformer.transformGraph( root, context );

        List<DependencyNode> trail = find( root, "j" );
        assertEquals( 5, trail.size() );
    }

}
