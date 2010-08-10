package org.sonatype.aether.util;

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

import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;

public class ChecksumUtilTest
{
    private static final String PREFIX = "src/test/resources/ChecksumUtilTest";

    /**
     * Generate checksum. Usage: main $datafile $checksumbasename
     */
    public static void main( String[] args )
        throws Throwable
    {

        Map<String, Object> checksums;

        File file = new File( args[0] );
        String targetBasename = args[1];

        checksums = ChecksumUtils.calc( file, Arrays.asList( "SHA-1", "MD5" ) );

        for ( Entry<String, Object> entry : checksums.entrySet() )
        {
            File target = new File( targetBasename + "." + entry.getKey() );
            FileWriter w = new FileWriter( target );
            if ( entry.getValue() instanceof Throwable )
            {
                throw (Throwable) entry.getValue();
            }

            w.write( entry.getValue().toString() );
            w.close();
        }
    }

    @Test
    public void testEquality()
        throws Throwable
    {
        Map<String, Object> checksums = null;

        File[] list = new File( PREFIX, "data" ).listFiles();
        for ( File file : list )
        {
            checksums = ChecksumUtils.calc( file, Arrays.asList( "SHA-1", "MD5" ) );

            File checksumDir = new File( PREFIX, "checksums" );
            for ( Entry<String, Object> entry : checksums.entrySet() )
            {
                if ( entry.getValue() instanceof Throwable )
                {
                    throw (Throwable) entry.getValue();
                }
                String actual = entry.getValue().toString();
                String expected = ChecksumUtils.read( new File( checksumDir, file.getName() + "." + entry.getKey() ) );
                assertEquals( String.format( "checksums do not match for '%s', algorithm '%s'", file.getName(),
                                             entry.getKey() ), expected, actual );
            }

        }

    }
}
