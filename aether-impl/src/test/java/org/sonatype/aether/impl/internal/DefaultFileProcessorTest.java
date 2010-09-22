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
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sonatype.aether.impl.internal.DefaultFileProcessor;
import org.sonatype.aether.test.util.TestFileUtils;

import edu.umd.cs.mtc.MultithreadedTestCase;
import edu.umd.cs.mtc.TestFramework;

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

    @SuppressWarnings( "unused" )
    @Test
    public void testBlockingCopyExistingWriteLockOnSrc()
        throws Throwable
    {
        TestFramework.runOnce( new MultithreadedTestCase()
        {
            private File locked;

            private File unlocked;

            private ReentrantReadWriteLock lock;

            private WriteLock writeLock;

            public void thread1()
            {
                writeLock = lock.writeLock();
                writeLock.lock();
                waitForTick( 2 );
                writeLock.unlock();
            }

            public void thread2()
            {
                waitForTick( 1 );

                try
                {
                    fileProcessor.copy( locked, unlocked, null );
                }
                catch ( IOException e )
                {
                    e.printStackTrace();
                    fail( "testBlockingCopy failed (write lock on src file): " + e.getMessage() );
                }

                assertTick( 2 );
            }

            @Override
            public void initialize()
            {
                lock = new ReentrantReadWriteLock( true );
                try
                {
                    locked = TestFileUtils.createTempFile( "some content" );
                    unlocked = TestFileUtils.createTempFile( "" );
                }
                catch ( IOException e )
                {
                    e.printStackTrace();
                }
                fileProcessor.locks.put( locked, lock );
            }
        } );
    }

    @SuppressWarnings( "unused" )
    @Test
    public void testBlockingCopyExistingWriteLockOnTarget()
        throws Throwable
    {
        TestFramework.runOnce( new MultithreadedTestCase()
        {
            private File locked;

            private File unlocked;

            private ReentrantReadWriteLock lock;

            private WriteLock writeLock;

            public void thread1()
            {
                writeLock = lock.writeLock();
                writeLock.lock();
                waitForTick( 2 );
                writeLock.unlock();
            }

            public void thread2()
            {
                waitForTick( 1 );

                try
                {
                    fileProcessor.copy( unlocked, locked, null );
                }
                catch ( IOException e )
                {
                    e.printStackTrace();
                    fail( "testBlockingCopy failed (write lock on src file): " + e.getMessage() );
                }

                assertTick( 2 );
            }

            @Override
            public void initialize()
            {
                lock = new ReentrantReadWriteLock( true );
                try
                {
                    locked = TestFileUtils.createTempFile( "some content" );
                    unlocked = TestFileUtils.createTempFile( "" );
                }
                catch ( IOException e )
                {
                    e.printStackTrace();
                }
                fileProcessor.locks.put( locked, lock );
            }
        } );

    }

    @SuppressWarnings( "unused" )
    @Test
    public void testBlockingCopyExistingReadLockOnTarget()
        throws Throwable
    {
        TestFramework.runOnce( new MultithreadedTestCase()
        {
            private File locked;

            private File unlocked;

            private ReentrantReadWriteLock lock;

            private ReadLock readLock;

            public void thread1()
            {
                readLock = lock.readLock();
                readLock.lock();
                waitForTick( 2 );
                readLock.unlock();
            }

            public void thread2()
            {
                waitForTick( 1 );

                try
                {
                    fileProcessor.copy( unlocked, locked, null );
                }
                catch ( IOException e )
                {
                    e.printStackTrace();
                    fail( "testBlockingCopy failed (write lock on src file): " + e.getMessage() );
                }

                assertTick( 2 );
            }

            @Override
            public void initialize()
            {
                lock = new ReentrantReadWriteLock( true );
                try
                {
                    locked = TestFileUtils.createTempFile( "some content" );
                    unlocked = TestFileUtils.createTempFile( "" );
                }
                catch ( IOException e )
                {
                    e.printStackTrace();
                }
                fileProcessor.locks.put( locked, lock );
            }
        } );
    }

    @SuppressWarnings( "unused" )
    @Test
    public void testDoNotBlockExistingReadLockOnSrc()
        throws Throwable
    {
        TestFramework.runOnce( new MultithreadedTestCase()
        {
            private File locked;

            private File unlocked;

            private ReentrantReadWriteLock lock;

            private ReadLock readLock;

            public void thread1()
            {
                readLock = lock.readLock();
                readLock.lock();
                waitForTick( 2 );
                readLock.unlock();
            }

            public void thread2()
            {
                waitForTick( 1 );

                try
                {
                    fileProcessor.copy( locked, unlocked, null );
                }
                catch ( IOException e )
                {
                    e.printStackTrace();
                    fail( "testBlockingCopy failed (write lock on src file): " + e.getMessage() );
                }

                assertTick( 1 );
                waitForTick( 2 );
            }

            @Override
            public void initialize()
            {
                lock = new ReentrantReadWriteLock( true );
                try
                {
                    locked = TestFileUtils.createTempFile( "some content" );
                    unlocked = TestFileUtils.createTempFile( "" );
                }
                catch ( IOException e )
                {
                    e.printStackTrace();
                }
                fileProcessor.locks.put( locked, lock );
            }
        } );
    }

}
