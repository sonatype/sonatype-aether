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

        public String toString()
        {
            return super.name().toLowerCase( Locale.ENGLISH );
        }
    }

    @Test
    public void testScopeOrderingWithConflict()
        throws RepositoryException, IOException
    {
        Scope[] scopes = Scope.values();
        for ( Scope scope1 : scopes )
        {
            for ( Scope scope2 : scopes )
            {
                parser.setSubstitutions( scope1.toString(), scope2.toString() );
                DependencyNode root = parser.parse( "ordering-compile.txt" );

                String expected = scope1.compareTo( scope2 ) > 0 ? scope1.toString() : scope2.toString();
                root = transform( root );

                List<DependencyNode> rootChildren = root.getChildren();
                assertEquals( 2, rootChildren.size() );

                List<DependencyNode> deepChildren = rootChildren.get( 1 ).getChildren();
                assertEquals( 1, deepChildren.size() );

                assertEquals( expected, rootChildren.get( 0 ).getDependency().getScope() );
                assertEquals( expected, deepChildren.get( 0 ).getDependency().getScope() );
            }
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
        expect( expected, coords, transform( parse( resource, "provided", "test" ) ) );
    }

    private void expect( String expected, int[] coords, DependencyNode root )
    {
        try
        {
            DependencyNode node = root;
            for ( int i = 0; i < coords.length; i++ )
            {
                node = node.getChildren().get( coords[i] );
            }

            assertEquals( expected, node.getDependency().getScope() );
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

}
