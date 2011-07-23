package org.sonatype.aether.connector.wagon;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import java.io.File;

import org.apache.maven.wagon.events.TransferEvent;
import org.apache.maven.wagon.observers.AbstractTransferListener;
import org.sonatype.aether.RequestTrace;
import org.sonatype.aether.transfer.TransferCancelledException;
import org.sonatype.aether.transfer.TransferEvent.EventType;
import org.sonatype.aether.transfer.TransferEvent.RequestType;
import org.sonatype.aether.transfer.TransferListener;
import org.sonatype.aether.util.listener.DefaultTransferEvent;
import org.sonatype.aether.util.listener.DefaultTransferResource;

/**
 * An adapter to transform transfer events from Wagon into events for the repository system.
 * 
 * @author Benjamin Bentmann
 */
class WagonTransferListenerAdapter
    extends AbstractTransferListener
{

    private final DefaultTransferResource resource;

    private final TransferListener delegate;

    private long transferredBytes;

    public WagonTransferListenerAdapter( TransferListener delegate, String repositoryUrl, String resourceName,
                                         File file, RequestTrace trace )
    {
        this.delegate = delegate;
        resource = new DefaultTransferResource( repositoryUrl, resourceName, file, trace );
    }

    @Override
    public void transferStarted( TransferEvent event )
    {
        transferredBytes = 0;
        resource.setContentLength( event.getResource().getContentLength() );
        try
        {
            delegate.transferStarted( wrap( event, EventType.STARTED ) );
        }
        catch ( TransferCancelledException e )
        {
            /*
             * NOTE: Wagon transfers are not freely abortable. In particular, aborting from
             * AbstractWagon.fire(Get|Put)Started() would result in unclosed streams so we avoid this case.
             */
        }
    }

    @Override
    public void transferProgress( TransferEvent event, byte[] buffer, int length )
    {
        transferredBytes += length;
        try
        {
            delegate.transferProgressed( wrap( event, EventType.PROGRESSED ).setDataBuffer( buffer, 0, length ) );
        }
        catch ( TransferCancelledException e )
        {
            throw new WagonCancelledException( e );
        }
    }

    private DefaultTransferEvent wrap( TransferEvent event, EventType type )
    {
        DefaultTransferEvent e = newEvent();
        e.setRequestType( event.getRequestType() == TransferEvent.REQUEST_PUT ? RequestType.PUT : RequestType.GET );
        e.setType( type );
        return e;
    }

    public DefaultTransferEvent newEvent()
    {
        DefaultTransferEvent e = new DefaultTransferEvent();
        e.setResource( resource );
        e.setTransferredBytes( transferredBytes );
        return e;
    }

}
