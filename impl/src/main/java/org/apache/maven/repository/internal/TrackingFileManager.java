package org.apache.maven.repository.internal;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

import org.apache.maven.repository.spi.Logger;

/**
 * @author Benjamin Bentmann
 */
class TrackingFileManager
{

    private final Logger logger;

    public TrackingFileManager( Logger logger )
    {
        this.logger = logger;
    }

    public Properties read( File file )
    {
        Properties props = new Properties();

        if ( !file.canRead() )
        {
            logger.debug( "Skipped unreadable resolution tracking file " + file );
            return props;
        }

        synchronized ( file.getAbsolutePath().intern() )
        {
            FileLock lock = null;
            FileChannel channel = null;
            try
            {
                FileInputStream stream = new FileInputStream( file );
                channel = new FileInputStream( file ).getChannel();
                lock = channel.lock( 0, channel.size(), true );

                logger.debug( "Reading resolution tracking file " + file );
                props.load( stream );
            }
            catch ( IOException e )
            {
                logger.debug( "Failed to read resolution tracking file " + file, e );
            }
            finally
            {
                release( lock, file );
                close( channel, file );
            }
        }

        return props;
    }

    public Properties update( File file, Map<String, String> updates )
    {
        Properties props = new Properties();

        synchronized ( file.getAbsolutePath().intern() )
        {
            File directory = file.getParentFile();
            if ( !directory.exists() && !directory.mkdirs() )
            {
                logger.debug( "Failed to create parent directories for resolution tracking file " + file );
                return props;
            }

            FileChannel channel = null;
            FileLock lock = null;
            try
            {
                channel = new RandomAccessFile( file, "rw" ).getChannel();
                lock = channel.lock( 0, channel.size(), false );

                if ( file.canRead() )
                {
                    logger.debug( "Reading resolution tracking file " + file );
                    ByteBuffer buffer = ByteBuffer.allocate( (int) channel.size() );

                    channel.read( buffer );
                    buffer.flip();

                    ByteArrayInputStream stream = new ByteArrayInputStream( buffer.array() );
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
                props.store( stream, "Last modified on: " + new Date() );

                byte[] data = stream.toByteArray();
                ByteBuffer buffer = ByteBuffer.allocate( data.length );
                buffer.put( data );
                buffer.flip();

                channel.position( 0 );
                channel.write( buffer );
            }
            catch ( IOException e )
            {
                logger.debug( "Failed to write resolution tracking file " + file, e );
            }
            finally
            {
                release( lock, file );
                close( channel, file );
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

    private void close( FileChannel channel, File file )
    {
        if ( channel != null )
        {
            try
            {
                channel.close();
            }
            catch ( IOException e )
            {
                logger.debug( "Error closing file channel for resolution tracking file " + file, e );
            }
        }
    }

}
