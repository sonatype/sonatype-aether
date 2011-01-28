package org.sonatype.aether.impl.internal;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *******************************************************************************/

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.util.Map;
import java.util.Properties;

import org.sonatype.aether.spi.log.Logger;
import org.sonatype.aether.spi.log.NullLogger;

/**
 * Manages potentially concurrent accesses to a properties file.
 * 
 * @author Benjamin Bentmann
 */
class TrackingFileManager
{

    private Logger logger = NullLogger.INSTANCE;

    public TrackingFileManager setLogger( Logger logger )
    {
        this.logger = ( logger != null ) ? logger : NullLogger.INSTANCE;
        return this;
    }

    public Properties read( File file )
    {
        synchronized ( getLock( file ) )
        {
            FileLock lock = null;
            FileInputStream stream = null;
            try
            {
                stream = new FileInputStream( file );

                lock = stream.getChannel().lock( 0, Math.max( 1, file.length() ), true );

                Properties props = new Properties();
                props.load( stream );

                return props;
            }
            catch ( FileNotFoundException e )
            {
                // not unusual
            }
            catch ( IOException e )
            {
                logger.debug( "Failed to read resolution tracking file " + file, e );
            }
            finally
            {
                release( lock, file );
                close( stream, file );
            }
        }

        return null;
    }

    public Properties update( File file, Map<String, String> updates )
    {
        Properties props = new Properties();

        synchronized ( getLock( file ) )
        {
            File directory = file.getParentFile();
            if ( !directory.exists() && !directory.mkdirs() )
            {
                logger.debug( "Failed to create parent directories for resolution tracking file " + file );
                return props;
            }

            RandomAccessFile raf = null;
            FileLock lock = null;
            try
            {
                raf = new RandomAccessFile( file, "rw" );
                lock = raf.getChannel().lock( 0, Math.max( 1, raf.length() ), false );

                if ( file.canRead() )
                {
                    logger.debug( "Reading resolution tracking file " + file );

                    byte[] buffer = new byte[(int) raf.length()];

                    raf.readFully( buffer );

                    ByteArrayInputStream stream = new ByteArrayInputStream( buffer );

                    props.load( stream );
                }

                for ( Map.Entry<String, String> update : updates.entrySet() )
                {
                    if ( update.getValue() == null )
                    {
                        props.remove( update.getKey() );
                    }
                    else
                    {
                        props.setProperty( update.getKey(), update.getValue() );
                    }
                }

                ByteArrayOutputStream stream = new ByteArrayOutputStream( 1024 * 2 );

                logger.debug( "Writing resolution tracking file " + file );
                props.store( stream,
                             "NOTE: This is an internal implementation file, its format can be changed without prior notice." );

                raf.seek( 0 );
                raf.write( stream.toByteArray() );
                raf.setLength( raf.getFilePointer() );
            }
            catch ( IOException e )
            {
                logger.debug( "Failed to write resolution tracking file " + file, e );
            }
            finally
            {
                release( lock, file );
                close( raf, file );
            }
        }

        return props;
    }

    private void release( FileLock lock, File file )
    {
        if ( lock != null )
        {
            try
            {
                lock.release();
            }
            catch ( IOException e )
            {
                logger.debug( "Error releasing lock for resolution tracking file " + file, e );
            }
        }
    }

    private void close( Closeable closeable, File file )
    {
        if ( closeable != null )
        {
            try
            {
                closeable.close();
            }
            catch ( IOException e )
            {
                logger.debug( "Error closing resolution tracking file " + file, e );
            }
        }
    }

    private Object getLock( File file )
    {
        return file.getAbsolutePath().intern();
    }

}
