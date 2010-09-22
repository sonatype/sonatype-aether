package org.sonatype.aether.test.impl;

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

import org.sonatype.aether.spi.io.FileProcessor;

/**
 * @author Benjamin Bentmann
 */
public class TestFileProcessor
    implements FileProcessor
{

    public static final FileProcessor INSTANCE = new TestFileProcessor();

    public boolean mkdirs( File directory )
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

    public void write( File file, String data )
        throws IOException
    {
        FileOutputStream fos = new FileOutputStream( file );
        try
        {
            if ( data != null )
            {
                fos.write( data.getBytes( "UTF-8" ) );
            }
        }
        finally
        {
            fos.close();
        }
    }

    public long copy( File source, File target, ProgressListener listener )
        throws IOException
    {
        long size = 0;

        FileInputStream fis = new FileInputStream( source );
        try
        {
            mkdirs( target.getParentFile() );

            FileOutputStream fos = new FileOutputStream( target );
            try
            {
                byte[] buffer = new byte[1024 * 16];
                while ( true )
                {
                    int bytes = fis.read( buffer );
                    if ( bytes < 0 )
                    {
                        break;
                    }
                    fos.write( buffer, 0, bytes );
                    size += bytes;

                    if ( listener != null )
                    {
                        listener.progressed( ByteBuffer.wrap( buffer, 0, bytes ) );
                    }
                }
            }
            finally
            {
                fos.close();
            }
        }
        finally
        {
            fis.close();
        }

        return size;
    }

}
