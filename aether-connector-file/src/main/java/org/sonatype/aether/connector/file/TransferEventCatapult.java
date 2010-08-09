package org.sonatype.aether.connector.file;

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

import org.sonatype.aether.TransferCancelledException;
import org.sonatype.aether.TransferEvent;
import org.sonatype.aether.TransferListener;
import org.sonatype.aether.util.listener.DefaultTransferEvent;

/**
 * Helper for {@link TransferEvent}-handling.
 * 
 * @author Benjamin Hanzelmann
 */
class TransferEventCatapult
{

    private TransferListener listener;

    public TransferEventCatapult( TransferListener listener )
    {
        super();
        if ( listener == null )
        {
            listener = new NoTransferListener();
        }
        else
        {
            this.listener = listener;
        }
    }

    protected void fireInitiated( DefaultTransferEvent event )
        throws TransferCancelledException
    {
        event.setType( TransferEvent.EventType.INITIATED );
        listener.transferInitiated( event );
    }

    protected void fireStarted( DefaultTransferEvent event )
        throws TransferCancelledException
    {
        event.setType( TransferEvent.EventType.STARTED );
        listener.transferStarted( event );
    }

    protected void fireSucceeded( DefaultTransferEvent event )
        throws TransferCancelledException
    {
        event.setType( TransferEvent.EventType.SUCCEEDED );
        listener.transferSucceeded( event );
    }

    protected void fireFailed( DefaultTransferEvent event )
        throws TransferCancelledException
    {
        event.setType( TransferEvent.EventType.FAILED );
        listener.transferFailed( event );
    }

    protected void fireCorrupted( DefaultTransferEvent event )
        throws TransferCancelledException
    {
        event.setType( TransferEvent.EventType.FAILED );
        listener.transferCorrupted( event );
    }

    protected void fireProgressed( DefaultTransferEvent event )
        throws TransferCancelledException
    {
        event.setType( TransferEvent.EventType.PROGRESSED );
        listener.transferProgressed( event );
    }
    

    private final class NoTransferListener
        implements TransferListener
    {
        public void transferSucceeded( TransferEvent event )
        {
        }
    
        public void transferStarted( TransferEvent event )
            throws TransferCancelledException
        {
        }
    
        public void transferProgressed( TransferEvent event )
            throws TransferCancelledException
        {
        }
    
        public void transferInitiated( TransferEvent event )
            throws TransferCancelledException
        {
        }
    
        public void transferFailed( TransferEvent event )
        {
        }
    
        public void transferCorrupted( TransferEvent event )
            throws TransferCancelledException
        {
        }
    }

}
