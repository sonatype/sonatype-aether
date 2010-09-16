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

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.WritableByteChannel;

/**
 * A utility class helping with file-based operations.
 * 
 * @author Benjamin Hanzelmann
 */
public class FileUtils
{

    private FileUtils()
    {
        // hide constructor
    }

    private static void close( Closeable closeable )
    {
        if ( closeable != null )
        {
            try
            {
                closeable.close();
            }
            catch ( IOException e )
            {
                // too bad but who cares
            }
        }
    }

    private static void close( RandomAccessFile closeable )
    {
        if ( closeable != null )
        {
            try
            {
                closeable.close();
            }
            catch ( IOException e )
            {
                // too bad but who cares
            }
        }
    }

    /**
     * Thread-safe variant of {@link File#mkdirs()}. Adapted from Java 6. Creates the directory named by the given
     * abstract pathname, including any necessary but nonexistent parent directories. Note that if this operation fails
     * it may have succeeded in creating some of the necessary parent directories.
     * 
     * @param directory The directory to create, may be {@code null}.
     * @return {@code true} if and only if the directory was created, along with all necessary parent directories;
     *         {@code false} otherwise
     */
    public static boolean mkdirs( File directory )
    {
        if ( directory == null )
        {
            return false;
        }

        if ( directory.exists() )
        {
            return false;
        }
        if ( directory.mkdir() )
        {
            return true;
        }

        File canonDir = null;
        try
        {
            canonDir = directory.getCanonicalFile();
        }
        catch ( IOException e )
        {
            return false;
        }

        File parentDir = canonDir.getParentFile();
        return ( parentDir != null && ( mkdirs( parentDir ) || parentDir.exists() ) && canonDir.mkdir() );
    }

    /**
     * Lock and copy src- to target-file. Creates the necessary directories for the target file. In case of an error,
     * the created directories will be left on the file system.
     * 
     * @param src the file to copy from, must not be {@code null}.
     * @param target the file to copy to, must not be {@code null}.
     * @return the number of copied bytes.
     * @throws IOException if an I/O error occurs.
     */
    public static long copy( File src, File target )
        throws IOException
    {
        RandomAccessFile in = null;
        RandomAccessFile out = null;
        try
        {
            in = new RandomAccessFile( src, "r" );

            File targetDir = target.getParentFile();
            if ( targetDir != null )
            {
                mkdirs( targetDir );
            }

            out = new RandomAccessFile( target, "rw" );
            out.setLength( 0 );
            return copy( in.getChannel(), out.getChannel() );
        }
        finally
        {
            close( in );
            close( out );
        }
    }

    /**
     * Lock and copy src- to target-channel.
     * 
     * @param in the channel to copy from, must not be {@code null}.
     * @param out the channel to copy to, must not be {@code null}.
     * @return the number of copied bytes.
     * @throws IOException if an I/O error occurs.
     */
    private static long copy( FileChannel in, FileChannel out )
        throws IOException
    {
        FileLock lock = null;
        try
        {
            lock = out.lock();
            return copy( in, (WritableByteChannel) out );
        }
        finally
        {
            if ( lock != null )
            {
                try
                {
                    lock.release();
                }
                catch ( IOException e )
                {
                    // tried everything
                }
            }
        }
    }

    /**
     * Copy src- to target-channel.
     * 
     * @param src the channel to copy from, must not be {@code null}.
     * @param target the channel to copy to, must not be {@code null}.
     * @return the number of copied bytes.
     * @throws IOException if an I/O error occurs.
     */
    public static long copy( FileChannel src, WritableByteChannel target )
        throws IOException
    {
        long total = 0;
        try
        {
            while ( ( total += ( src.transferTo( total, src.size(), target ) ) ) < src.size() )
            {
                // copy all
            }
        }
        finally
        {
            close( src );
            close( target );
        }

        return total;
    }

    /**
     * Write the given data to a file. UTF-8 is assumed as encoding for the data.
     * 
     * @param file The file to write to, must not be {@code null}. This file will be truncated.
     * @param data The data to write, may be {@code null}.
     * @throws IOException if an I/O error occurs.
     */
    public static void write( File file, String data )
        throws IOException
    {
        FileOutputStream out = null;
        try
        {
            out = new FileOutputStream( file );
            if ( data != null )
            {
                out.write( data.getBytes( "UTF-8" ) );
            }
            out.flush();
        }
        finally
        {
            close( out );
        }
    }

}
