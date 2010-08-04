package org.sonatype.aether.connector.file;

import org.sonatype.aether.TransferCancelledException;
import org.sonatype.aether.TransferEvent;
import org.sonatype.aether.TransferListener;
import org.sonatype.aether.util.listener.DefaultTransferEvent;

class TransferEventCatapult
{
    
    private TransferListener listener;

    public TransferEventCatapult(TransferListener listener)
    {
        super();
        this.listener = listener;
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

}
