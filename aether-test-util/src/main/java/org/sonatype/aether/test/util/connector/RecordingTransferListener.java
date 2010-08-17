package org.sonatype.aether.test.util.connector;

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

import java.util.ArrayList;
import java.util.List;

import org.sonatype.aether.TransferCancelledException;
import org.sonatype.aether.TransferEvent;
import org.sonatype.aether.TransferListener;

class RecordingTransferListener
    implements TransferListener
{

    private List<TransferEvent> events = new ArrayList<TransferEvent>();

    private List<TransferEvent> progressEvents = new ArrayList<TransferEvent>();

    private TransferListener realListener;

    public RecordingTransferListener()
    {
        this( null );
    }

    public RecordingTransferListener( TransferListener transferListener )
    {
        this.realListener = transferListener;
    }

    public List<TransferEvent> getEvents()
    {
        return events;
    }

    public List<TransferEvent> getProgressEvents()
    {
        return progressEvents;
    }

    public void transferSucceeded( TransferEvent event )
    {
        events.add( event );
        if ( realListener != null )
        {
            realListener.transferSucceeded( event );
        }
    }

    public void transferStarted( TransferEvent event )
        throws TransferCancelledException
    {
        events.add( event );
        if ( realListener != null )
        {
            realListener.transferStarted( event );
        }
    }

    public void transferProgressed( TransferEvent event )
        throws TransferCancelledException
    {
        events.add( event );
        progressEvents.add( event );
        if ( realListener != null )
        {
            realListener.transferProgressed( event );
        }
    }

    public void transferInitiated( TransferEvent event )
        throws TransferCancelledException
    {
        events.add( event );
        if ( realListener != null )
        {
            realListener.transferInitiated( event );
        }
    }

    public void transferFailed( TransferEvent event )
    {
        events.add( event );
        if ( realListener != null )
        {
            realListener.transferFailed( event );
        }
    }

    public void transferCorrupted( TransferEvent event )
        throws TransferCancelledException
    {
        events.add( event );
        if ( realListener != null )
        {
            realListener.transferCorrupted( event );
        }
    }

    public void clear()
    {
        events.clear();
        progressEvents.clear();
    }
}