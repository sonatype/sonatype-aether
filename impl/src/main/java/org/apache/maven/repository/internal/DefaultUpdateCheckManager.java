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

import java.io.File;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;

import org.apache.maven.repository.Artifact;
import org.apache.maven.repository.ArtifactNotFoundException;
import org.apache.maven.repository.ArtifactTransferException;
import org.apache.maven.repository.Authentication;
import org.apache.maven.repository.Metadata;
import org.apache.maven.repository.MetadataNotFoundException;
import org.apache.maven.repository.MetadataTransferException;
import org.apache.maven.repository.Proxy;
import org.apache.maven.repository.RemoteRepository;
import org.apache.maven.repository.RepositoryContext;
import org.apache.maven.repository.RepositoryPolicy;
import org.apache.maven.repository.spi.Logger;
import org.apache.maven.repository.spi.NullLogger;
import org.apache.maven.repository.spi.UpdateCheck;
import org.apache.maven.repository.spi.UpdateCheckManager;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

/**
 * @author Benjamin Bentmann
 */
@Component( role = UpdateCheckManager.class )
public class DefaultUpdateCheckManager
    implements UpdateCheckManager
{

    @Requirement
    private Logger logger = NullLogger.INSTANCE;

    private static final String UPDATED_KEY_SUFFIX = ".lastUpdated";

    private static final String ERROR_KEY_SUFFIX = ".error";

    public DefaultUpdateCheckManager setLogger( Logger logger )
    {
        this.logger = ( logger != null ) ? logger : NullLogger.INSTANCE;
        return this;
    }

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

    public void checkArtifact( RepositoryContext context, UpdateCheck<Artifact, ArtifactTransferException> check )
    {
        if ( check.getLocalLastUpdated() != 0 && !isUpdatedRequired( check.getLocalLastUpdated(), check.getPolicy() ) )
        {
            check.setRequired( false );
            return;
        }

        Artifact artifact = check.getItem();
        RemoteRepository repository = check.getRepository();

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
                if ( context.isNotFoundCachingEnabled() )
                {
                    check.setRequired( false );
                    check.setException( new ArtifactNotFoundException( artifact, repository, "Failure to find "
                        + artifact + " in " + repository.getUrl() + " was cached in the local repository. "
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
                if ( context.isTransferErrorCachingEnabled() )
                {
                    check.setRequired( false );
                    check.setException( new ArtifactTransferException( artifact, repository, "Failure to transfer "
                        + artifact + " from " + repository.getUrl() + " was cached in the local repository. "
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

    public void checkMetadata( RepositoryContext context, UpdateCheck<Metadata, MetadataTransferException> check )
    {
        if ( check.getLocalLastUpdated() != 0 && !isUpdatedRequired( check.getLocalLastUpdated(), check.getPolicy() ) )
        {
            check.setRequired( false );
            return;
        }

        Metadata metadata = check.getItem();
        RemoteRepository repository = check.getRepository();

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
                if ( context.isNotFoundCachingEnabled() )
                {
                    check.setRequired( false );
                    check.setException( new MetadataNotFoundException( metadata, repository, "Failure to find "
                        + metadata + " in " + repository.getUrl() + " was cached in the local repository. "
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
                if ( context.isTransferErrorCachingEnabled() )
                {
                    check.setRequired( false );
                    check.setException( new MetadataTransferException( metadata, repository, "Failure to transfer "
                        + metadata + " from " + repository.getUrl() + " was cached in the local repository. "
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
            return ( value.length() > 0 ) ? Long.parseLong( value ) : 0;
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
        return new TrackingFileManager( logger ).read( touchFile );
    }

    public void touchArtifact( RepositoryContext context, UpdateCheck<Artifact, ArtifactTransferException> check )
    {
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

    public void touchMetadata( RepositoryContext context, UpdateCheck<Metadata, MetadataTransferException> check )
    {
        File touchFile = getTouchFile( check.getItem(), check.getFile() );

        String key = getRepoKey( check.getRepository() ) + '.' + check.getItem().getType();

        write( touchFile, key, check.getException(), logger );
    }

    private void write( File touchFile, String key, Exception error, Logger logger )
    {
        Map<String, String> updates = new HashMap<String, String>();

        updates.put( key + UPDATED_KEY_SUFFIX, Long.toString( System.currentTimeMillis() ) );

        if ( error == null || error instanceof ArtifactNotFoundException || error instanceof MetadataNotFoundException )
        {
            updates.put( key + ERROR_KEY_SUFFIX, null );
        }
        else
        {
            String msg = error.getMessage();
            if ( msg == null || msg.length() <= 0 )
            {
                msg = error.getClass().getSimpleName();
            }
            updates.put( key + ERROR_KEY_SUFFIX, msg );
        }

        new TrackingFileManager( logger ).update( touchFile, updates );
    }

}
