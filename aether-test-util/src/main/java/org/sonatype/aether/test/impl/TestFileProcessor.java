package org.sonatype.aether.test.impl;

/*******************************************************************************
 * Copyright (c) 2010 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

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

    public void move( File source, File target )
        throws IOException
    {
        target.delete();

        if ( !source.renameTo( target ) )
        {
            copy( source, target, null );

            source.delete();
        }
    }

}
