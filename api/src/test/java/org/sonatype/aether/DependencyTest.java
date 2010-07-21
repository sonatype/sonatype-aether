package org.sonatype.aether;

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

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

/**
 * @author Benjamin Bentmann
 */
public class DependencyTest
{

    @Test
    public void testSetScope()
    {
        Dependency d1 = new Dependency( new DefaultArtifact( "gid:aid:ver" ), "compile" );

        Dependency d2 = d1.setScope( null );
        assertNotSame( d2, d1 );
        assertEquals( "", d2.getScope() );

        Dependency d3 = d1.setScope( "test" );
        assertNotSame( d3, d1 );
        assertEquals( "test", d3.getScope() );
    }

    @Test
    public void testSetExclusions()
    {
        Dependency d1 =
            new Dependency( new DefaultArtifact( "gid:aid:ver" ), "compile", false,
                            Collections.singleton( new Exclusion( "g", "a", "c", "e" ) ) );

        Dependency d2 = d1.setExclusions( null );
        assertNotSame( d2, d1 );
        assertEquals( 0, d2.getExclusions().size() );

        Dependency d3 =
            d1.setExclusions( Arrays.asList( new Exclusion( "g", "a", "c", "e" ), new Exclusion( "g", "a", "c", "e" ) ) );
        assertNotSame( d3, d1 );
        assertEquals( 2, d3.getExclusions().size() );
    }

}
