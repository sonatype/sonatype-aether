package org.sonatype.aether.util.graph.transformer;

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

    @Test
    public void testSimple()
        throws IOException, RepositoryException
    {
        DependencyNode node = parser.parse( "simple.txt" );
        node = transform( node );

        expectOrder( "gid2:aid::ext", "gid:aid::ext", "gid:aid2::ext" );
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

    public DependencyNode transform( DependencyNode node )
        throws RepositoryException
    {
        node = new SimpleConflictMarker().transformGraph( node, ctx );
        node = sorter.transformGraph( node, ctx );
        return node;
    }

    @Test
    public void testCycle()
        throws RepositoryException, IOException
    {
        DependencyNode node = parser.parse( "cycle.txt" );
        node = transform( node );

        expectOrder( "gid:aid::ext", "gid2:aid::ext" );
    }

    @Test
    public void testNoConflicts()
        throws RepositoryException, IOException
    {
        DependencyNode node = parser.parse( "no-conflicts.txt" );
        node = transform( node );

        expectOrder( "gid:aid::ext", "gid3:aid::ext", "gid2:aid::ext", "gid4:aid::ext" );

    }
}
