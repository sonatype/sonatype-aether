package org.sonatype.aether.impl.internal;

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
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.impl.UpdateCheck;
import org.sonatype.aether.impl.UpdateCheckManager;
import org.sonatype.aether.metadata.Metadata;
import org.sonatype.aether.repository.Authentication;
import org.sonatype.aether.repository.Proxy;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.repository.RepositoryPolicy;
import org.sonatype.aether.spi.locator.Service;
import org.sonatype.aether.spi.locator.ServiceLocator;
import org.sonatype.aether.spi.log.Logger;
import org.sonatype.aether.spi.log.NullLogger;
import org.sonatype.aether.transfer.ArtifactNotFoundException;
import org.sonatype.aether.transfer.ArtifactTransferException;
import org.sonatype.aether.transfer.MetadataNotFoundException;
import org.sonatype.aether.transfer.MetadataTransferException;

/**
 * @author Benjamin Bentmann
 */
@Component( role = UpdateCheckManager.class )
public class DefaultUpdateCheckManager
    implements UpdateCheckManager, Service
{

    @Requirement
    private Logger logger = NullLogger.INSTANCE;

    private static final String UPDATED_KEY_SUFFIX = ".lastUpdated";

    private static final String ERROR_KEY_SUFFIX = ".error";

    private static final String NOT_FOUND = "";

    public DefaultUpdateCheckManager()
    {
        // enables default constructor
    }

    public DefaultUpdateCheckManager( Logger logger )
    {
        setLogger( logger );
    }

    public void initService( ServiceLocator locator )
    {
        setLogger( locator.getService( Logger.class ) );
    }

    public DefaultUpdateCheckManager setLogger( Logger logger )
    {
        this.logger = ( logger != null ) ? logger : NullLogger.INSTANCE;
        return this;
    }

    public String getEffectiveUpdatePolicy( RepositorySystemSession session, String policy1, String policy2 )
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

    public void checkArtifact( RepositorySystemSession session, UpdateCheck<Artifact, ArtifactTransferException> check )
    {
        if ( logger.isDebugEnabled() )
        {
            logger.debug( "Calculating update check for " + check.getItem() + " using policy " + check.getPolicy() );
        }

        if ( check.getLocalLastUpdated() != 0 && !isUpdatedRequired( check.getLocalLastUpdated(), check.getPolicy() ) )
        {
            check.setRequired( false );
            return;
        }

        Artifact artifact = check.getItem();
        RemoteRepository repository = check.getRepository();

        if ( check.getFile() == null )
        {
            throw new IllegalArgumentException( String.format( "The artifact '%s' had no file attached", artifact ) );
        }

        File touchFile = getTouchFile( artifact, check.getFile() );
        Properties props = read( touchFile );

        boolean fileExists = check.getFile().exists();
        String key = getRepoKey( repository );

        long lastUpdated = fileExists ? check.getFile().lastModified() : getLastUpdated( props, key );

        if ( lastUpdated == 0 )
        {
            check.setRequired( true );
        }
        else if ( isUpdatedRequired( lastUpdated, check.getPolicy() ) )
        {
            check.setRequired( true );
        }
        else if ( fileExists )
        {
            check.setRequired( false );
        }
        else
        {
            String error = getError( props, key );
            if ( error == null || error.length() <= 0 )
            {
                if ( session.isNotFoundCachingEnabled() )
                {
                    check.setRequired( false );
                    check.setException( new ArtifactNotFoundException( artifact, repository, "Failure to find "
                        + artifact + " in " + repository.getUrl() + " was cached in the local repository, "
                        + "resolution will not be reattempted until the update interval of " + repository.getId()
                        + " has elapsed or updates are forced" ) );
                }
                else
                {
                    check.setRequired( true );
                }
            }
            else
            {
                if ( session.isTransferErrorCachingEnabled() )
                {
                    check.setRequired( false );
                    check.setException( new ArtifactTransferException( artifact, repository, "Failure to transfer "
                        + artifact + " from " + repository.getUrl() + " was cached in the local repository, "
                        + "resolution will not be reattempted until the update interval of " + repository.getId()
                        + " has elapsed or updates are forced. Original error: " + error ) );
                }
                else
                {
                    check.setRequired( true );
                }
            }
        }
    }

    public void checkMetadata( RepositorySystemSession session, UpdateCheck<Metadata, MetadataTransferException> check )
    {
        if ( logger.isDebugEnabled() )
        {
            logger.debug( "Calculating update check for " + check.getItem() + " using policy " + check.getPolicy() );
        }

        if ( check.getLocalLastUpdated() != 0 && !isUpdatedRequired( check.getLocalLastUpdated(), check.getPolicy() ) )
        {
            check.setRequired( false );
            return;
        }

        Metadata metadata = check.getItem();
        RemoteRepository repository = check.getRepository();

        if ( check.getFile() == null )
        {
            throw new IllegalArgumentException( String.format( "The metadata '%s' had no file attached", metadata ) );
        }

        File touchFile = getTouchFile( check.getItem(), check.getFile() );
        Properties props = read( touchFile );

        boolean fileExists = check.getFile().exists();
        String key = fileExists ? check.getFile().getName() : ( getRepoKey( repository ) + '.' + metadata.getType() );

        long lastUpdated = getLastUpdated( props, key );

        if ( lastUpdated == 0 )
        {
            check.setRequired( true );
        }
        else if ( isUpdatedRequired( lastUpdated, check.getPolicy() ) )
        {
            check.setRequired( true );
        }
        else if ( fileExists )
        {
            check.setRequired( false );
        }
        else
        {
            String error = getError( props, key );
            if ( error == null || error.length() <= 0 )
            {
                check.setRequired( false );
                check.setException( new MetadataNotFoundException( metadata, repository, "Failure to find " + metadata
                    + " in " + repository.getUrl() + " was cached in the local repository, "
                    + "resolution will not be reattempted until the update interval of " + repository.getId()
                    + " has elapsed or updates are forced" ) );
            }
            else
            {
                if ( session.isTransferErrorCachingEnabled() )
                {
                    check.setRequired( false );
                    check.setException( new MetadataTransferException( metadata, repository, "Failure to transfer "
                        + metadata + " from " + repository.getUrl() + " was cached in the local repository, "
                        + "resolution will not be reattempted until the update interval of " + repository.getId()
                        + " has elapsed or updates are forced. Original error: " + error ) );
                }
                else
                {
                    check.setRequired( true );
                }
            }
        }
    }

    private long getLastUpdated( Properties props, String key )
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
            SimpleDigest digest = new SimpleDigest();
            digest.update( auth.getUsername() );
            digest.update( auth.getPassword() );
            digest.update( auth.getPrivateKeyFile() );
            digest.update( auth.getPassphrase() );
            buffer.append( digest.digest() ).append( '@' );
        }
    }

    private boolean isUpdatedRequired( long lastModified, String policy )
    {
        boolean checkForUpdates;

        if ( policy == null )
        {
            policy = "";
        }

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

    private Properties read( File touchFile )
    {
        Properties props = new TrackingFileManager().setLogger( logger ).read( touchFile );
        return ( props != null ) ? props : new Properties();
    }

    public void touchArtifact( RepositorySystemSession session, UpdateCheck<Artifact, ArtifactTransferException> check )
    {
        File touchFile = getTouchFile( check.getItem(), check.getFile() );

        String key = getRepoKey( check.getRepository() );

        Properties props = write( touchFile, key, "artifact", check.getException() );

        if ( check.getFile().exists() && !hasErrors( props ) )
        {
            touchFile.delete();
        }
    }

    private boolean hasErrors( Properties props )
    {
        for ( Object key : props.keySet() )
        {
            if ( key.toString().endsWith( ERROR_KEY_SUFFIX ) )
            {
                return true;
            }
        }
        return false;
    }

    public void touchMetadata( RepositorySystemSession session, UpdateCheck<Metadata, MetadataTransferException> check )
    {
        File touchFile = getTouchFile( check.getItem(), check.getFile() );

        String key = getRepoKey( check.getRepository() ) + '.' + check.getItem().getType();

        write( touchFile, key, check.getFile().getName(), check.getException() );
    }

    private Properties write( File touchFile, String fullKey, String simpleKey, Exception error )
    {
        Map<String, String> updates = new HashMap<String, String>();

        String timestamp = Long.toString( System.currentTimeMillis() );
        updates.put( fullKey + UPDATED_KEY_SUFFIX, timestamp );
        updates.put( simpleKey + UPDATED_KEY_SUFFIX, timestamp );

        if ( error == null )
        {
            updates.put( fullKey + ERROR_KEY_SUFFIX, null );
        }
        else if ( error instanceof ArtifactNotFoundException || error instanceof MetadataNotFoundException )
        {
            updates.put( fullKey + ERROR_KEY_SUFFIX, NOT_FOUND );
        }
        else
        {
            String msg = error.getMessage();
            if ( msg == null || msg.length() <= 0 )
            {
                msg = error.getClass().getSimpleName();
            }
            updates.put( fullKey + ERROR_KEY_SUFFIX, msg );
        }

        return new TrackingFileManager().setLogger( logger ).update( touchFile, updates );
    }

}
