package org.sonatype.aether.impl.internal;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *******************************************************************************/

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.impl.LocalRepositoryMaintainer;
import org.sonatype.aether.impl.UpdateCheckManager;
import org.sonatype.aether.impl.VersionResolver;
import org.sonatype.aether.metadata.Metadata;
import org.sonatype.aether.repository.LocalArtifactRegistration;
import org.sonatype.aether.repository.LocalArtifactRequest;
import org.sonatype.aether.repository.LocalArtifactResult;
import org.sonatype.aether.repository.LocalRepository;
import org.sonatype.aether.repository.LocalRepositoryManager;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.repository.WorkspaceReader;
import org.sonatype.aether.repository.WorkspaceRepository;
import org.sonatype.aether.resolution.ArtifactRequest;
import org.sonatype.aether.resolution.ArtifactResolutionException;
import org.sonatype.aether.resolution.ArtifactResult;
import org.sonatype.aether.resolution.VersionRequest;
import org.sonatype.aether.resolution.VersionResolutionException;
import org.sonatype.aether.resolution.VersionResult;
import org.sonatype.aether.spi.connector.ArtifactDownload;
import org.sonatype.aether.spi.connector.MetadataDownload;
import org.sonatype.aether.spi.log.NullLogger;
import org.sonatype.aether.test.impl.RecordingRepositoryListener;
import org.sonatype.aether.test.impl.RecordingRepositoryListener.EventWrapper;
import org.sonatype.aether.test.impl.RecordingRepositoryListener.Type;
import org.sonatype.aether.test.impl.TestFileProcessor;
import org.sonatype.aether.test.impl.TestRepositorySystemSession;
import org.sonatype.aether.test.util.TestFileUtils;
import org.sonatype.aether.test.util.impl.StubArtifact;
import org.sonatype.aether.transfer.ArtifactNotFoundException;
import org.sonatype.aether.transfer.ArtifactTransferException;
import org.sonatype.aether.util.artifact.ArtifactProperties;

/**
 * @author Benjamin Hanzelmann
 */
public class DefaultArtifactResolverTest
{
    private DefaultArtifactResolver resolver;

    private TestRepositorySystemSession session;

    private StubRemoteRepositoryManager remoteRepositoryManager;

    private Artifact artifact;

    private RecordingRepositoryConnector connector;

    @Before
    public void setup()
        throws IOException
    {
        UpdateCheckManager updateCheckManager = new StaticUpdateCheckManager( true );
        List<LocalRepositoryMaintainer> localRepositoryMaintainers = null;
        remoteRepositoryManager = new StubRemoteRepositoryManager();
        VersionResolver versionResolver = new StubVersionResolver();
        session = new TestRepositorySystemSession();
        resolver =
            new DefaultArtifactResolver( NullLogger.INSTANCE, TestFileProcessor.INSTANCE, versionResolver,
                                         updateCheckManager, remoteRepositoryManager, localRepositoryMaintainers );

        artifact = new StubArtifact( "gid", "aid", "", "ext", "ver" );

        connector = new RecordingRepositoryConnector();
        remoteRepositoryManager.setConnector( connector );
    }

    @After
    public void teardown()
        throws Exception
    {
        if ( session.getLocalRepository() != null )
        {
            TestFileUtils.delete( session.getLocalRepository().getBasedir() );
        }
    }

    @Test
    public void testResolveLocalArtifactSuccessful()
        throws IOException, ArtifactResolutionException
    {
        File tmpFile = TestFileUtils.createTempFile( "tmp" );
        Map<String, String> properties = new HashMap<String, String>();
        properties.put( ArtifactProperties.LOCAL_PATH, tmpFile.getAbsolutePath() );
        artifact = artifact.setProperties( properties );

        ArtifactRequest request = new ArtifactRequest( artifact, null, "" );
        ArtifactResult result = resolver.resolveArtifact( session, request );

        assertTrue( result.getExceptions().isEmpty() );

        Artifact resolved = result.getArtifact();
        assertNotNull( resolved.getFile() );
        resolved = resolved.setFile( null );

        assertEquals( artifact, resolved );
    }

    @Test
    public void testResolveLocalArtifactUnsuccessful()
        throws IOException, ArtifactResolutionException
    {
        File tmpFile = TestFileUtils.createTempFile( "tmp" );
        Map<String, String> properties = new HashMap<String, String>();
        properties.put( ArtifactProperties.LOCAL_PATH, tmpFile.getAbsolutePath() );
        artifact = artifact.setProperties( properties );

        tmpFile.delete();

        ArtifactRequest request = new ArtifactRequest( artifact, null, "" );

        try
        {
            resolver.resolveArtifact( session, request );
            fail( "expected exception" );
        }
        catch ( ArtifactResolutionException e )
        {
            assertNotNull( e.getResults() );
            assertEquals( 1, e.getResults().size() );

            ArtifactResult result = e.getResults().get( 0 );

            assertSame( request, result.getRequest() );

            assertFalse( result.getExceptions().isEmpty() );
            assertTrue( result.getExceptions().get( 0 ) instanceof ArtifactNotFoundException );

            Artifact resolved = result.getArtifact();
            assertNull( resolved );
        }

    }

    @Test
    public void testResolveRemoteArtifact()
        throws IOException, ArtifactResolutionException
    {
        connector.setExpectGet( artifact );

        ArtifactRequest request = new ArtifactRequest( artifact, null, "" );
        request.addRepository( new RemoteRepository( "id", "default", "file:///" ) );

        ArtifactResult result = resolver.resolveArtifact( session, request );

        assertTrue( result.getExceptions().isEmpty() );

        Artifact resolved = result.getArtifact();
        assertNotNull( resolved.getFile() );

        resolved = resolved.setFile( null );
        assertEquals( artifact, resolved );

        connector.assertSeenExpected();
    }

    @Test
    public void testResolveRemoteArtifactUnsuccessful()
        throws IOException, ArtifactResolutionException
    {
        RecordingRepositoryConnector connector = new RecordingRepositoryConnector()
        {

            @Override
            public void get( Collection<? extends ArtifactDownload> artifactDownloads,
                             Collection<? extends MetadataDownload> metadataDownloads )
            {
                super.get( artifactDownloads, metadataDownloads );
                ArtifactDownload download = artifactDownloads.iterator().next();
                ArtifactTransferException exception =
                    new ArtifactNotFoundException( download.getArtifact(), null, "not found" );
                download.setException( exception );
            }

        };

        connector.setExpectGet( artifact );
        remoteRepositoryManager.setConnector( connector );

        ArtifactRequest request = new ArtifactRequest( artifact, null, "" );
        request.addRepository( new RemoteRepository( "id", "default", "file:///" ) );

        try
        {
            resolver.resolveArtifact( session, request );
            fail( "expected exception" );
        }
        catch ( ArtifactResolutionException e )
        {
            connector.assertSeenExpected();
            assertNotNull( e.getResults() );
            assertEquals( 1, e.getResults().size() );

            ArtifactResult result = e.getResults().get( 0 );

            assertSame( request, result.getRequest() );

            assertFalse( result.getExceptions().isEmpty() );
            assertTrue( result.getExceptions().get( 0 ) instanceof ArtifactNotFoundException );

            Artifact resolved = result.getArtifact();
            assertNull( resolved );
        }

    }

    @Test
    public void testResolveFromWorkspace()
        throws IOException, ArtifactResolutionException
    {
        session = new TestRepositorySystemSession()
        {
            @Override
            public WorkspaceReader getWorkspaceReader()
            {
                return new WorkspaceReader()
                {

                    public WorkspaceRepository getRepository()
                    {
                        return new WorkspaceRepository( "default" );
                    }

                    public List<String> findVersions( Artifact artifact )
                    {
                        return Arrays.asList( artifact.getVersion() );
                    }

                    public File findArtifact( Artifact artifact )
                    {
                        try
                        {
                            return TestFileUtils.createTempFile( artifact.toString() );
                        }
                        catch ( IOException e )
                        {
                            throw new RuntimeException( e.getMessage(), e );
                        }
                    }
                };
            }
        };

        ArtifactRequest request = new ArtifactRequest( artifact, null, "" );
        request.addRepository( new RemoteRepository( "id", "default", "file:///" ) );

        ArtifactResult result = resolver.resolveArtifact( session, request );

        assertTrue( result.getExceptions().isEmpty() );

        Artifact resolved = result.getArtifact();
        assertNotNull( resolved.getFile() );

        byte[] expected = resolved.toString().getBytes( "UTF-8" );
        TestFileUtils.assertContent( expected, resolved.getFile() );

        resolved = resolved.setFile( null );
        assertEquals( artifact, resolved );

        connector.assertSeenExpected();
    }

    @Test
    public void testResolveFromWorkspaceFallbackToRepository()
        throws IOException, ArtifactResolutionException
    {
        session = new TestRepositorySystemSession()
        {
            @Override
            public WorkspaceReader getWorkspaceReader()
            {
                return new WorkspaceReader()
                {

                    public WorkspaceRepository getRepository()
                    {
                        return new WorkspaceRepository( "default" );
                    }

                    public List<String> findVersions( Artifact artifact )
                    {
                        return Arrays.asList( artifact.getVersion() );
                    }

                    public File findArtifact( Artifact artifact )
                    {
                        return null;
                    }
                };
            }
        };

        connector.setExpectGet( artifact );
        remoteRepositoryManager.setConnector( connector );

        ArtifactRequest request = new ArtifactRequest( artifact, null, "" );
        request.addRepository( new RemoteRepository( "id", "default", "file:///" ) );

        ArtifactResult result = resolver.resolveArtifact( session, request );

        assertTrue( "exception on resolveArtifact", result.getExceptions().isEmpty() );

        Artifact resolved = result.getArtifact();
        assertNotNull( resolved.getFile() );

        resolved = resolved.setFile( null );
        assertEquals( artifact, resolved );

        connector.assertSeenExpected();
    }

    @Test
    public void testRepositoryEventsSuccessfulLocal()
        throws ArtifactResolutionException, IOException
    {
        RecordingRepositoryListener listener = new RecordingRepositoryListener();
        session.setRepositoryListener( listener );

        File tmpFile = TestFileUtils.createTempFile( "tmp" );
        Map<String, String> properties = new HashMap<String, String>();
        properties.put( ArtifactProperties.LOCAL_PATH, tmpFile.getAbsolutePath() );
        artifact = artifact.setProperties( properties );

        ArtifactRequest request = new ArtifactRequest( artifact, null, "" );
        resolver.resolveArtifact( session, request );

        List<EventWrapper> events = listener.getEvents();
        assertEquals( 2, events.size() );
        EventWrapper event = events.get( 0 );
        assertEquals( RecordingRepositoryListener.Type.ARTIFACT_RESOLVING, event.getType() );
        assertNull( event.getEvent().getException() );
        assertEquals( artifact, event.getEvent().getArtifact() );

        event = events.get( 1 );
        assertEquals( RecordingRepositoryListener.Type.ARTIFACT_RESOLVED, event.getType() );
        assertNull( event.getEvent().getException() );
        assertEquals( artifact, event.getEvent().getArtifact().setFile( null ) );
    }

    @Test
    public void testRepositoryEventsUnsuccessfulLocal()
        throws IOException
    {
        RecordingRepositoryListener listener = new RecordingRepositoryListener();
        session.setRepositoryListener( listener );

        Map<String, String> properties = new HashMap<String, String>();
        properties.put( ArtifactProperties.LOCAL_PATH, "doesnotexist" );
        artifact = artifact.setProperties( properties );

        ArtifactRequest request = new ArtifactRequest( artifact, null, "" );
        try
        {
            resolver.resolveArtifact( session, request );
            fail( "expected exception" );
        }
        catch ( ArtifactResolutionException e )
        {
        }

        List<EventWrapper> events = listener.getEvents();
        assertEquals( 2, events.size() );

        EventWrapper event = events.get( 0 );
        assertEquals( artifact, event.getEvent().getArtifact() );
        assertEquals( Type.ARTIFACT_RESOLVING, event.getType() );

        event = events.get( 1 );
        assertEquals( artifact, event.getEvent().getArtifact() );
        assertEquals( Type.ARTIFACT_RESOLVED, event.getType() );
        assertNotNull( event.getEvent().getException() );
        assertEquals( 1, event.getEvent().getExceptions().size() );

    }

    @Test
    public void testRepositoryEventsSuccessfulRemote()
        throws ArtifactResolutionException
    {
        RecordingRepositoryListener listener = new RecordingRepositoryListener();
        session.setRepositoryListener( listener );

        ArtifactRequest request = new ArtifactRequest( artifact, null, "" );
        request.addRepository( new RemoteRepository( "id", "default", "file:///" ) );

        resolver.resolveArtifact( session, request );

        List<EventWrapper> events = listener.getEvents();
        assertEquals( 2, events.size() );
        EventWrapper event = events.get( 0 );
        assertEquals( RecordingRepositoryListener.Type.ARTIFACT_RESOLVING, event.getType() );
        assertNull( event.getEvent().getException() );
        assertEquals( artifact, event.getEvent().getArtifact() );

        event = events.get( 1 );
        assertEquals( RecordingRepositoryListener.Type.ARTIFACT_RESOLVED, event.getType() );
        assertNull( event.getEvent().getException() );
        assertEquals( artifact, event.getEvent().getArtifact().setFile( null ) );
    }

    @Test
    public void testRepositoryEventsUnsuccessfulRemote()
        throws IOException, ArtifactResolutionException
    {
        RecordingRepositoryConnector connector = new RecordingRepositoryConnector()
        {

            @Override
            public void get( Collection<? extends ArtifactDownload> artifactDownloads,
                             Collection<? extends MetadataDownload> metadataDownloads )
            {
                super.get( artifactDownloads, metadataDownloads );
                ArtifactDownload download = artifactDownloads.iterator().next();
                ArtifactTransferException exception =
                    new ArtifactNotFoundException( download.getArtifact(), null, "not found" );
                download.setException( exception );
            }

        };
        remoteRepositoryManager.setConnector( connector );

        RecordingRepositoryListener listener = new RecordingRepositoryListener();
        session.setRepositoryListener( listener );

        ArtifactRequest request = new ArtifactRequest( artifact, null, "" );
        request.addRepository( new RemoteRepository( "id", "default", "file:///" ) );

        try
        {
            resolver.resolveArtifact( session, request );
            fail( "expected exception" );
        }
        catch ( ArtifactResolutionException e )
        {
        }

        List<EventWrapper> events = listener.getEvents();
        assertEquals( 2, events.size() );

        EventWrapper event = events.get( 0 );
        assertEquals( artifact, event.getEvent().getArtifact() );
        assertEquals( Type.ARTIFACT_RESOLVING, event.getType() );

        event = events.get( 1 );
        assertEquals( artifact, event.getEvent().getArtifact() );
        assertEquals( Type.ARTIFACT_RESOLVED, event.getType() );
        assertNotNull( event.getEvent().getException() );
        assertEquals( 1, event.getEvent().getExceptions().size() );
    }

    @Test
    public void testVersionResolverFails()
    {
        resolver.setVersionResolver( new VersionResolver()
        {

            public VersionResult resolveVersion( RepositorySystemSession session, VersionRequest request )
                throws VersionResolutionException
            {
                throw new VersionResolutionException( new VersionResult( request ) );
            }
        } );

        ArtifactRequest request = new ArtifactRequest( artifact, null, "" );
        try
        {
            resolver.resolveArtifact( session, request );
            fail( "expected exception" );
        }
        catch ( ArtifactResolutionException e )
        {
            connector.assertSeenExpected();
            assertNotNull( e.getResults() );
            assertEquals( 1, e.getResults().size() );

            ArtifactResult result = e.getResults().get( 0 );

            assertSame( request, result.getRequest() );

            assertFalse( result.getExceptions().isEmpty() );
            assertTrue( result.getExceptions().get( 0 ) instanceof VersionResolutionException );

            Artifact resolved = result.getArtifact();
            assertNull( resolved );
        }
    }

    @Test
    public void testRepositoryEventsOnVersionResolverFail()
    {
        resolver.setVersionResolver( new VersionResolver()
        {

            public VersionResult resolveVersion( RepositorySystemSession session, VersionRequest request )
                throws VersionResolutionException
            {
                throw new VersionResolutionException( new VersionResult( request ) );
            }
        } );

        RecordingRepositoryListener listener = new RecordingRepositoryListener();
        session.setRepositoryListener( listener );

        ArtifactRequest request = new ArtifactRequest( artifact, null, "" );
        try
        {
            resolver.resolveArtifact( session, request );
            fail( "expected exception" );
        }
        catch ( ArtifactResolutionException e )
        {
        }

        List<EventWrapper> events = listener.getEvents();
        assertEquals( 2, events.size() );

        EventWrapper event = events.get( 0 );
        assertEquals( artifact, event.getEvent().getArtifact() );
        assertEquals( Type.ARTIFACT_RESOLVING, event.getType() );

        event = events.get( 1 );
        assertEquals( artifact, event.getEvent().getArtifact() );
        assertEquals( Type.ARTIFACT_RESOLVED, event.getType() );
        assertNotNull( event.getEvent().getException() );
        assertEquals( 1, event.getEvent().getExceptions().size() );
    }

    @Test
    public void testLocalArtifactAvailable()
        throws ArtifactResolutionException
    {
        session.setLocalRepositoryManager( new LocalRepositoryManager()
        {

            public LocalRepository getRepository()
            {
                return null;
            }

            public String getPathForRemoteMetadata( Metadata metadata, RemoteRepository repository, String context )
            {
                return null;
            }

            public String getPathForRemoteArtifact( Artifact artifact, RemoteRepository repository, String context )
            {
                return null;
            }

            public String getPathForLocalMetadata( Metadata metadata )
            {
                return null;
            }

            public String getPathForLocalArtifact( Artifact artifact )
            {
                return null;
            }

            public LocalArtifactResult find( RepositorySystemSession session, LocalArtifactRequest request )
            {

                LocalArtifactResult result = new LocalArtifactResult( request );
                result.setAvailable( true );
                try
                {
                    result.setFile( TestFileUtils.createTempFile( "" ) );
                }
                catch ( IOException e )
                {
                    e.printStackTrace();
                }
                return result;
            }

            public void add( RepositorySystemSession session, LocalArtifactRegistration request )
            {
            }
        } );

        ArtifactRequest request = new ArtifactRequest( artifact, null, "" );
        request.addRepository( new RemoteRepository( "id", "default", "file:///" ) );

        ArtifactResult result = resolver.resolveArtifact( session, request );

        assertTrue( result.getExceptions().isEmpty() );

        Artifact resolved = result.getArtifact();
        assertNotNull( resolved.getFile() );

        resolved = resolved.setFile( null );
        assertEquals( artifact, resolved );

    }

    @Test
    public void testFindInLocalRepository()
        throws ArtifactResolutionException
    {
        session.setLocalRepositoryManager( new LocalRepositoryManager()
        {

            public LocalRepository getRepository()
            {
                return null;
            }

            public String getPathForRemoteMetadata( Metadata metadata, RemoteRepository repository, String context )
            {
                return null;
            }

            public String getPathForRemoteArtifact( Artifact artifact, RemoteRepository repository, String context )
            {
                return null;
            }

            public String getPathForLocalMetadata( Metadata metadata )
            {
                return null;
            }

            public String getPathForLocalArtifact( Artifact artifact )
            {
                return null;
            }

            public LocalArtifactResult find( RepositorySystemSession session, LocalArtifactRequest request )
            {

                LocalArtifactResult result = new LocalArtifactResult( request );
                result.setAvailable( false );
                try
                {
                    result.setFile( TestFileUtils.createTempFile( "" ) );
                }
                catch ( IOException e )
                {
                    e.printStackTrace();
                }
                return result;
            }

            public void add( RepositorySystemSession session, LocalArtifactRegistration request )
            {
            }
        } );
        ArtifactRequest request = new ArtifactRequest( artifact, null, "" );
        request.addRepository( new RemoteRepository( "id", "default", "file:///" ) );

        resolver.setVersionResolver( new VersionResolver()
        {

            public VersionResult resolveVersion( RepositorySystemSession session, VersionRequest request )
                throws VersionResolutionException
            {
                return new VersionResult( request ).setRepository( new LocalRepository( "id" ) ).setVersion( request.getArtifact().getVersion() );
            }
        } );
        ArtifactResult result = resolver.resolveArtifact( session, request );

        assertTrue( result.getExceptions().isEmpty() );

        Artifact resolved = result.getArtifact();
        assertNotNull( resolved.getFile() );

        resolved = resolved.setFile( null );
        assertEquals( artifact, resolved );
    }
}
