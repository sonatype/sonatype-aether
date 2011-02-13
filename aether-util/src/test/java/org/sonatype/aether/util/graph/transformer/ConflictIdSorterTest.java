package org.sonatype.aether.util.graph.transformer;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *******************************************************************************/

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.junit.Before;
import org.junit.Test;
import org.sonatype.aether.RepositoryException;
import org.sonatype.aether.collection.DependencyGraphTransformationContext;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.test.util.DependencyGraphParser;

/**
 * @author Benjamin Hanzelmann
 */
public class ConflictIdSorterTest
{

    private ConflictIdSorter sorter;

    private DependencyGraphTransformationContext ctx;

    private DependencyGraphParser parser;

    @Before
    public void setup()
    {
        sorter = new ConflictIdSorter();
        ctx = new SimpleDependencyGraphTransformationContext();
        parser = new DependencyGraphParser( "conflictid-sorter-test/" );
    }

    private void expectOrder( List<String> sorted, String... id )
    {
        Queue<String> queue = new LinkedList<String>( sorted );

        for ( int i = 0; i < id.length; i++ )
        {
            String item = queue.poll();
            assertNotNull( String.format( "not enough conflict groups (no match for '%s'", id[i] ), item );

            assertEquals( id[i], item );
        }

        assertTrue( String.format( "leftover conflict groups (remaining: '%s')", queue ), queue.isEmpty() );
    }

    private void expectOrder( String... id )
    {
        @SuppressWarnings( "unchecked" )
        List<String> sorted = (List<String>) ctx.get( TransformationContextKeys.SORTED_CONFLICT_IDS );
        expectOrder( sorted, id );
    }

    private void expectCycle( boolean cycle )
    {
        assertEquals( Boolean.valueOf( cycle ), ctx.get( TransformationContextKeys.CYCLIC_CONFLICT_IDS ) );
    }

    public DependencyNode transform( DependencyNode node )
        throws RepositoryException
    {
        node = new SimpleConflictMarker().transformGraph( node, ctx );
        node = sorter.transformGraph( node, ctx );
        return node;
    }

    @Test
    public void testSimple()
        throws IOException, RepositoryException
    {
        DependencyNode node = parser.parse( "simple.txt" );
        node = transform( node );

        expectOrder( "gid2:aid::ext", "gid:aid::ext", "gid:aid2::ext" );
        expectCycle( false );
    }

    @Test
    public void testCycle()
        throws RepositoryException, IOException
    {
        DependencyNode node = parser.parse( "cycle.txt" );
        node = transform( node );

        expectOrder( "gid:aid::ext", "gid2:aid::ext" );
        expectCycle( true );
    }

    @Test
    public void testNoConflicts()
        throws RepositoryException, IOException
    {
        DependencyNode node = parser.parse( "no-conflicts.txt" );
        node = transform( node );

        expectOrder( "gid:aid::ext", "gid3:aid::ext", "gid2:aid::ext", "gid4:aid::ext" );
        expectCycle( false );
    }

}
