package org.sonatype.aether.impl.internal;

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
import static org.sonatype.aether.test.impl.RecordingRepositoryListener.Type.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sonatype.aether.RepositoryEvent;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.installation.InstallRequest;
import org.sonatype.aether.installation.InstallResult;
import org.sonatype.aether.installation.InstallationException;
import org.sonatype.aether.metadata.Metadata;
import org.sonatype.aether.metadata.Metadata.Nature;
import org.sonatype.aether.test.impl.RecordingRepositoryListener;
import org.sonatype.aether.test.impl.TestFileProcessor;
import org.sonatype.aether.test.impl.RecordingRepositoryListener.EventWrapper;
import org.sonatype.aether.test.impl.TestRepositorySystemSession;
import org.sonatype.aether.test.util.TestFileUtils;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.util.listener.AbstractRepositoryListener;
import org.sonatype.aether.util.metadata.DefaultMetadata;

public class DefaultInstallerTest
{

    private Artifact artifact;

    private DefaultMetadata metadata;

    private TestRepositorySystemSession session;

    private String localArtifactPath;

    private String localMetadataPath;

    private DefaultInstaller installer;

    private InstallRequest request;

    private RecordingRepositoryListener listener;

    @Before
    public void setup()
        throws IOException
    {
        artifact = new DefaultArtifact( "gid", "aid", "jar", "ver" );
        artifact = artifact.setFile( TestFileUtils.createTempFile( "artifact".getBytes(), 1 ) );
        metadata =
            new DefaultMetadata( "gid", "aid", "ver", "type", Nature.RELEASE_OR_SNAPSHOT,
                                 TestFileUtils.createTempFile( "metadata".getBytes(), 1 ) );

        session = new TestRepositorySystemSession();
        localArtifactPath = session.getLocalRepositoryManager().getPathForLocalArtifact( artifact );
        localMetadataPath = session.getLocalRepositoryManager().getPathForLocalMetadata( metadata );

        installer = new DefaultInstaller().setFileProcessor( TestFileProcessor.INSTANCE );
        request = new InstallRequest();
        listener = new RecordingRepositoryListener();
        session.setRepositoryListener( listener );
    }

    @After
    public void teardown()
    {
        TestFileUtils.deleteDir( session.getLocalRepository().getBasedir() );
    }

    @Test
    public void testSuccessfulInstall()
        throws InstallationException
    {
        File artifactFile =
            new File( session.getLocalRepositoryManager().getRepository().getBasedir(), localArtifactPath );
        File metadataFile =
            new File( session.getLocalRepositoryManager().getRepository().getBasedir(), localMetadataPath );

        artifactFile.delete();
        metadataFile.delete();

        request.addArtifact( artifact );
        request.addMetadata( metadata );

        InstallResult result = installer.install( session, request );

        assertTrue( artifactFile.exists() );
        assertEquals( artifactFile.length(), 8 );

        assertTrue( metadataFile.exists() );
        assertEquals( metadataFile.length(), 8 );

        assertEquals( result.getRequest(), request );

        assertEquals( result.getArtifacts().size(), 1 );
        assertTrue( result.getArtifacts().contains( artifact ) );

        assertEquals( result.getMetadata().size(), 1 );
        assertTrue( result.getMetadata().contains( metadata ) );
    }

    @Test( expected = InstallationException.class )
    public void testNullArtifactFile()
        throws InstallationException
    {
        InstallRequest request = new InstallRequest();
        request.addArtifact( artifact.setFile( null ) );

        DefaultInstaller installer = new DefaultInstaller();
        installer.install( session, request );
    }

    @Test( expected = InstallationException.class )
    public void testNullMetadataFile()
        throws InstallationException
    {
        InstallRequest request = new InstallRequest();
        request.addMetadata( metadata.setFile( null ) );

        DefaultInstaller installer = new DefaultInstaller();
        installer.install( session, request );
    }

    @Test( expected = InstallationException.class )
    public void testArtifactExistsAsDir()
        throws InstallationException
    {
        String path = session.getLocalRepositoryManager().getPathForLocalArtifact( artifact );
        assertTrue( "failed to setup test: could not create " + path,
                    new File( session.getLocalRepository().getBasedir(), path ).mkdirs() );

        request.addArtifact( artifact );
        new DefaultInstaller().install( session, request );
    }

    @Test( expected = InstallationException.class )
    public void testMetadataExistsAsDir()
        throws InstallationException
    {
        String path = session.getLocalRepositoryManager().getPathForLocalMetadata( metadata );
        assertTrue( "failed to setup test: could not create " + path,
                    new File( session.getLocalRepository().getBasedir(), path ).mkdirs() );

        request.addMetadata( metadata );
        new DefaultInstaller().install( session, request );
    }

    @Test
    public void testSuccessfulEvents()
        throws InstallationException
    {
        InstallRequest request = new InstallRequest();
        request.addArtifact( artifact );
        request.addMetadata( metadata );

        session.setRepositoryListener( new AbstractRepositoryListener()
        {
            private boolean seenArtifactInstalling = false;

            private boolean seenMetadataInstalling = false;

            @Override
            public void artifactInstalled( RepositoryEvent event )
            {
                File artifactFile =
                    new File( session.getLocalRepositoryManager().getRepository().getBasedir(), localArtifactPath );

                assertTrue( seenArtifactInstalling );
                assertEquals( artifactFile, event.getFile() );
                assertEquals( event.getArtifact(), artifact );
                assertNull( String.valueOf( event.getException() ), event.getException() );
            }

            @Override
            public void artifactInstalling( RepositoryEvent event )
            {
                this.seenArtifactInstalling = true;
                File artifactFile =
                    new File( session.getLocalRepositoryManager().getRepository().getBasedir(), localArtifactPath );

                assertEquals( artifactFile, event.getFile() );
                assertEquals( event.getArtifact(), artifact );
                assertNull( String.valueOf( event.getException() ), event.getException() );
            }

            @Override
            public void metadataInstalled( RepositoryEvent event )
            {
                File metadataFile =
                    new File( session.getLocalRepositoryManager().getRepository().getBasedir(), localMetadataPath );

                assertTrue( seenMetadataInstalling );
                assertEquals( metadataFile, event.getFile() );
                assertEquals( event.getMetadata(), metadata );
                assertNull( String.valueOf( event.getException() ), event.getException() );
            }

            @Override
            public void metadataInstalling( RepositoryEvent event )
            {
                this.seenMetadataInstalling = true;
                File metadataFile =
                    new File( session.getLocalRepositoryManager().getRepository().getBasedir(), localMetadataPath );

                assertEquals( metadataFile, event.getFile() );
                assertEquals( event.getMetadata(), metadata );
                assertNull( String.valueOf( event.getException() ), event.getException() );
            }

        } );

        installer.install( session, request );
    }


    @Test
    public void testFailingEventsNullArtifactFile()
    {
        checkFailedEvents( "null artifact file", this.artifact.setFile( null ) );
    }

    @Test
    public void testFailingEventsNullMetadataFile()
    {
        checkFailedEvents( "null metadata file", this.metadata.setFile( null ) );
    }

    @Test
    public void testFailingEventsArtifactExistsAsDir()
    {
        String path = session.getLocalRepositoryManager().getPathForLocalArtifact( artifact );
        assertTrue( "failed to setup test: could not create " + path,
                    new File( session.getLocalRepository().getBasedir(), path ).mkdirs() );
        checkFailedEvents( "target exists as dir", artifact );
    }

    @Test
    public void testFailingEventsMetadataExistsAsDir()
    {
        String path = session.getLocalRepositoryManager().getPathForLocalMetadata( metadata );
        assertTrue( "failed to setup test: could not create " + path,
                    new File( session.getLocalRepository().getBasedir(), path ).mkdirs() );
        checkFailedEvents( "target exists as dir", metadata );
    }

    private void checkFailedEvents( String msg, Metadata metadata )
    {
        InstallRequest request = new InstallRequest().addMetadata( metadata );
        msg = "Repository events problem (case: " + msg + ")";

        try
        {
            installer.install( session, request );
            fail( "expected exception" );
        }
        catch ( InstallationException e )
        {
            List<EventWrapper> events = listener.getEvents();
            assertEquals( msg, 2, events.size() );
            EventWrapper wrapper = events.get( 0 );
            assertEquals( msg, METADATA_INSTALLING, wrapper.getType() );

            RepositoryEvent event = wrapper.getEvent();
            assertEquals( msg, metadata, event.getMetadata() );
            assertNull( msg, event.getException() );

            wrapper = events.get( 1 );
            assertEquals( msg, METADATA_INSTALLED, wrapper.getType() );
            event = wrapper.getEvent();
            assertEquals( msg, metadata, event.getMetadata() );
            assertNotNull( msg, event.getException() );
        }

    }

    private void checkFailedEvents( String msg, Artifact artifact )
    {
        InstallRequest request = new InstallRequest().addArtifact( artifact );
        msg = "Repository events problem (case: " + msg + ")";

        try
        {
            installer.install( session, request );
            fail( "expected exception" );
        }
        catch ( InstallationException e )
        {
            List<EventWrapper> events = listener.getEvents();
            assertEquals( msg, 2, events.size() );
            EventWrapper wrapper = events.get( 0 );
            assertEquals( msg, ARTIFACT_INSTALLING, wrapper.getType() );
            
            RepositoryEvent event = wrapper.getEvent();
            assertEquals( msg, artifact, event.getArtifact() );
            assertNull( msg, event.getException() );
            
            wrapper = events.get( 1 );
            assertEquals( msg, ARTIFACT_INSTALLED, wrapper.getType() );
            event = wrapper.getEvent();
            assertEquals( msg, artifact, event.getArtifact() );
            assertNotNull( msg, event.getException() );
        }
    }
}
