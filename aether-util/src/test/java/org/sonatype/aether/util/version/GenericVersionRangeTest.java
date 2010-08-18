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
import org.sonatype.aether.InvalidVersionRangeException;

public class GenericVersionRangeTest
{
    private static String[] invalidRanges = { "1.0", "[1.0", "(1.0", "1.0]", "1.0)", "[1,2,3]", "(1,2,3)", "[1,2,3)",
        "[2,1]", "1,2", "[1,1]", "(1)" };

    private static String[] validRanges = { "[1,2]", "(1.2.3.4.5,1.2.3.4.6)", "[1a,1b]", "[1]" };

    // TODO: multiple range sets are parsed somewhere else? works in POM, but not in unit test
    // "(,1.0],[1.2,)", "(,1.1),(1.1,)"

    @Test
    public void testValidRanges()
    {
        for ( String range : validRanges )
        {
            try
            {
                new GenericVersionRange( range );
            }
            catch ( InvalidVersionRangeException e )
            {
                fail( range + " should be valid." );
            }
        }
    }

    @Test
    public void testInvalidRanges()
    {
        for ( String range : invalidRanges )
        {
            try
            {
                GenericVersionRange mvRange = new GenericVersionRange( range );
                System.err.println( String.format( "lower bound: %s\nupper bound: %s\n", mvRange.getLowerBound(),
                                                   mvRange.getUpperBound() ) );
                fail( range + " should be invalid." );
            }
            catch ( InvalidVersionRangeException e )
            {
                // expected
            }
        }
    }

}
