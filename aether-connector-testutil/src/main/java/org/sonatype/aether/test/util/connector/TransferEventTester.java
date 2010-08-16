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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import org.sonatype.aether.DefaultArtifact;
import org.sonatype.aether.DefaultMetadata;
import org.sonatype.aether.Metadata;
import org.sonatype.aether.NoRepositoryConnectorException;
import org.sonatype.aether.RemoteRepository;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.TransferCancelledException;
import org.sonatype.aether.TransferEvent;
import org.sonatype.aether.TransferEvent.EventType;
import org.sonatype.aether.TransferListener;
import org.sonatype.aether.spi.connector.ArtifactDownload;
import org.sonatype.aether.spi.connector.ArtifactUpload;
import org.sonatype.aether.spi.connector.MetadataDownload;
import org.sonatype.aether.spi.connector.MetadataUpload;
import org.sonatype.aether.spi.connector.RepositoryConnector;
import org.sonatype.aether.spi.connector.RepositoryConnectorFactory;
import org.sonatype.aether.spi.connector.Transfer;
import org.sonatype.aether.test.util.FileUtil;
import org.sonatype.aether.util.DefaultRepositorySystemSession;

public class TransferEventTester
{

    private static class RecordingTransferListener
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
    }

    public static class TestContext
    {

        private RemoteRepository repository;

        private RepositorySystemSession session;

        public TestContext( RemoteRepository repository, RepositorySystemSession session )
        {
            super();
            this.repository = repository;
            this.session = session;
        }

        public TestContext()
        {
            super();
        }

        public RemoteRepository getRepository()
        {
            return repository;
        }

        public RepositorySystemSession getSession()
        {
            return session;
        }

        public void setRepository( RemoteRepository repository )
        {
            this.repository = repository;
        }

        public void setSession( RepositorySystemSession session )
        {
            this.session = session;
        }

        public RecordingTransferListener getRecordingTransferListener()
        {
            if ( session.getTransferListener() instanceof RecordingTransferListener )
            {
                return (RecordingTransferListener) session.getTransferListener();
            }
            else
            {
                return new RecordingTransferListener( session.getTransferListener() );
            }
        }

    }

    public static void testTransferEvents( RepositoryConnectorFactory factory )
        throws IOException, NoRepositoryConnectorException
    {

        RecordingTransferListener listener = new RecordingTransferListener();

        TestContext ctx = setupTestContext( listener );
        testTransferEvents( factory, ctx );
    }

    public static void testTransferEvents( RepositoryConnectorFactory factory, TestContext ctx )
        throws IOException, NoRepositoryConnectorException
    {
        RepositorySystemSession session = ctx.getSession();
        RemoteRepository repository = ctx.getRepository();
        RecordingTransferListener listener = ctx.getRecordingTransferListener();

        RepositoryConnector connector = factory.newInstance( session, repository );

        byte[] pattern = "tmpFile".getBytes();
        File tmpFile = FileUtil.createTempFile( pattern, 10000 );

        Collection<ArtifactUpload> artUps = createTransfers( ArtifactUpload.class, 1, tmpFile );

        connector.put( artUps, null );
        Deque<TransferEvent> events = new LinkedList<TransferEvent>( listener.getEvents() );

        TransferEvent currentEvent = events.pop();
        assertEquals( TransferEvent.EventType.INITIATED, currentEvent.getType() );
        // TODO: check mandatory attributes

        currentEvent = events.pop();
        assertEquals( TransferEvent.EventType.STARTED, currentEvent.getType() );
        // TODO: check mandatory attributes

        EventType progressed = TransferEvent.EventType.PROGRESSED;
        EventType succeeded = TransferEvent.EventType.SUCCEEDED;

        TransferEvent succeedEvent = null;

        int transferredBytes = 0;
        while ( ( currentEvent = events.pollFirst() ) != null )
        {
            EventType currentType = currentEvent.getType();

            if ( succeeded.equals( currentType ) )
            {
                succeedEvent = currentEvent;
                break;
            }
            else
            {
                assertTrue( progressed.equals( currentType ) );
                transferredBytes += currentEvent.getTransferredBytes();
                // TODO: check mandatory attributes
            }
        }

        // all events consumed
        assertEquals( 0, events.size() );

        // test transferred size
        assertEquals( tmpFile.length(), transferredBytes );
        assertEquals( tmpFile.length(), succeedEvent.getTransferredBytes() );
    }

    public static TestContext setupTestContext()
    {
        return setupTestContext( null );
    }

    public static TestContext setupTestContext( TransferListener listener )
    {

        File testRepo = new File( "target/test-repo" );
        testRepo.mkdirs();

        RemoteRepository repository;
        try
        {
            repository = new RemoteRepository( "test-repo", "default", testRepo.toURI().toURL().toString() );
        }
        catch ( MalformedURLException e )
        {
            // conversion File->URL... should not happen
            throw new UnsupportedOperationException(
                                                     "Malformed URL with File->URI->URL: " + testRepo.getAbsolutePath(),
                                                     e );
        }

        DefaultRepositorySystemSession session = DefaultRepositorySystemSession.newMavenRepositorySystemSession();
        if ( listener != null )
        {
            session.setTransferListener( listener );
        }

        return new TestContext( repository, session );

    }

    @SuppressWarnings( "unchecked" )
    public static <T extends Transfer> Collection<T> createTransfers( Class<T> cls, int count, File file )
    {
        ArrayList<T> ret = new ArrayList<T>();

        for ( int i = 0; i < count; i++ )
        {
            DefaultArtifact artifact =
                new DefaultArtifact( "testGroup", "testArtifact", "sources", "jar", ( i + 1 ) + "-test" );

            T obj = null;
            if ( cls.isAssignableFrom( ArtifactUpload.class ) )
            {
                obj = (T) new ArtifactUpload( artifact, file );
            }
            else if ( cls.isAssignableFrom( ArtifactDownload.class ) )
            {
                String context = null;
                String checksumPolicy = null;
                obj = (T) new ArtifactDownload( artifact, context, file, checksumPolicy );
            }
            else if ( cls.isAssignableFrom( MetadataUpload.class ) )
            {
                DefaultMetadata metadata =
                    new DefaultMetadata( "testGroup", "testArtifact", ( i + 1 ) + "test", "jar",
                                         Metadata.Nature.RELEASE_OR_SNAPSHOT, file );
                obj = (T) new MetadataUpload( metadata, file );
            }
            else if ( cls.isAssignableFrom( MetadataDownload.class ) )
            {
                obj = (T) new MetadataDownload();
            }

            ret.add( obj );
        }

        return ret;
    }
}
