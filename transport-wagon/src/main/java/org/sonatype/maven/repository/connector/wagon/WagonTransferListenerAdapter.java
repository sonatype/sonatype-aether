package org.sonatype.maven.repository.connector.wagon;

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

import org.apache.maven.wagon.events.TransferEvent;
import org.apache.maven.wagon.observers.AbstractTransferListener;
import org.sonatype.maven.repository.TransferCancelledException;
import org.sonatype.maven.repository.TransferListener;
import org.sonatype.maven.repository.TransferResource;
import org.sonatype.maven.repository.TransferEvent.EventType;
import org.sonatype.maven.repository.TransferEvent.RequestType;
import org.sonatype.maven.repository.util.DefaultTransferEvent;
import org.sonatype.maven.repository.util.DefaultTransferResource;

/**
 * An adapter to transform transfer events from Wagon into events for the repository system.
 * 
 * @author Benjamin Bentmann
 */
class WagonTransferListenerAdapter
    extends AbstractTransferListener
{

    private final TransferResource resource;

    private final TransferListener delegate;

    private long transferredBytes;

    public WagonTransferListenerAdapter( TransferListener delegate, String repositoryUrl, String resourceName, File file )
    {
        this.delegate = delegate;
        resource = new DefaultTransferResource( repositoryUrl, resourceName, file );
    }

    public void transferStarted( TransferEvent event )
    {
        transferredBytes = 0;
        try
        {
            delegate.transferStarted( wrap( event, EventType.STARTED ) );
        }
        catch ( TransferCancelledException e )
        {
            // wagon transfers are not freely abortable
        }
    }

    public void transferProgress( TransferEvent event, byte[] buffer, int length )
    {
        transferredBytes += length;
        try
        {
            delegate.transferProgressed( wrap( event, EventType.PROGRESSED ).setDataBuffer( buffer ).setDataLength(
                                                                                                                    length ) );
        }
        catch ( TransferCancelledException e )
        {
            // wagon transfers are not freely abortable
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
