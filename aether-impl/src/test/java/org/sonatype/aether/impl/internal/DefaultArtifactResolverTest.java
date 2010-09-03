package org.sonatype.aether.impl.internal;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.impl.LocalRepositoryMaintainer;
import org.sonatype.aether.impl.UpdateCheckManager;
import org.sonatype.aether.impl.VersionResolver;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.ArtifactRequest;
import org.sonatype.aether.resolution.ArtifactResolutionException;
import org.sonatype.aether.resolution.ArtifactResult;
import org.sonatype.aether.spi.connector.ArtifactDownload;
import org.sonatype.aether.spi.connector.MetadataDownload;
import org.sonatype.aether.spi.log.NullLogger;
import org.sonatype.aether.test.impl.TestRepositorySystemSession;
import org.sonatype.aether.test.util.DependencyGraphParser;
import org.sonatype.aether.test.util.FileUtil;
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
    }

    @Test
    public void testResolveLocalArtifactSuccessful()
        throws IOException, ArtifactResolutionException
    {
        DependencyNode node = parser.parseLiteral( "gid:aid:ext:ver" );
        Artifact artifact = node.getDependency().getArtifact();

        File tmpFile = FileUtil.createTempFile( "tmp" );
        Map<String, String> properties = new HashMap<String, String>();
        properties.put( ArtifactProperties.LOCAL_PATH, tmpFile.getAbsolutePath() );
        artifact = artifact.setProperties( properties );

        ArtifactRequest request = new ArtifactRequest( artifact, null, "" );
        ArtifactResult result = resolver.resolveArtifact( session, request );

        assertTrue( result.getExceptions().isEmpty() );

        Artifact resolved = result.getArtifact();
        assertEquals( artifact, resolved );

    }

    @Test( expected = ArtifactResolutionException.class )
    public void testResolveLocalArtifactUnsuccessful()
        throws IOException, ArtifactResolutionException
    {
        DependencyNode node = parser.parseLiteral( "gid:aid:ext:ver" );
        Artifact artifact = node.getDependency().getArtifact();

        File tmpFile = FileUtil.createTempFile( "tmp" );
        Map<String, String> properties = new HashMap<String, String>();
        properties.put( ArtifactProperties.LOCAL_PATH, tmpFile.getAbsolutePath() );
        artifact = artifact.setProperties( properties );

        tmpFile.delete();

        ArtifactRequest request = new ArtifactRequest( artifact, null, "" );
        ArtifactResult result = resolver.resolveArtifact( session, request );

        assertTrue( result.getExceptions().isEmpty() );

        Artifact resolved = result.getArtifact();
        assertEquals( artifact, resolved );

    }

    @Test
    public void testResolveRemoteArtifact()
        throws IOException, ArtifactResolutionException
    {
        DependencyNode node = parser.parseLiteral( "gid:aid:ext:ver" );
        Artifact artifact = node.getDependency().getArtifact();
        RecordingRepositoryConnector connector =
            new RecordingRepositoryConnector( Collections.singleton( artifact ).toArray( new Artifact[0] ), null, null,
                                              null );
        remoteRepositoryManager.setConnector( connector );

        ArtifactRequest request = new ArtifactRequest( artifact, null, "" );
        request.addRepository( new RemoteRepository( "id", "default", "file:///" ) );

        ArtifactResult result = resolver.resolveArtifact( session, request );

        assertTrue( result.getExceptions().isEmpty() );

        Artifact resolved = result.getArtifact();
        assertEquals( artifact, resolved );

        connector.assertSeenExpected();
    }

    @Test( expected = ArtifactResolutionException.class )
    public void testResolveRemoteArtifactUnsuccessful()
        throws IOException, ArtifactResolutionException
    {
        DependencyNode node = parser.parseLiteral( "gid:aid:ext:ver" );
        Artifact artifact = node.getDependency().getArtifact();
        RecordingRepositoryConnector connector = new RecordingRepositoryConnector( null, null, null, null )
        {

            @Override
            public void get( Collection<? extends ArtifactDownload> artifactDownloads,
                             Collection<? extends MetadataDownload> metadataDownloads )
            {
                super.get( artifactDownloads, metadataDownloads );
                ArtifactDownload download = artifactDownloads.iterator().next();
                ArtifactTransferException exception =
                    new ArtifactTransferException( download.getArtifact(), null, "not found" );
                download.setException( exception );
            }

        };
        remoteRepositoryManager.setConnector( connector );

        ArtifactRequest request = new ArtifactRequest( artifact, null, "" );
        request.addRepository( new RemoteRepository( "id", "default", "file:///" ) );

        ArtifactResult result = resolver.resolveArtifact( session, request );

        assertTrue( result.getExceptions().isEmpty() );

        Artifact resolved = result.getArtifact();
        assertEquals( artifact, resolved );

    }
}
