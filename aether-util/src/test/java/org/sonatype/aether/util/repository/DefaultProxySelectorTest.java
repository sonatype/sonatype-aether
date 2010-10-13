package org.sonatype.aether.util.repository;

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
public class DefaultProxySelectorTest
{

    private boolean isNonProxyHosts( String host, String nonProxyHosts )
    {
        return DefaultProxySelector.isNonProxyHosts( host, nonProxyHosts );
    }

    @Test
    public void testIsNonProxyHosts()
    {
        assertFalse( isNonProxyHosts( "www.sonatype.org", null ) );
        assertFalse( isNonProxyHosts( "www.sonatype.org", "" ) );

        assertTrue( isNonProxyHosts( "www.sonatype.org", "*" ) );
        assertTrue( isNonProxyHosts( "www.sonatype.org", "*.org" ) );
        assertTrue( isNonProxyHosts( "www.sonatype.org", "www.*" ) );
        assertTrue( isNonProxyHosts( "www.sonatype.org", "www.*.org" ) );

        assertFalse( isNonProxyHosts( "www.sonatype.org", "www.sonatype.com" ) );
        assertFalse( isNonProxyHosts( "www.sonatype.org", "*.com" ) );
        assertFalse( isNonProxyHosts( "www.sonatype.org", "sonatype.org" ) );

        assertTrue( isNonProxyHosts( "www.sonatype.org", "*.com|*.org" ) );
    }

}
