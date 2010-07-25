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

import org.sonatype.aether.Artifact;
import org.sonatype.aether.RemoteRepository;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.TransferCancelledException;
import org.sonatype.aether.TransferEvent;
import org.sonatype.aether.TransferEvent.RequestType;
import org.sonatype.aether.TransferListener;
import org.sonatype.aether.connector.file.FileConnectorWorker.Direction;
import org.sonatype.aether.spi.connector.ArtifactTransfer;
import org.sonatype.aether.util.listener.DefaultTransferEvent;
import org.sonatype.aether.util.listener.DefaultTransferResource;

public abstract class FileConnectorWorker
{
    protected enum Direction
    {
        UPLOAD( TransferEvent.RequestType.PUT ), DOWNLOAD( TransferEvent.RequestType.GET );

        TransferEvent.RequestType type;

        private Direction( TransferEvent.RequestType type )
        {
            this.type = type;
        }

        public RequestType getType()
        {
            return type;
        }
    }

    private TransferListener listener;

    protected Direction direction;

    public FileConnectorWorker( RepositorySystemSession session, RemoteRepository repository, Direction direction )
    {
        this.listener = session.getTransferListener();
        this.direction = direction;
    }

    protected DefaultTransferEvent newEvent( TransferWrapper transfer, RemoteRepository repository )
    {
        DefaultTransferEvent event = new DefaultTransferEvent();
        Artifact artifact = transfer.getArtifact();
        String resourceName =
            String.format( "%s:%s:%s", artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion() );
        event.setResource( new DefaultTransferResource( repository.getUrl(), resourceName, transfer.getFile() ) );
        event.setRequestType( direction.getType() );
        return event;

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

    protected void fireProgressed( DefaultTransferEvent event )
        throws TransferCancelledException
    {
        event.setType( TransferEvent.EventType.PROGRESSED );
        listener.transferProgressed( event );
    }

}
