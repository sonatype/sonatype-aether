package org.sonatype.aether.test.util.connector.suite;

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

import static org.junit.Assert.*;
import static org.sonatype.aether.transfer.TransferEvent.EventType.*;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.spi.connector.ArtifactDownload;
import org.sonatype.aether.spi.connector.ArtifactUpload;
import org.sonatype.aether.spi.connector.MetadataDownload;
import org.sonatype.aether.spi.connector.MetadataUpload;
import org.sonatype.aether.spi.connector.RepositoryConnector;
import org.sonatype.aether.spi.connector.RepositoryConnectorFactory;
import org.sonatype.aether.test.impl.RecordingTransferListener;
import org.sonatype.aether.test.impl.TestRepositorySystemSession;
import org.sonatype.aether.test.util.FileUtil;
import org.sonatype.aether.transfer.NoRepositoryConnectorException;
import org.sonatype.aether.transfer.TransferEvent;
import org.sonatype.aether.transfer.TransferEvent.EventType;

public class TransferEventTester
{
    public static void testSuccessfulTransferEvents( RepositoryConnectorFactory factory,
                                                     TestRepositorySystemSession session, RemoteRepository repository )
        throws NoRepositoryConnectorException, IOException
    {
        RecordingTransferListener listener = new RecordingTransferListener( session.getTransferListener() );
        session.setTransferListener( listener );

        RepositoryConnector connector = factory.newInstance( session, repository );

        byte[] pattern = "tmpFile".getBytes();
        File tmpFile = FileUtil.createTempFile( pattern, 10000 );
        long expectedBytes = tmpFile.length();

        Collection<ArtifactUpload> artUps = ConnectorTestUtil.createTransfers( ArtifactUpload.class, 1, tmpFile );
        Collection<ArtifactDownload> artDowns = ConnectorTestUtil.createTransfers( ArtifactDownload.class, 1, tmpFile );
        Collection<MetadataUpload> metaUps = ConnectorTestUtil.createTransfers( MetadataUpload.class, 1, tmpFile );
        Collection<MetadataDownload> metaDowns = ConnectorTestUtil.createTransfers( MetadataDownload.class, 1, tmpFile );

        connector.put( artUps, null );
        LinkedList<TransferEvent> events = new LinkedList<TransferEvent>( listener.getEvents() );
        checkEvents( events, expectedBytes );
        listener.clear();

        connector.get( artDowns, null );
        events = new LinkedList<TransferEvent>( listener.getEvents() );
        checkEvents( events, expectedBytes );
        listener.clear();

        connector.put( null, metaUps );
        events = new LinkedList<TransferEvent>( listener.getEvents() );
        checkEvents( events, expectedBytes );
        listener.clear();

        connector.get( null, metaDowns );
        events = new LinkedList<TransferEvent>( listener.getEvents() );
        checkEvents( events, expectedBytes );
    }

    private static void checkEvents( Queue<TransferEvent> events, long expectedBytes )
    {
        TransferEvent currentEvent = events.poll();
        String msg = "initiate event is missing";
        assertNotNull( msg, currentEvent );
        assertEquals( msg, INITIATED, currentEvent.getType() );
        checkProperties( currentEvent );

        currentEvent = events.poll();
        msg = "start event is missing";
        assertNotNull( msg, currentEvent );
        assertEquals( msg, TransferEvent.EventType.STARTED, currentEvent.getType() );
        checkProperties( currentEvent );

        EventType progressed = TransferEvent.EventType.PROGRESSED;
        EventType succeeded = TransferEvent.EventType.SUCCEEDED;

        TransferEvent succeedEvent = null;

        int dataLength = 0;
        long transferredBytes = 0;
        while ( ( currentEvent = events.poll() ) != null )
        {
            EventType currentType = currentEvent.getType();

            if ( succeeded.equals( currentType ) )
            {
                succeedEvent = currentEvent;
                checkProperties( currentEvent );
                break;
            }
            else
            {
                assertTrue( progressed.equals( currentType ) );
                assertTrue( currentEvent.getTransferredBytes() > transferredBytes );
                transferredBytes = currentEvent.getTransferredBytes();
                dataLength += currentEvent.getDataLength();
                checkProperties( currentEvent );
            }
        }

        // all events consumed
        assertEquals( "too many events left: " + events.toString(), 0, events.size() );

        // test transferred size
        assertEquals( "progress events transferred bytes don't match: data length does not add up", expectedBytes,
                      dataLength );
        assertEquals( "succeed event transferred bytes don't match", expectedBytes, succeedEvent.getTransferredBytes() );
    }

    private static void checkProperties( TransferEvent event )
    {
        assertNotNull( "resource is null for type: " + event.getType(), event.getResource() );
        assertNotNull( "request type is null for type: " + event.getType(), event.getRequestType() );
        assertNotNull( "type is null for type: " + event.getType(), event.getType() );

        if ( PROGRESSED.equals( event.getType() ) )
        {
            assertNotNull( "data buffer is null for type: " + event.getType(), event.getDataBuffer() );
            assertTrue( "data length is not set/not positive for type: " + event.getType(), event.getDataLength() > -1 );
            assertTrue( "data offset is not valid for type: " + event.getType(), event.getDataOffset() > -1 );

            assertTrue( "invalid data offset: bigger than data length", event.getDataOffset() < event.getDataLength() );
            assertTrue( "invalid data window: offset+data length > data buffer length",
                        event.getDataOffset() + event.getDataLength() <= event.getDataBuffer().length );

            assertTrue( "transferred byte is not set/not positive for type: " + event.getType(),
                        event.getTransferredBytes() > -1 );
        }
        else if ( SUCCEEDED.equals( event.getType() ) )
        {
            assertTrue( "transferred byte is not set/not positive for type: " + event.getType(),
                        event.getTransferredBytes() > -1 );
        }
    }
}
