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

import org.apache.maven.repository.DefaultTransferEvent;
import org.apache.maven.repository.DefaultTransferResource;
import org.apache.maven.repository.TransferCancelledException;
import org.apache.maven.repository.TransferEvent.EventType;
import org.apache.maven.repository.TransferEvent.RequestType;
import org.apache.maven.repository.TransferListener;
import org.apache.maven.repository.TransferResource;
import org.apache.maven.wagon.events.TransferEvent;
import org.apache.maven.wagon.observers.AbstractTransferListener;

/**
 * @author Benjamin Bentmann
 */
class WagonTransferListenerAdapter
    extends AbstractTransferListener
{

    private final TransferResource resource;

    private final TransferListener delegate;

    private long transferredBytes;

    public WagonTransferListenerAdapter( TransferListener delegate, String repositoryUrl, String resourceName )
    {
        this.delegate = delegate;
        resource = new DefaultTransferResource( repositoryUrl, resourceName );
    }

    public TransferResource getResource()
    {
        return this.resource;
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
        DefaultTransferEvent e = new DefaultTransferEvent();
        e.setResource( resource );
        e.setRequestType( event.getRequestType() == TransferEvent.REQUEST_PUT ? RequestType.PUT : RequestType.GET );
        e.setType( type );
        e.setTransferredBytes( transferredBytes );
        return e;
    }

}
