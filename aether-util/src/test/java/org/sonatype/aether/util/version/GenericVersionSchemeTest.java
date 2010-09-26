package org.sonatype.aether.util.version;


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

import org.junit.Before;
import org.junit.Test;
import org.sonatype.aether.version.InvalidVersionSpecificationException;
import org.sonatype.aether.version.VersionConstraint;

/**
 * @author Benjamin Hanzelmann
 */
public class GenericVersionSchemeTest
{

    private GenericVersionScheme scheme;

    @Before
    public void setUp()
        throws Exception
    {
        scheme = new GenericVersionScheme();
    }

    private InvalidVersionSpecificationException parseInvalid( String constraint )
    {
        try
        {
            VersionConstraint c = scheme.parseVersionConstraint( constraint );
            fail( "expected exception for constraint " + constraint );
            return null;
        }
        catch ( InvalidVersionSpecificationException e )
        {
            return e;
        }
    }


    @Test
    public void testEnumeratedVersions()
        throws InvalidVersionSpecificationException
    {
        VersionConstraint c = scheme.parseVersionConstraint( "1.0" );
        assertEquals( "1.0", c.getVersion().toString() );
        assertTrue( c.containsVersion( new GenericVersion( "1.0" ) ) );

        c = scheme.parseVersionConstraint( "[1.0]" );
        assertEquals( null, c.getVersion() );
        assertTrue( c.containsVersion( new GenericVersion( "1.0" ) ) );

        c = scheme.parseVersionConstraint( "[1.0],[2.0]" );
        assertTrue( c.containsVersion( new GenericVersion( "1.0" ) ) );
        assertTrue( c.containsVersion( new GenericVersion( "2.0" ) ) );

        c = scheme.parseVersionConstraint( "[1.0],[2.0],[3.0]" );
        assertContains( c, "1.0", "2.0", "3.0" );
        assertNotContains( c, "1.5" );

        c = scheme.parseVersionConstraint( "[1,3),(3,5)" );
        assertContains( c, "1", "2", "4" );
        assertNotContains( c, "3", "5" );

        c = scheme.parseVersionConstraint( "[1,3),(3,)" );
        assertContains( c, "1", "2", "4" );
        assertNotContains( c, "3" );
    }

    private void assertNotContains( VersionConstraint c, String... versions )
    {
        assertContains( String.format( "%s: %%s should not be contained\n", c.toString() ), c, false, versions );
    }

    private void assertContains( String msg, VersionConstraint c, boolean b, String... versions )
    {
        for ( String v : versions )
        {
            assertEquals( String.format( msg, v ), b, c.containsVersion( new GenericVersion( v ) ) );
        }
    }

    private void assertContains( VersionConstraint c, String... versions )
    {
        assertContains( String.format( "%s: %%s should be contained\n", c.toString() ), c, true, versions );
    }
    
    @Test
    public void testInvalid()
    {
        parseInvalid( "[1," );
        parseInvalid( "[1,2],(3," );
        parseInvalid( "[1,2],3" );
    }
}
