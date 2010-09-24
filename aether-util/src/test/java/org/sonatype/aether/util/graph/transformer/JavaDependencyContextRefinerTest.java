package org.sonatype.aether.util.graph.transformer;


import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.sonatype.aether.RepositoryException;
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
 *
 */
public class JavaDependencyContextRefinerTest
{

    private JavaDependencyContextRefiner refiner;

    private DependencyGraphParser parser;

    private SimpleDependencyGraphTransformationContext context;

    @Before
    public void setUp()
    {
        refiner = new JavaDependencyContextRefiner();
        parser = new DependencyGraphParser();
        context = new SimpleDependencyGraphTransformationContext();

    }

    @Test
    public void testDoNotRefineOtherContext()
        throws IOException, RepositoryException
    {
        DependencyNode node = parser.parseLiteral( "gid:aid:ext:cls:ver" );
        node.setRequestContext( "otherContext" );

        DependencyNode refinedNode = refiner.transformGraph( node, context );
        assertEquals( node, refinedNode );
    }

    @Test
    public void testRefineToCompile()
        throws IOException, RepositoryException
    {
        String expected = "project/compile";

        DependencyNode node = parser.parseLiteral( "gid:aid:ext:ver:compile" );
        node.setRequestContext( "project" );
        DependencyNode refinedNode = refiner.transformGraph( node, context );
        assertEquals( expected, refinedNode.getRequestContext() );

        node = parser.parseLiteral( "gid:aid:ext:ver:system" );
        node.setRequestContext( "project" );
        refinedNode = refiner.transformGraph( node, context );
        assertEquals( expected, refinedNode.getRequestContext() );

        node = parser.parseLiteral( "gid:aid:ext:ver:provided" );
        node.setRequestContext( "project" );
        refinedNode = refiner.transformGraph( node, context );
        assertEquals( expected, refinedNode.getRequestContext() );
    }

    @Test
    public void testRefineToTest()
        throws IOException, RepositoryException
    {
        String expected = "project/test";

        DependencyNode node = parser.parseLiteral( "gid:aid:ext:ver:test" );
        node.setRequestContext( "project" );
        DependencyNode refinedNode = refiner.transformGraph( node, context );
        assertEquals( expected, refinedNode.getRequestContext() );
    }

    @Test
    public void testRefineToRuntime()
        throws IOException, RepositoryException
    {
        String expected = "project/runtime";

        DependencyNode node = parser.parseLiteral( "gid:aid:ext:ver:runtime" );
        node.setRequestContext( "project" );
        DependencyNode refinedNode = refiner.transformGraph( node, context );
        assertEquals( expected, refinedNode.getRequestContext() );
    }

    @Test
    public void testDoNotRefineUnknownScopes()
        throws IOException, RepositoryException
    {
        String expected = "project";

        DependencyNode node = parser.parseLiteral( "gid:aid:ext:ver:unknownScope" );
        node.setRequestContext( "project" );
        DependencyNode refinedNode = refiner.transformGraph( node, context );
        assertEquals( expected, refinedNode.getRequestContext() );
    }
}
