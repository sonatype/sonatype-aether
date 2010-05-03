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
import java.util.Properties;

import org.apache.maven.repository.Artifact;
import org.apache.maven.repository.ArtifactNotFoundException;
import org.apache.maven.repository.Logger;
import org.apache.maven.repository.MetadataNotFoundException;
import org.apache.maven.repository.RemoteRepository;

/**
 * @author Benjamin Bentmann
 */
public class TrackingLocalRepositoryManager
    extends SimpleLocalRepositoryManager
{

    public TrackingLocalRepositoryManager( File basedir )
    {
        super( basedir );
    }

    @Override
    public void addLocalArtifact( Artifact artifact )
    {
        addArtifact( artifact, "local" );
    }

    @Override
    public void addRemoteArtifact( Artifact artifact, RemoteRepository repository )
    {
        addArtifact( artifact, repository.getId() );
    }

    private void addArtifact( Artifact artifact, String repository )
    {
        String path = getPathForLocalArtifact( artifact );
        File file = new File( getBasedir(), path );
        addRepo( file, repository );
    }

    private Properties readRepos( File artifactFile )
    {
        File dir = artifactFile.getParentFile();
        File trackingFile = new File( dir, "_maven.repositories" );

        Properties props = new Properties();

        if ( trackingFile.isFile() )
        {

            synchronized ( trackingFile.getAbsolutePath().intern() )
            {
                FileInputStream stream = null;
                FileLock lock = null;
                FileChannel channel = null;
                try
                {

                    stream = new FileInputStream( trackingFile );
                    channel = stream.getChannel();
                    lock = channel.lock( 0, channel.size(), true );

                    // logger.debug( "Reading resolution-state from: " + touchFile );
                    props.load( stream );
                }
                catch ( IOException e )
                {
                    // logger.debug( "Failed to read resolution tracking file " + touchFile, e );
                }
                finally
                {
                    release( lock, trackingFile, null );
                    close( channel, trackingFile, null );
                }
            }
        }

        return props;
    }

    private void addRepo( File artifactFile, String repository )
    {
        File dir = artifactFile.getParentFile();
        File trackingFile = new File( dir, "_maven.repositories" );

        if ( !dir.exists() )
        {
            dir.mkdirs();
        }

        synchronized ( trackingFile.getAbsolutePath().intern() )
        {
            RandomAccessFile raf = null;
            FileChannel channel = null;
            FileLock lock = null;
            try
            {
                Properties props = new Properties();

                raf = new RandomAccessFile( trackingFile, "rw" );
                channel = raf.getChannel();
                lock = channel.lock( 0, channel.size(), false );

                if ( trackingFile.canRead() )
                {
                    // logger.debug( "Reading resolution-state from: " + trackingFile );
                    ByteBuffer buffer = ByteBuffer.allocate( (int) channel.size() );

                    channel.read( buffer );
                    buffer.flip();

                    ByteArrayInputStream stream = new ByteArrayInputStream( buffer.array() );
                    props.load( stream );
                }

                props.setProperty( artifactFile.getName() + '>' + repository, "" );

                ByteArrayOutputStream stream = new ByteArrayOutputStream();

                // logger.debug( "Writing resolution-state to: " + touchFile );
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
                // logger.debug( "Failed to record lastUpdated information for resolution.\nFile: " +
                // trackingFile.toString() + "; key: " + key, e );
            }
            finally
            {
                release( lock, trackingFile, null );
                close( channel, trackingFile, null );
                if ( raf != null )
                {
                    try
                    {
                        raf.close();
                    }
                    catch ( IOException e )
                    {
                        // log
                    }
                }
            }
        }
    }

    private void release( FileLock lock, File touchFile, Logger logger )
    {
        if ( lock != null )
        {
            try
            {
                lock.release();
            }
            catch ( IOException e )
            {
                logger.debug( "Error releasing exclusive lock for resolution tracking file: " + touchFile, e );
            }
        }
    }

    private void close( FileChannel channel, File touchFile, Logger logger )
    {
        if ( channel != null )
        {
            try
            {
                channel.close();
            }
            catch ( IOException e )
            {
                logger.debug( "Error closing file channel for resolution tracking file: " + touchFile, e );
            }
        }
    }

}
