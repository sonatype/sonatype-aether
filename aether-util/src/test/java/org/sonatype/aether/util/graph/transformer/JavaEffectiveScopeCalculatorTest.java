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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.sonatype.aether.RepositoryException;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.test.util.DependencyGraphParser;

public class JavaEffectiveScopeCalculatorTest
{

    private DependencyGraphParser parser;

    private SimpleDependencyGraphTransformationContext ctx;

    private enum Scope
    {
        TEST, PROVIDED, RUNTIME, COMPILE;

        @Override
        public String toString()
        {
            return super.name().toLowerCase( Locale.ENGLISH );
        }
    }

    private DependencyNode parse( String name, String... substitutions )
        throws IOException
    {
        parser.setSubstitutions( Arrays.asList( substitutions ) );
        return parser.parse( name );
    }

    @Test
    public void testScopeInheritanceProvided()
        throws IOException, RepositoryException
    {
        String resource = "inheritance.txt";

        String expected = "provided";
        int[] coords = new int[] { 0, 0 };
        expectScope( expected, transform( parse( resource, "provided", "test" ) ), coords );
    }

    private void expectScope( String expected, DependencyNode root, int... coords )
    {
        expectScope( "", expected, root, coords );
    }

    private void expectScope( String msg, String expected, DependencyNode root, int... coords )
    {

        try
        {
            DependencyNode node = root;
            node = path( node, coords );

            assertEquals( msg + "\nculprit: " + node.toString() + "\n", expected,
                          node.getDependency().getScope() );
        }
        catch ( IndexOutOfBoundsException e )
        {
            throw new IllegalArgumentException( "Illegal coordinates for child", e );
        }
        catch ( NullPointerException e )
        {
            throw new IllegalArgumentException( "Illegal coordinates for child", e );
        }

    }

    private DependencyNode path( DependencyNode node, int... coords )
    {
        for ( int i = 0; i < coords.length; i++ )
        {
            node = node.getChildren().get( coords[i] );
        }
        return node;
    }

    // @Test
    /**
     * Does not work yet, provide/test are not ordered
     */
    public void testScopeInheritance()
        throws IOException, RepositoryException
    {
        Scope[] scopes = Scope.values();
        for ( Scope scope1 : scopes )
        {
            for ( Scope scope2 : scopes )
            {
                parser.setSubstitutions( scope1.toString(), scope2.toString() );
                String expected = scope1.compareTo( scope2 ) > 0 ? scope1.toString() : scope2.toString();

                DependencyNode root = parser.parse( "inheritance.txt" );
                System.out.println( parser.dump( root ) );
                root = transform( root );
                System.out.println( parser.dump( root ) );

                List<DependencyNode> rootChildren = root.getChildren();
                assertEquals( 1, rootChildren.size() );

                List<DependencyNode> deepChildren = rootChildren.get( 0 ).getChildren();
                assertEquals( 1, deepChildren.size() );

                assertEquals( expected, rootChildren.get( 0 ).getDependency().getScope() );
                assertEquals( expected, deepChildren.get( 0 ).getDependency().getScope() );

            }
        }
    }

    @Test
    public void testConflictWinningScopeGetsUsedForInheritance()
        throws Exception
    {
        DependencyNode root = parser.parse( "conflict-and-inheritance.txt" );
        root = transform( root );

        expectScope( "compile", root, 0, 0 );
        expectScope( "compile", root, 0, 0, 0 );
    }

    @Test
    public void testScopeOfDirectDependencyWinsConflictAndGetsUsedForInheritanceToChildrenEverywhereInGraph()
        throws Exception
    {
        DependencyNode root = parser.parse( "direct-with-conflict-and-inheritance.txt" );
        root = transform( root );

        expectScope( "test", root, 0, 0 );
        expectScope( "test", root, 1, 0, 0 );
    }

    @Test
    public void testCycleA()
        throws Exception
    {
        DependencyNode root = parser.parse( "cycle-a.txt" );
        root = transform( root );

        expectScope( "compile", root, 0 );
        expectScope( "runtime", root, 0, 0 );
        expectScope( "runtime", root, 1 );
        expectScope( "compile", root, 1, 0 );
    }

    @Test
    public void testCycleB()
        throws Exception
    {
        DependencyNode root = parser.parse( "cycle-b.txt" );
        root = transform( root );

        expectScope( "runtime", root, 0 );
        expectScope( "compile", root, 0, 0 );
        expectScope( "compile", root, 1 );
        expectScope( "runtime", root, 1, 0 );
    }

    @Before
    public void setup()
    {
        parser = new DependencyGraphParser( "org/sonatype/aether/util/graph/transformer/scope/" );
        ctx = new SimpleDependencyGraphTransformationContext();
    }

    private DependencyNode transform( DependencyNode root )
        throws RepositoryException
    {
        root = new SimpleConflictMarker().transformGraph( root, ctx );
        root = new JavaEffectiveScopeCalculator().transformGraph( root, ctx );
        return root;
    }

    @Test
    public void testDirectNodesAlwaysWin()
        throws IOException, RepositoryException
    {

        for ( Scope directScope : Scope.values() )
        {
            String direct = directScope.toString();

            parser.setSubstitutions( direct );
            DependencyNode root = transform( parser.parse( "direct-nodes-winning.txt" ) );

            String msg =
                String.format( "direct node should be setting scope ('%s') for all nodes.\n" + parser.dump( root ),
                               direct );
            expectScope( msg, direct, root, 0 );
            expectScope( msg, direct, root, 1, 0 );
            expectScope( msg, direct, root, 2, 0 );
            expectScope( msg, direct, root, 3, 0 );
            expectScope( msg, direct, root, 4, 0 );
        }
    }
}
