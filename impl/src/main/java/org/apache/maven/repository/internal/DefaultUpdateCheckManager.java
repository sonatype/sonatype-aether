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
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;

import org.apache.maven.repository.Artifact;
import org.apache.maven.repository.ArtifactNotFoundException;
import org.apache.maven.repository.ArtifactTransferException;
import org.apache.maven.repository.Authentication;
import org.apache.maven.repository.Logger;
import org.apache.maven.repository.Metadata;
import org.apache.maven.repository.MetadataNotFoundException;
import org.apache.maven.repository.MetadataTransferException;
import org.apache.maven.repository.Proxy;
import org.apache.maven.repository.RemoteRepository;
import org.apache.maven.repository.RepositoryContext;
import org.apache.maven.repository.RepositoryPolicy;
import org.apache.maven.repository.UpdateCheck;
import org.apache.maven.repository.UpdateCheckManager;

/**
 * @author Benjamin Bentmann
 * @plexus.component role="org.apache.maven.repository.UpdateCheckManager" role-hint="default"
 */
public class DefaultUpdateCheckManager
    implements UpdateCheckManager
{

    private static final String UPDATED_KEY_SUFFIX = ".lastUpdated";

    private static final String ERROR_KEY_SUFFIX = ".error";

    public String getEffectiveUpdatePolicy( RepositoryContext context, String policy1, String policy2 )
    {
        return ordinalOfUpdatePolicy( policy1 ) < ordinalOfUpdatePolicy( policy2 ) ? policy1 : policy2;
    }

    private int ordinalOfUpdatePolicy( String policy )
    {
        if ( RepositoryPolicy.UPDATE_POLICY_DAILY.equals( policy ) )
        {
            return 1440;
        }
        else if ( RepositoryPolicy.UPDATE_POLICY_ALWAYS.equals( policy ) )
        {
            return 0;
        }
        else if ( policy != null && policy.startsWith( RepositoryPolicy.UPDATE_POLICY_INTERVAL ) )
        {
            String s = policy.substring( RepositoryPolicy.UPDATE_POLICY_INTERVAL.length() + 1 );
            return Integer.valueOf( s );
        }
        else
        {
            // assume "never"
            return Integer.MAX_VALUE;
        }
    }

    public void checkArtifact( UpdateCheck<Artifact, ArtifactTransferException> check )
    {
        if ( check.getLocalLastUpdated() != 0 && !isUpdatedRequired( check.getLocalLastUpdated(), check.getPolicy() ) )
        {
            check.setRequired( false );
            return;
        }

        Artifact artifact = check.getItem();
        RemoteRepository repository = check.getRepository();

        Logger logger = check.getContext().getLogger();

        File touchFile = getTouchFile( artifact, check.getFile() );
        Properties props = read( touchFile, logger );

        String key = getRepoKey( repository );

        long lastUpdated =
            check.getFile().exists() ? check.getFile().lastModified() : getLastUpdated( props, key, logger );

        if ( lastUpdated == 0 )
        {
            check.setRequired( true );
        }
        else if ( isUpdatedRequired( lastUpdated, check.getPolicy() ) )
        {
            check.setRequired( true );
        }
        else if ( check.getFile().exists() )
        {
            check.setRequired( false );
        }
        else
        {
            String error = getError( props, key );
            if ( error == null )
            {
                if ( check.getContext().isNotFoundCachingEnabled() )
                {
                    check.setRequired( false );
                    check.setException( new ArtifactNotFoundException( artifact, "Failure to find " + artifact + " in "
                        + repository.getUrl() + " was cached in the local repository. "
                        + "Resolution will not be reattempted until the update interval of " + repository.getId()
                        + " has elapsed or updates are forced." ) );
                }
                else
                {
                    check.setRequired( true );
                }
            }
            else
            {
                if ( check.getContext().isTransferErrorCachingEnabled() )
                {
                    check.setRequired( false );
                    check.setException( new ArtifactTransferException( artifact, "Failure to transfer " + artifact
                        + " from " + repository.getUrl() + " was cached in the local repository. "
                        + "Resolution will not be reattempted until the update interval of " + repository.getId()
                        + " has elapsed or updates are forced. Original error: " + error ) );
                }
                else
                {
                    check.setRequired( true );
                }
            }
        }
    }

    public void checkMetadata( UpdateCheck<Metadata, MetadataTransferException> check )
    {
        if ( check.getLocalLastUpdated() != 0 && !isUpdatedRequired( check.getLocalLastUpdated(), check.getPolicy() ) )
        {
            check.setRequired( false );
            return;
        }

        Metadata metadata = check.getItem();
        RemoteRepository repository = check.getRepository();

        Logger logger = check.getContext().getLogger();

        File touchFile = getTouchFile( check.getItem(), check.getFile() );
        Properties props = read( touchFile, logger );

        String key = getRepoKey( repository ) + '.' + metadata.getType();

        long lastUpdated = getLastUpdated( props, key, logger );

        if ( lastUpdated == 0 )
        {
            check.setRequired( true );
        }
        else if ( isUpdatedRequired( lastUpdated, check.getPolicy() ) )
        {
            check.setRequired( true );
        }
        else if ( check.getFile().exists() )
        {
            check.setRequired( false );
        }
        else
        {
            String error = getError( props, key );
            if ( error == null )
            {
                if ( check.getContext().isNotFoundCachingEnabled() )
                {
                    check.setRequired( false );
                    check.setException( new MetadataNotFoundException( metadata, "Failure to find " + metadata + " in "
                        + repository.getUrl() + " was cached in the local repository. "
                        + "Resolution will not be reattempted until the update interval of " + repository.getId()
                        + " has elapsed or updates are forced." ) );
                }
                else
                {
                    check.setRequired( true );
                }
            }
            else
            {
                if ( check.getContext().isTransferErrorCachingEnabled() )
                {
                    check.setRequired( false );
                    check.setException( new MetadataTransferException( metadata, "Failure to transfer " + metadata
                        + " from " + repository.getUrl() + " was cached in the local repository. "
                        + "Resolution will not be reattempted until the update interval of " + repository.getId()
                        + " has elapsed or updates are forced. Original error: " + error ) );
                }
                else
                {
                    check.setRequired( true );
                }
            }
        }
    }

    private long getLastUpdated( Properties props, String key, Logger logger )
    {
        String value = props.getProperty( key + UPDATED_KEY_SUFFIX, "" );
        try
        {
            return Long.parseLong( value );
        }
        catch ( NumberFormatException e )
        {
            logger.debug( "Cannot parse lastUpdated date: \'" + value + "\'. Ignoring.", e );
            return 0;
        }
    }

    private String getError( Properties props, String key )
    {
        return props.getProperty( key + ERROR_KEY_SUFFIX );
    }

    private File getTouchFile( Artifact artifact, File artifactFile )
    {
        return new File( artifactFile.getPath() + ".lastUpdated" );
    }

    private File getTouchFile( Metadata metadata, File metadataFile )
    {
        return new File( metadataFile.getParent(), "resolver-status.properties" );
    }

    private String getRepoKey( RemoteRepository repository )
    {
        StringBuilder buffer = new StringBuilder( 128 );

        Proxy proxy = repository.getProxy();
        if ( proxy != null )
        {
            appendAuth( buffer, proxy.getAuthentication() );
            buffer.append( proxy.getHost() ).append( ':' ).append( proxy.getPort() ).append( '>' );
        }

        Authentication auth = repository.getAuthentication();
        appendAuth( buffer, auth );

        buffer.append( repository.getUrl() );

        return buffer.toString();
    }

    private void appendAuth( StringBuilder buffer, Authentication auth )
    {
        if ( auth != null )
        {
            String infos = auth.getUsername() + auth.getPassword() + auth.getPrivateKeyFile() + auth.getPassphrase();
            if ( infos.length() > 0 )
            {
                buffer.append( infos.hashCode() ).append( '@' );
            }
        }
    }

    private boolean isUpdatedRequired( long lastModified, String policy )
    {
        boolean checkForUpdates;

        if ( RepositoryPolicy.UPDATE_POLICY_ALWAYS.equals( policy ) )
        {
            checkForUpdates = true;
        }
        else if ( RepositoryPolicy.UPDATE_POLICY_DAILY.equals( policy ) )
        {
            // Get midnight boundary
            Calendar cal = Calendar.getInstance( TimeZone.getTimeZone( "UTC" ) );
            cal.set( Calendar.HOUR_OF_DAY, 0 );
            cal.set( Calendar.MINUTE, 0 );
            cal.set( Calendar.SECOND, 0 );
            cal.set( Calendar.MILLISECOND, 0 );

            checkForUpdates = cal.getTimeInMillis() > lastModified;
        }
        else if ( policy.startsWith( RepositoryPolicy.UPDATE_POLICY_INTERVAL ) )
        {
            String s = policy.substring( RepositoryPolicy.UPDATE_POLICY_INTERVAL.length() + 1 );
            int minutes = Integer.valueOf( s );

            Calendar cal = Calendar.getInstance();
            cal.add( Calendar.MINUTE, -minutes );

            checkForUpdates = cal.getTimeInMillis() > lastModified;
        }
        else
        {
            // assume "never"
            checkForUpdates = false;
        }

        return checkForUpdates;
    }

    private Properties read( File touchFile, Logger logger )
    {
        if ( !touchFile.canRead() )
        {
            logger.debug( "Skipped unreadable resolution tracking file " + touchFile );
            return null;
        }

        Properties props = new Properties();

        synchronized ( touchFile.getAbsolutePath().intern() )
        {

            FileInputStream stream = null;
            FileLock lock = null;
            FileChannel channel = null;
            try
            {

                stream = new FileInputStream( touchFile );
                channel = stream.getChannel();
                lock = channel.lock( 0, channel.size(), true );

                logger.debug( "Reading resolution-state from: " + touchFile );
                props.load( stream );
            }
            catch ( IOException e )
            {
                logger.debug( "Failed to read resolution tracking file " + touchFile, e );
            }
            finally
            {
                release( lock, touchFile, logger );
                close( channel, touchFile, logger );
            }
        }

        return props;
    }

    public void touchArtifact( UpdateCheck<Artifact, ArtifactTransferException> check )
    {
        Logger logger = check.getContext().getLogger();

        File touchFile = getTouchFile( check.getItem(), check.getFile() );

        if ( check.getFile().exists() )
        {
            touchFile.delete();
        }
        else
        {
            String key = getRepoKey( check.getRepository() );

            write( touchFile, key, check.getException(), logger );
        }
    }

    public void touchMetadata( UpdateCheck<Metadata, MetadataTransferException> check )
    {
        Logger logger = check.getContext().getLogger();

        File touchFile = getTouchFile( check.getItem(), check.getFile() );

        String key = getRepoKey( check.getRepository() ) + '.' + check.getItem().getType();

        write( touchFile, key, check.getException(), logger );
    }

    private void write( File touchFile, String key, Exception error, Logger logger )
    {
        synchronized ( touchFile.getAbsolutePath().intern() )
        {
            if ( !touchFile.getParentFile().exists() && !touchFile.getParentFile().mkdirs() )
            {
                logger.debug( "Failed to create directory: " + touchFile.getParent()
                    + " for tracking artifact metadata resolution." );
                return;
            }

            FileChannel channel = null;
            FileLock lock = null;
            try
            {
                Properties props = new Properties();

                channel = new RandomAccessFile( touchFile, "rw" ).getChannel();
                lock = channel.lock( 0, channel.size(), false );

                if ( touchFile.canRead() )
                {
                    logger.debug( "Reading resolution-state from: " + touchFile );
                    ByteBuffer buffer = ByteBuffer.allocate( (int) channel.size() );

                    channel.read( buffer );
                    buffer.flip();

                    ByteArrayInputStream stream = new ByteArrayInputStream( buffer.array() );
                    props.load( stream );
                }

                props.setProperty( key, Long.toString( System.currentTimeMillis() ) );

                if ( error == null || error instanceof ArtifactNotFoundException
                    || error instanceof MetadataNotFoundException )
                {
                    props.remove( key + ERROR_KEY_SUFFIX );
                }
                else
                {
                    String msg = error.getMessage();
                    if ( msg == null || msg.length() <= 0 )
                    {
                        msg = error.getClass().getSimpleName();
                    }
                    props.setProperty( key + ERROR_KEY_SUFFIX, msg );
                }

                ByteArrayOutputStream stream = new ByteArrayOutputStream();

                logger.debug( "Writing resolution-state to: " + touchFile );
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
                logger.debug( "Failed to record lastUpdated information for resolution.\nFile: " + touchFile.toString()
                    + "; key: " + key, e );
            }
            finally
            {
                release( lock, touchFile, logger );
                close( channel, touchFile, logger );
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
