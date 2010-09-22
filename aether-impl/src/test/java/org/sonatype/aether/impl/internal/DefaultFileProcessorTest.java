package org.sonatype.aether.impl.internal;

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
import java.io.RandomAccessFile;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sonatype.aether.impl.internal.DefaultFileProcessor;
import org.sonatype.aether.test.util.TestFileUtils;

/**
 * @author Benjamin Hanzelmann
 */
public class DefaultFileProcessorTest
{

    private File targetDir;

    private DefaultFileProcessor fileProcessor;

    @Before
    public void setup()
    {
        targetDir = new File( "target/test-FileUtils" );
        fileProcessor = new DefaultFileProcessor();
    }

    @After
    public void teardown()
    {
        TestFileUtils.deleteDir( targetDir );
        fileProcessor = null;
    }

    @Test
    public void testCopy()
        throws IOException
    {
        File file = TestFileUtils.createTempFile( "testCopy\nasdf" );
        File target = new File( targetDir, "testCopy.txt" );

        fileProcessor.copy( file, target, null );

        assertContent( file, "testCopy\nasdf".getBytes( "UTF-8" ) );

        file.delete();
    }

    private void assertContent( File file, byte[] content )
        throws IOException
    {
        RandomAccessFile in = null;
        try
        {
            in = new RandomAccessFile( file, "r" );
            byte[] buffer = new byte[(int) in.length()];
            in.readFully( buffer );
            assertArrayEquals( "content did not match", content, buffer );
        }
        finally
        {
            in.close();
        }
    }

    @Test
    public void testOverwrite()
        throws IOException
    {
        File file = TestFileUtils.createTempFile( "testCopy\nasdf" );

        for ( int i = 0; i < 5; i++ )
        {
            File target = new File( targetDir, "testCopy.txt" );
            fileProcessor.copy( file, target, null );
            assertContent( file, "testCopy\nasdf".getBytes( "UTF-8" ) );
        }

        file.delete();
    }

}
