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

import org.junit.Test;
import org.sonatype.aether.version.InvalidVersionSpecificationException;
import org.sonatype.aether.version.Version;
import org.sonatype.aether.version.VersionRange;

public class GenericVersionRangeTest
{

    private Version newVersion( String version )
    {
        return new GenericVersion( version );
    }

    private VersionRange parseValid( String range )
    {
        try
        {
            return new GenericVersionRange( range );
        }
        catch ( InvalidVersionSpecificationException e )
        {
            AssertionError error =
                new AssertionError( range + " should be valid but failed to parse due to: " + e.getMessage() );
            error.initCause( e );
            throw error;
        }
    }

    private void parseInvalid( String range )
    {
        try
        {
            new GenericVersionRange( range );
            fail( range + " should be invalid" );
        }
        catch ( InvalidVersionSpecificationException e )
        {
            assertTrue( true );
        }
    }

    private void assertContains( VersionRange range, String version )
    {
        assertTrue( range + " should contain " + version, range.containsVersion( newVersion( version ) ) );
    }

    private void assertNotContains( VersionRange range, String version )
    {
        assertFalse( range + " should not contain " + version, range.containsVersion( newVersion( version ) ) );
    }

    @Test
    public void testLowerBoundInclusiveUpperBoundInclusive()
    {
        VersionRange range = parseValid( "[1,2]" );
        assertContains( range, "1" );
        assertContains( range, "2" );
        assertFalse( range.acceptsSnapshots() );
        assertEquals( range, parseValid( range.toString() ) );
    }

    @Test
    public void testLowerBoundInclusiveUpperBoundExclusive()
    {
        VersionRange range = parseValid( "[1.2.3.4.5,1.2.3.4.6)" );
        assertContains( range, "1.2.3.4.5" );
        assertNotContains( range, "1.2.3.4.6" );
        assertFalse( range.acceptsSnapshots() );
        assertEquals( range, parseValid( range.toString() ) );
    }

    @Test
    public void testLowerBoundExclusiveUpperBoundInclusive()
    {
        VersionRange range = parseValid( "(1a,1b]" );
        assertNotContains( range, "1a" );
        assertContains( range, "1b" );
        assertFalse( range.acceptsSnapshots() );
        assertEquals( range, parseValid( range.toString() ) );
    }

    @Test
    public void testLowerBoundExclusiveUpperBoundExclusive()
    {
        VersionRange range = parseValid( "(1,3)" );
        assertNotContains( range, "1" );
        assertNotContains( range, "3" );
        assertFalse( range.acceptsSnapshots() );
        assertEquals( range, parseValid( range.toString() ) );
    }

    @Test
    public void testSingleVersion()
    {
        VersionRange range = parseValid( "[1]" );
        assertContains( range, "1" );
        assertFalse( range.acceptsSnapshots() );
        assertEquals( range, parseValid( range.toString() ) );

        range = parseValid( "[1,1]" );
        assertContains( range, "1" );
        assertFalse( range.acceptsSnapshots() );
        assertEquals( range, parseValid( range.toString() ) );
    }

    @Test
    public void testMissingOpenCloseDelimiter()
    {
        parseInvalid( "1.0" );
    }

    @Test
    public void testMissingOpenDelimiter()
    {
        parseInvalid( "1.0]" );
        parseInvalid( "1.0)" );
    }

    @Test
    public void testMissingCloseDelimiter()
    {
        parseInvalid( "[1.0" );
        parseInvalid( "(1.0" );
    }

    @Test
    public void testTooManyVersions()
    {
        parseInvalid( "[1,2,3]" );
        parseInvalid( "(1,2,3)" );
        parseInvalid( "[1,2,3)" );
    }

}
