package org.sonatype.aether.impl.internal;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.impl.LocalRepositoryMaintainer;
import org.sonatype.aether.impl.UpdateCheckManager;
import org.sonatype.aether.impl.VersionResolver;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.repository.WorkspaceReader;
import org.sonatype.aether.repository.WorkspaceRepository;
import org.sonatype.aether.resolution.ArtifactRequest;
import org.sonatype.aether.resolution.ArtifactResolutionException;
import org.sonatype.aether.resolution.ArtifactResult;
import org.sonatype.aether.spi.connector.ArtifactDownload;
import org.sonatype.aether.spi.connector.MetadataDownload;
import org.sonatype.aether.spi.log.NullLogger;
import org.sonatype.aether.test.impl.TestRepositorySystemSession;
import org.sonatype.aether.test.util.DependencyGraphParser;
import org.sonatype.aether.test.util.FileUtil;
import org.sonatype.aether.test.util.impl.StubArtifact;
import org.sonatype.aether.transfer.ArtifactNotFoundException;
import org.sonatype.aether.transfer.ArtifactTransferException;
import org.sonatype.aether.util.artifact.ArtifactProperties;

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

/**
 * @author Benjamin Hanzelmann
 */
public class DefaultArtifactResolverTest
{
    private DefaultArtifactResolver resolver;

    private TestRepositorySystemSession session;

    private DependencyGraphParser parser;

    private StubRemoteRepositoryManager remoteRepositoryManager;

    private Artifact artifact;

    private RecordingRepositoryConnector connector;

    @Before
    public void setup()
    {
        UpdateCheckManager updateCheckManager = new DoNothingUpdateCheckManager();
        List<LocalRepositoryMaintainer> localRepositoryMaintainers = null;
        remoteRepositoryManager = new StubRemoteRepositoryManager();
        VersionResolver versionResolver = new StubVersionResolver();
        session = new TestRepositorySystemSession();
        resolver =
            new DefaultArtifactResolver( NullLogger.INSTANCE, versionResolver, updateCheckManager,
                                         remoteRepositoryManager, localRepositoryMaintainers );

        parser = new DependencyGraphParser();

        artifact = new StubArtifact( "gid", "aid", "", "ext", "ver" );

        connector = new RecordingRepositoryConnector();
        remoteRepositoryManager.setConnector( connector );
    }

    @Test
    public void testResolveLocalArtifactSuccessful()
        throws IOException, ArtifactResolutionException
    {
        File tmpFile = FileUtil.createTempFile( "tmp" );
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
        File tmpFile = FileUtil.createTempFile( "tmp" );
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

            assertEquals( request, result.getRequest() );

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

            assertEquals( request, result.getRequest() );

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
                            return FileUtil.createTempFile( artifact.toString() );
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

        assertTrue( result.getExceptions().isEmpty() );

        Artifact resolved = result.getArtifact();
        assertNotNull( resolved.getFile() );

        resolved = resolved.setFile( null );
        assertEquals( artifact, resolved );

        connector.assertSeenExpected();
    }

}
