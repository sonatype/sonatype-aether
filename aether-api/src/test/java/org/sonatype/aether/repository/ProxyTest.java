package org.sonatype.aether.repository;

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

/**
 * @author Benjamin Bentmann
 */
public class ProxyTest
{

    @Test
    public void testSetHost()
    {
        Proxy p1 = new Proxy( "type", "host", 80, null );

        Proxy p2 = p1.setHost( null );
        assertNotSame( p2, p1 );
        assertEquals( "", p2.getHost() );

        Proxy p3 = p1.setHost( "HOST" );
        assertNotSame( p3, p1 );
        assertEquals( "HOST", p3.getHost() );
    }

    @Test
    public void testSetType()
    {
        Proxy p1 = new Proxy( "type", "host", 80, null );

        Proxy p2 = p1.setType( null );
        assertNotSame( p2, p1 );
        assertEquals( "", p2.getType() );

        Proxy p3 = p1.setType( "TYPE" );
        assertNotSame( p3, p1 );
        assertEquals( "TYPE", p3.getType() );
    }

    @Test
    public void testSetPort()
    {
        Proxy p1 = new Proxy( "type", "host", 80, null );

        Proxy p2 = p1.setPort( 8080 );
        assertNotSame( p2, p1 );
        assertEquals( 8080, p2.getPort() );
    }

}
