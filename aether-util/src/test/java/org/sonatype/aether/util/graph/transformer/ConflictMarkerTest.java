package org.sonatype.aether.util.graph.transformer;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Test;
import org.sonatype.aether.collection.DependencyGraphTransformationContext;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.test.util.NodeBuilder;
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
