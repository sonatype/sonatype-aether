package org.sonatype.aether.connector.async;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import java.nio.ByteBuffer;

import org.sonatype.aether.transfer.ChecksumFailureException;
import org.sonatype.aether.transfer.TransferCancelledException;
import org.sonatype.aether.transfer.TransferEvent;
import org.sonatype.aether.transfer.TransferEvent.RequestType;
import org.sonatype.aether.transfer.TransferListener;
import org.sonatype.aether.transfer.TransferResource;
import org.sonatype.aether.util.listener.DefaultTransferEvent;

/**
 * @author Benjamin Hanzelmann
 */
class TransferEventCatapult
{

    private TransferListener listener;

    private TransferResource transferResource;

    private RequestType requestType;

    private boolean startMissing = true;

    private TransferEventCatapult( TransferListener listener, TransferResource transferResource, RequestType requestType )
    {
        this.listener = listener;
        this.transferResource = transferResource;
        this.requestType = requestType;
    }

    void fireCorrupted( ChecksumFailureException e )
        throws TransferCancelledException
    {
        if ( listener != null )
        {
            TransferEvent event = newEvent( e, TransferEvent.EventType.CORRUPTED );
            listener.transferCorrupted( event );
        }

    }

    void fireFailed( Exception exception )
    {
        if ( listener != null )
        {
            listener.transferFailed( newEvent( exception, TransferEvent.EventType.FAILED ) );
        }
    }

    void fireInitiated()
        throws TransferCancelledException
    {
        if ( listener != null )
        {
            listener.transferInitiated( newEvent( null, TransferEvent.EventType.INITIATED ) );
        }
    }

    void fireSucceeded( long transferredBytes )
    {
        if ( listener != null )
        {
            listener.transferSucceeded( newEvent( null, TransferEvent.EventType.SUCCEEDED ).setTransferredBytes( transferredBytes ) );
        }
    }

    DefaultTransferEvent newEvent( Exception e, TransferEvent.EventType eventType )
    {
        DefaultTransferEvent event = new DefaultTransferEvent();
        event.setResource( transferResource );
        event.setRequestType( requestType );
        event.setType( eventType );
        event.setException( e );
        return event;
    }

    public void fireProgressed( ByteBuffer buffer, long transferredBytes )
        throws TransferCancelledException
    {
        if ( listener != null )
        {
            if ( startMissing )
            {
                startMissing = false;
                fireStarted();
            }
            DefaultTransferEvent event =
                newEvent( null, TransferEvent.EventType.PROGRESSED ).setDataBuffer( buffer ).setTransferredBytes( transferredBytes );
            listener.transferProgressed( event );
        }
    }

    public void fireStarted()
        throws TransferCancelledException
    {
        if ( listener != null )
        {
            listener.transferStarted( newEvent( null, TransferEvent.EventType.STARTED ) );
        }
    }

    public static TransferEventCatapult newDownloadCatapult( TransferListener listener,
                                                             TransferResource transferResource )
    {
        return new TransferEventCatapult( listener, transferResource, TransferEvent.RequestType.GET );
    }

    public static TransferEventCatapult newUploadCatapult( TransferListener listener, TransferResource transferResource )
    {
        return new TransferEventCatapult( listener, transferResource, TransferEvent.RequestType.PUT );
    }

}
