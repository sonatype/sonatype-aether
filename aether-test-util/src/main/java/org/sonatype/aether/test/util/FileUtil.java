package org.sonatype.aether.test.util;

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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FileUtil
{
    
    public static File createTempFile( String contents )
        throws IOException
    {
        return createTempFile( contents.getBytes("UTF-8"), 1);
    }

    public static File createTempFile( byte[] pattern, int repeat )
        throws IOException
    {

        File tmpFile = null;
        FileOutputStream out = null;
        try
        {
            tmpFile = File.createTempFile( "aether-test-util-", ".data" );
            tmpFile.deleteOnExit();

            out = new FileOutputStream( tmpFile );
            for ( int i = 0; i < repeat; i++ )
            {
                out.write( pattern );
            }
        }
        finally
        {
            if ( out != null )
            {
                out.close();
            }
        }

        return tmpFile;
    }

    public static long copy( File src, File target )
        throws IOException
    {
        if ( src == null || target == null )
        {
            throw new IllegalArgumentException( String.format( "src and target may not be null: '%s' -> '%s'", src,
                                                               target ) );
        }

        FileChannel in = null;
        FileChannel out = null;
        FileInputStream inStream = null;
        FileOutputStream outStream = null;

        long total = 0;
        try
        {
            inStream = new FileInputStream( src );
            in = inStream.getChannel();
            outStream = new FileOutputStream( target );
            out = outStream.getChannel();
            long count = 20000L;
            ByteBuffer buf = ByteBuffer.allocate( (int) count );

            buf.clear();
            int transferred;
            while ( ( transferred = in.read( buf ) ) >= 0 || buf.position() != 0 )
            {
                total += transferred;

                buf.flip();
                out.write( buf );
                buf.compact();
            }
            buf.flip();
            while ( buf.hasRemaining() )
            {
                out.write( buf );
            }
        }
        finally
        {
            if ( inStream != null )
            {
                inStream.close();
            }
            if ( in != null )
            {
                in.close();
            }
            if ( outStream != null )
            {
                outStream.close();
            }
            if ( out != null )
            {
                out.close();
            }
        }

        return total;
    }

    public static boolean deleteDir( File dir )
    {
        if ( dir.isDirectory() )
        {
            String[] children = dir.list();
            for ( int i = 0; i < children.length; i++ )
            {
                boolean success = deleteDir( new File( dir, children[i] ) );
                if ( !success )
                {
                    return false;
                }
            }
        }

        return dir.delete();
    }

}
