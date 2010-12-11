package org.sonatype.aether.impl.internal;

/*******************************************************************************
 * Copyright (c) 2010 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.aether.RepositoryEvent;
import org.sonatype.aether.RepositoryListener;
import org.sonatype.aether.impl.RepositoryEventDispatcher;
import org.sonatype.aether.spi.locator.Service;
import org.sonatype.aether.spi.locator.ServiceLocator;
import org.sonatype.aether.spi.log.Logger;
import org.sonatype.aether.spi.log.NullLogger;

/**
 * @author Benjamin Bentmann
 */
@Component( role = RepositoryEventDispatcher.class )
public class DefaultRepositoryEventDispatcher
    implements RepositoryEventDispatcher, Service
{

    @Requirement
    private Logger logger = NullLogger.INSTANCE;

    @Requirement( role = RepositoryListener.class )
    private List<RepositoryListener> listeners = new ArrayList<RepositoryListener>();

    public DefaultRepositoryEventDispatcher()
    {
        // enables no-arg constructor
    }

    public DefaultRepositoryEventDispatcher( Logger logger, List<RepositoryListener> listeners )
    {
        setLogger( logger );
        setListeners( listeners );
    }

    public DefaultRepositoryEventDispatcher setLogger( Logger logger )
    {
        this.logger = ( logger != null ) ? logger : NullLogger.INSTANCE;
        return this;
    }

    public DefaultRepositoryEventDispatcher addListener( RepositoryListener listener )
    {
        if ( listener == null )
        {
            throw new IllegalArgumentException( "repository listener has not been specified" );
        }
        this.listeners.add( listener );
        return this;
    }

    public DefaultRepositoryEventDispatcher setListeners( List<RepositoryListener> listeners )
    {
        if ( listeners == null )
        {
            this.listeners = new ArrayList<RepositoryListener>();
        }
        else
        {
            this.listeners = listeners;
        }
        return this;
    }

    public void initService( ServiceLocator locator )
    {
        setLogger( locator.getService( Logger.class ) );
        setListeners( locator.getServices( RepositoryListener.class ) );
    }

    public void dispatch( RepositoryEvent event )
    {
        if ( !listeners.isEmpty() )
        {
            for ( RepositoryListener listener : listeners )
            {
                dispatch( event, listener );
            }
        }

        RepositoryListener listener = event.getSession().getRepositoryListener();

        if ( listener != null )
        {
            dispatch( event, listener );
        }
    }

    private void dispatch( RepositoryEvent event, RepositoryListener listener )
    {
        try
        {
            switch ( event.getType() )
            {
                case ARTIFACT_DEPLOYED:
                    listener.artifactDeployed( event );
                    break;
                case ARTIFACT_DEPLOYING:
                    listener.artifactDeploying( event );
                    break;
                case ARTIFACT_DESCRIPTOR_INVALID:
                    listener.artifactDescriptorInvalid( event );
                    break;
                case ARTIFACT_DESCRIPTOR_MISSING:
                    listener.artifactDescriptorMissing( event );
                    break;
                case ARTIFACT_DOWNLOADED:
                    listener.artifactDownloaded( event );
                    break;
                case ARTIFACT_DOWNLOADING:
                    listener.artifactDownloading( event );
                    break;
                case ARTIFACT_INSTALLED:
                    listener.artifactInstalled( event );
                    break;
                case ARTIFACT_INSTALLING:
                    listener.artifactInstalling( event );
                    break;
                case ARTIFACT_RESOLVED:
                    listener.artifactResolved( event );
                    break;
                case ARTIFACT_RESOLVING:
                    listener.artifactResolving( event );
                    break;
                case METADATA_DEPLOYED:
                    listener.metadataDeployed( event );
                    break;
                case METADATA_DEPLOYING:
                    listener.metadataDeploying( event );
                    break;
                case METADATA_DOWNLOADED:
                    listener.metadataDownloaded( event );
                    break;
                case METADATA_DOWNLOADING:
                    listener.metadataDownloading( event );
                    break;
                case METADATA_INSTALLED:
                    listener.metadataInstalled( event );
                    break;
                case METADATA_INSTALLING:
                    listener.metadataInstalling( event );
                    break;
                case METADATA_INVALID:
                    listener.metadataInvalid( event );
                    break;
                case METADATA_RESOLVED:
                    listener.metadataResolved( event );
                    break;
                case METADATA_RESOLVING:
                    listener.metadataResolving( event );
                    break;
                default:
                    throw new IllegalStateException( "unknown repository event type " + event.getType() );
            }
        }
        catch ( Exception e )
        {
            if ( logger.isDebugEnabled() )
            {
                logger.warn( "Failed to dispatch repository event to " + listener.getClass().getCanonicalName() + ": "
                    + e.getMessage(), e );
            }
            else
            {
                logger.warn( "Failed to dispatch repository event to " + listener.getClass().getCanonicalName() + ": "
                    + e.getMessage() );
            }
        }
    }

}
