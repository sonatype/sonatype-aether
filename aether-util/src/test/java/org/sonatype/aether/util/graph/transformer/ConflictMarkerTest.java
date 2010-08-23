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

import java.util.Map;

import org.junit.Test;
import org.sonatype.aether.DependencyGraphTransformationContext;
import org.sonatype.aether.DependencyNode;
import org.sonatype.aether.util.NodeBuilder;
import org.sonatype.aether.util.graph.transformer.ConflictMarker;

/**
 * @author Benjamin Bentmann
 */
public class ConflictMarkerTest
{

    private DependencyGraphTransformationContext newContext()
    {
        return new SimpleDependencyGraphTransformationContext();
    }

    @Test
    public void testSimple()
        throws Exception
    {
        NodeBuilder builder = new NodeBuilder();

        DependencyNode root = builder.build();
        root.getChildren().add( builder.artifactId( "a" ).build() );
        root.getChildren().add( builder.artifactId( "b" ).build() );

        DependencyGraphTransformationContext context = newContext();

        assertSame( root, new ConflictMarker().transformGraph( root, context ) );

        Map<?, ?> ids = (Map<?, ?>) context.get( TransformationContextKeys.CONFLICT_IDS );
        assertNotNull( ids );

        assertNull( ids.get( root ) );
        assertNotNull( ids.get( root.getChildren().get( 0 ) ) );
        assertNotNull( ids.get( root.getChildren().get( 1 ) ) );
        assertNotSame( ids.get( root.getChildren().get( 0 ) ), ids.get( root.getChildren().get( 1 ) ) );
        assertFalse( ids.get( root.getChildren().get( 0 ) ).equals( ids.get( root.getChildren().get( 1 ) ) ) );
    }

    @Test
    public void testRelocation1()
        throws Exception
    {
        NodeBuilder builder = new NodeBuilder();

        DependencyNode root = builder.build();
        root.getChildren().add( builder.artifactId( "a" ).build() );
        root.getChildren().add( builder.artifactId( "a" ).reloc( "reloc" ).build() );

        DependencyGraphTransformationContext context = newContext();

        assertSame( root, new ConflictMarker().transformGraph( root, context ) );

        Map<?, ?> ids = (Map<?, ?>) context.get( TransformationContextKeys.CONFLICT_IDS );
        assertNotNull( ids );

        assertNull( ids.get( root ) );
        assertNotNull( ids.get( root.getChildren().get( 0 ) ) );
        assertNotNull( ids.get( root.getChildren().get( 1 ) ) );
        assertSame( ids.get( root.getChildren().get( 0 ) ), ids.get( root.getChildren().get( 1 ) ) );
    }

    @Test
    public void testRelocation2()
        throws Exception
    {
        NodeBuilder builder = new NodeBuilder();

        DependencyNode root = builder.build();
        root.getChildren().add( builder.artifactId( "a" ).reloc( "reloc" ).build() );
        root.getChildren().add( builder.artifactId( "a" ).build() );

        DependencyGraphTransformationContext context = newContext();

        assertSame( root, new ConflictMarker().transformGraph( root, context ) );

        Map<?, ?> ids = (Map<?, ?>) context.get( TransformationContextKeys.CONFLICT_IDS );
        assertNotNull( ids );

        assertNull( ids.get( root ) );
        assertNotNull( ids.get( root.getChildren().get( 0 ) ) );
        assertNotNull( ids.get( root.getChildren().get( 1 ) ) );
        assertSame( ids.get( root.getChildren().get( 0 ) ), ids.get( root.getChildren().get( 1 ) ) );
    }

    @Test
    public void testRelocation3()
        throws Exception
    {
        NodeBuilder builder = new NodeBuilder();

        DependencyNode root = builder.build();
        root.getChildren().add( builder.artifactId( "a" ).build() );
        root.getChildren().add( builder.artifactId( "b" ).build() );
        root.getChildren().add( builder.artifactId( "c" ).reloc( "a" ).reloc( "b" ).build() );

        DependencyGraphTransformationContext context = newContext();

        assertSame( root, new ConflictMarker().transformGraph( root, context ) );

        Map<?, ?> ids = (Map<?, ?>) context.get( TransformationContextKeys.CONFLICT_IDS );
        assertNotNull( ids );

        assertNull( ids.get( root ) );
        assertNotNull( ids.get( root.getChildren().get( 0 ) ) );
        assertNotNull( ids.get( root.getChildren().get( 1 ) ) );
        assertNotNull( ids.get( root.getChildren().get( 2 ) ) );
        assertSame( ids.get( root.getChildren().get( 0 ) ), ids.get( root.getChildren().get( 1 ) ) );
        assertSame( ids.get( root.getChildren().get( 1 ) ), ids.get( root.getChildren().get( 2 ) ) );
    }

}
