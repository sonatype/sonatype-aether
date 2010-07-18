package org.sonatype.aether.util;

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

import org.junit.Test;
import org.sonatype.aether.DependencyNode;
import org.sonatype.aether.util.graph.transformer.ConflictMarker;

/**
 * @author Benjamin Bentmann
 */
public class ConflictMarkerTest
{

    @Test
    public void testSimple()
        throws Exception
    {
        NodeBuilder builder = new NodeBuilder();

        DependencyNode root = builder.build();
        builder.parent( root );
        root.getChildren().add( builder.artifactId( "a" ).build() );
        root.getChildren().add( builder.artifactId( "b" ).build() );

        root = new ConflictMarker().transformGraph( root );

        assertNotNull( root );
        assertNull( root.getConflictId() );
        assertNotNull( root.getChildren().get( 0 ).getConflictId() );
        assertNotNull( root.getChildren().get( 1 ).getConflictId() );
        assertNotSame( root.getChildren().get( 0 ).getConflictId(), root.getChildren().get( 1 ).getConflictId() );
        assertFalse( root.getChildren().get( 0 ).getConflictId().equals( root.getChildren().get( 1 ).getConflictId() ) );
    }

    @Test
    public void testRelocation1()
        throws Exception
    {
        NodeBuilder builder = new NodeBuilder();

        DependencyNode root = builder.build();
        builder.parent( root );
        root.getChildren().add( builder.artifactId( "a" ).build() );
        root.getChildren().add( builder.artifactId( "a" ).reloc( "reloc" ).build() );

        root = new ConflictMarker().transformGraph( root );

        assertNotNull( root );
        assertNull( root.getConflictId() );
        assertNotNull( root.getChildren().get( 0 ).getConflictId() );
        assertNotNull( root.getChildren().get( 1 ).getConflictId() );
        assertSame( root.getChildren().get( 0 ).getConflictId(), root.getChildren().get( 1 ).getConflictId() );
    }

    @Test
    public void testRelocation2()
        throws Exception
    {
        NodeBuilder builder = new NodeBuilder();

        DependencyNode root = builder.build();
        builder.parent( root );
        root.getChildren().add( builder.artifactId( "a" ).reloc( "reloc" ).build() );
        root.getChildren().add( builder.artifactId( "a" ).build() );

        root = new ConflictMarker().transformGraph( root );

        assertNotNull( root );
        assertNull( root.getConflictId() );
        assertNotNull( root.getChildren().get( 0 ).getConflictId() );
        assertNotNull( root.getChildren().get( 1 ).getConflictId() );
        assertSame( root.getChildren().get( 0 ).getConflictId(), root.getChildren().get( 1 ).getConflictId() );
    }

    @Test
    public void testRelocation3()
        throws Exception
    {
        NodeBuilder builder = new NodeBuilder();

        DependencyNode root = builder.build();
        builder.parent( root );
        root.getChildren().add( builder.artifactId( "a" ).build() );
        root.getChildren().add( builder.artifactId( "b" ).build() );
        root.getChildren().add( builder.artifactId( "c" ).reloc( "a" ).reloc( "b" ).build() );

        root = new ConflictMarker().transformGraph( root );

        assertNotNull( root );
        assertNull( root.getConflictId() );
        assertNotNull( root.getChildren().get( 0 ).getConflictId() );
        assertNotNull( root.getChildren().get( 1 ).getConflictId() );
        assertNotNull( root.getChildren().get( 2 ).getConflictId() );
        assertSame( root.getChildren().get( 0 ).getConflictId(), root.getChildren().get( 1 ).getConflictId() );
        assertSame( root.getChildren().get( 1 ).getConflictId(), root.getChildren().get( 2 ).getConflictId() );
    }

}
