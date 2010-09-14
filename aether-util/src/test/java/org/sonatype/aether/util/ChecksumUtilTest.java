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
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sonatype.aether.test.util.TestFileUtils;

public class ChecksumUtilTest
{
    private static File emptyFile;

    private static Map<String, String> emptyFileChecksums = new HashMap<String, String>();

    private static File patternFile;

    private static Map<String, String> patternFileChecksums = new HashMap<String, String>();

    private static File textFile;

    private static Map<String, String> textFileChecksums = new HashMap<String, String>();

    private static Map<File, Map<String, String>> sums = new HashMap<File, Map<String, String>>();
    
    @BeforeClass
    public static void beforeClass() {
        emptyFileChecksums.put( "MD5", "d41d8cd98f00b204e9800998ecf8427e" );
        emptyFileChecksums.put( "SHA-1", "da39a3ee5e6b4b0d3255bfef95601890afd80709" );
        patternFileChecksums.put( "MD5", "14f01d6c7de7d4cf0a4887baa3528b5a" );
        patternFileChecksums.put( "SHA-1", "feeeda19f626f9b0ef6cbf5948c1ec9531694295" );
        textFileChecksums.put( "MD5", "12582d1a662cefe3385f2113998e43ed" );
        textFileChecksums.put( "SHA-1", "a8ae272db549850eef2ff54376f8cac2770745ee" );
    }

    @Before
    public void before()
        throws IOException
    {
        emptyFile = TestFileUtils.createTempFile( new byte[] {}, 0 );
        sums.put( emptyFile, emptyFileChecksums );

        patternFile =
            TestFileUtils.createTempFile( new byte[] { 0, 1, 2, 4, 8, 16, 32, 64, 127, -1, -2, -4, -8, -16, -32, -64, -127 }, 1000 );
        sums.put( patternFile, patternFileChecksums );

        textFile = TestFileUtils.createTempFile( "the quick brown fox jumps over the lazy dog\n".getBytes( "UTF-8" ), 500 );
        sums.put( textFile, textFileChecksums );

    }

    @Test
    public void testEquality()
        throws Throwable
    {
        Map<String, Object> checksums = null;

        for ( File file : new File[] { emptyFile, patternFile, textFile } )
        {

            checksums = ChecksumUtils.calc( file, Arrays.asList( "SHA-1", "MD5" ) );

            for ( Entry<String, Object> entry : checksums.entrySet() )
            {
                if ( entry.getValue() instanceof Throwable )
                {
                    throw (Throwable) entry.getValue();
                }
                String actual = entry.getValue().toString();
                String expected = sums.get( file ).get( entry.getKey() );
                assertEquals( String.format( "checksums do not match for '%s', algorithm '%s'", file.getName(),
                                             entry.getKey() ), expected, actual );
            }
            assertTrue( "Could not delete file", file.delete() );
        }
    }

    @Test
    public void testFileHandleLeakage()
        throws IOException
    {
        for ( File file : new File[] { emptyFile, patternFile, textFile } )
        {
            for ( int i = 0; i < 150; i++ )
            {
                ChecksumUtils.calc( file, Arrays.asList( "SHA-1", "MD5" ) );
            }
            assertTrue( "Could not delete file", file.delete() );
        }

    }
}
