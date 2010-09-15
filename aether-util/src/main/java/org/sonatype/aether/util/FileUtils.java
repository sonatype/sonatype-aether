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

    /**
     * Thread-safe variant of {@link File#mkdirs()}. Adapted from Java 6. Creates the directory named by the given
     * abstract pathname, including any necessary but nonexistent parent directories. Note that if this operation fails
     * it may have succeeded in creating some of the necessary parent directories.
     * 
     * @param dir the directory to create.
     * @return <code>true</code> if and only if the directory was created, along with all necessary parent directories;
     *         <code>false</code> otherwise
     */
    public static boolean mkdirs( File dir )
    {
        if ( dir.exists() )
        {
            return false;
        }
        if ( dir.mkdir() )
        {
            return true;
        }

        File canonFile = null;
        try
        {
            canonFile = dir.getCanonicalFile();
        }
        catch ( IOException e )
        {
            return false;
        }

        File parent = canonFile.getParentFile();
        return ( parent != null && ( mkdirs( parent ) || parent.exists() ) && canonFile.mkdir() );
    }

    /**
     * Lock and copy src- to target-file.
     * 
     * @param src the file to copy from.
     * @param target the file to copy to.
     * @return the number of copied bytes.
     * @throws IOException if an I/O error occurs.
     */
    public static long copy( File src, File target )
        throws IOException
    {
        RandomAccessFile in = new RandomAccessFile( src, "r" );
        RandomAccessFile out = new RandomAccessFile( target, "rw" );
        try
        {
            return copy( in.getChannel(), out.getChannel() );
        }
        finally
        {
            in.close();
            out.close();
        }
    }

    /**
     * Lock and copy src- to target-channel.
     * 
     * @param src the channel to copy from.
     * @param target the channel to copy to.
     * @return the number of copied bytes.
     * @throws IOException if an I/O error occurs.
     */
    public static long copy( FileChannel in, FileChannel out )
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
     * @param src the channel to copy from.
     * @param target the channel to copy to.
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
            if ( src != null )
            {
                src.close();
            }
            if ( target != null )
            {
                target.close();
            }
        }

        return total;
    }

    /**
     * Write the given data to a file. UTF-8 is assumed as encoding for the data.
     * 
     * @param fileName the file to write to. This file will be truncated.
     * @param data the data to write.
     * @throws IOException if an I/O error occurs.
     */
    public static void write( String fileName, String data )
        throws IOException
    {
        write( fileName, null, data );
    }

    /**
     * Write the given data to a file. If encoding is {@code null}, UTF-8 is assumed.
     * 
     * @param fileName the file to write to. This file will be truncated.
     * @param encoding the encoding to use to convert the given data into binary format. May be {@code null}.
     * @param data the data to write.
     * @throws IOException if an I/O error occurs.
     */
    public static void write( String fileName, String encoding, String data )
        throws IOException
    {
        if ( encoding == null )
        {
            encoding = "UTF-8";
        }

        File f = new File( fileName );

        FileOutputStream out = null;
        try
        {
            out = new FileOutputStream( f );
            out.write( data.getBytes( encoding ) );
            out.flush();
        }
        finally
        {
            if ( out != null )
            {
                out.close();
            }
        }

    }

}
