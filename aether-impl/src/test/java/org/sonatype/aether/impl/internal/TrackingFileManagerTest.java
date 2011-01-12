package org.sonatype.aether.impl.internal;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import static org.junit.Assert.*;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.Test;
import org.sonatype.aether.test.util.TestFileUtils;

/**
 */
public class TrackingFileManagerTest
{

    @Test
    public void testRead()
        throws Exception
    {
        TrackingFileManager tfm = new TrackingFileManager();

        File propFile = TestFileUtils.createTempFile( "#COMMENT\nkey1=value1\nkey2 : value2" );
        Properties props = tfm.read( propFile );

        assertNotNull( props );
        assertEquals( String.valueOf( props ), 2, props.size() );
        assertEquals( "value1", props.get( "key1" ) );
        assertEquals( "value2", props.get( "key2" ) );

        assertTrue( "Leaked file: " + propFile, propFile.delete() );

        props = tfm.read( propFile );
        assertNull( String.valueOf( props ), props );
    }

    @Test
    public void testReadNoFileLeak()
        throws Exception
    {
        TrackingFileManager tfm = new TrackingFileManager();

        for ( int i = 0; i < 1000; i++ )
        {
            File propFile = TestFileUtils.createTempFile( "#COMMENT\nkey1=value1\nkey2 : value2" );
            assertNotNull( tfm.read( propFile ) );
            assertTrue( "Leaked file: " + propFile, propFile.delete() );
        }
    }

    @Test
    public void testUpdate()
        throws Exception
    {
        TrackingFileManager tfm = new TrackingFileManager();

        // NOTE: The excessive repetitions are to check the update properly truncates the file
        File propFile = TestFileUtils.createTempFile( "key1=value1\nkey2 : value2\n".getBytes( "UTF-8" ), 1000 );

        Map<String, String> updates = new HashMap<String, String>();
        updates.put( "key1", "v" );
        updates.put( "key2", null );

        tfm.update( propFile, updates );

        Properties props = tfm.read( propFile );

        assertNotNull( props );
        assertEquals( String.valueOf( props ), 1, props.size() );
        assertEquals( "v", props.get( "key1" ) );
        assertNull( String.valueOf( props.get( "key2" ) ), props.get( "key2" ) );
    }

    @Test
    public void testUpdateNoFileLeak()
        throws Exception
    {
        TrackingFileManager tfm = new TrackingFileManager();

        Map<String, String> updates = new HashMap<String, String>();
        updates.put( "k", "v" );

        for ( int i = 0; i < 1000; i++ )
        {
            File propFile = TestFileUtils.createTempFile( "#COMMENT\nkey1=value1\nkey2 : value2" );
            assertNotNull( tfm.update( propFile, updates ) );
            assertTrue( "Leaked file: " + propFile, propFile.delete() );
        }
    }

}
