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
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.metadata.Metadata;
import org.sonatype.aether.metadata.Metadata.Nature;
import org.sonatype.aether.repository.LocalArtifactRegistration;
import org.sonatype.aether.repository.LocalArtifactRequest;
import org.sonatype.aether.repository.LocalArtifactResult;
import org.sonatype.aether.repository.LocalMetadataRequest;
import org.sonatype.aether.repository.LocalMetadataResult;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.test.impl.TestRepositorySystemSession;
import org.sonatype.aether.test.util.TestFileUtils;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.util.metadata.DefaultMetadata;

public class EnhancedLocalRepositoryManagerTest
{

    private Artifact artifact;

    private File baseDir;

    private EnhancedLocalRepositoryManager manager;

    private File artifactFile;

    private RemoteRepository repository;

    private String testContext = "project/compile";

    private RepositorySystemSession session;

    private Metadata metadata;

    private Metadata noVerMetadata;

    @Before
    public void setup()
        throws IOException
    {
        repository =
            new RemoteRepository( "enhanced-remote-repo", "default",
                                  TestFileUtils.createTempDir( "enhanced-remote-repo" ).toURI().toURL().toString() );
        repository.setRepositoryManager( true );

        artifact =
            new DefaultArtifact( "gid", "aid", "", "jar", "1-test", Collections.<String, String> emptyMap(),
                                 TestFileUtils.createTempFile( "artifact" ) );

        metadata =
            new DefaultMetadata( "gid", "aid", "1-test", "maven-metadata.xml", Nature.RELEASE,
                                 TestFileUtils.createTempFile( "metadata" ) );

        noVerMetadata =
            new DefaultMetadata( "gid", "aid", null, "maven-metadata.xml", Nature.RELEASE,
                                 TestFileUtils.createTempFile( "metadata" ) );

        baseDir = TestFileUtils.createTempDir( "enhanced-repo" );
        manager = new EnhancedLocalRepositoryManager( baseDir );

        artifactFile = new File( baseDir, manager.getPathForLocalArtifact( artifact ) );

        session = new TestRepositorySystemSession();
    }

    @After
    public void tearDown()
        throws Exception
    {
        TestFileUtils.delete( baseDir );
        TestFileUtils.delete( new File( new URI( repository.getUrl() ) ) );

        session = null;
        manager = null;
        repository = null;
        artifact = null;
    }

    private long addLocalArtifact( Artifact artifact )
        throws IOException
    {
        manager.add( session, new LocalArtifactRegistration( artifact ) );
        String path = manager.getPathForLocalArtifact( artifact );

        return copy( artifact, path );
    }

    private long addRemoteArtifact( Artifact artifact )
        throws IOException
    {
        Collection<String> contexts = Arrays.asList( testContext );
        manager.add( session, new LocalArtifactRegistration( artifact, repository, contexts ) );
        String path = manager.getPathForRemoteArtifact( artifact, repository, testContext );
        return copy( artifact, path );
    }
    
    private long copy( Metadata metadata, String path )
        throws IOException
    {
        if ( metadata.getFile() == null )
        {
            return -1;
        }
        return TestFileUtils.copy( metadata.getFile(), new File( baseDir, path ) );
    }

    private long copy( Artifact artifact, String path )
        throws IOException
    {
        if ( artifact.getFile() == null )
        {
            return -1;
        }
        File artifactFile = new File( baseDir, path );
        return TestFileUtils.copy( artifact.getFile(), artifactFile );
    }

    @Test
    public void testFindLocalArtifact()
        throws IOException
    {
        addLocalArtifact( artifact );

        LocalArtifactRequest request = new LocalArtifactRequest( artifact, null, null );
        LocalArtifactResult result = manager.find( session, request );
        assertTrue( result.isAvailable() );
    }

    @Test
    public void testFindRemoteArtifact()
        throws IOException
    {
        addRemoteArtifact( artifact );

        LocalArtifactRequest request = new LocalArtifactRequest( artifact, Arrays.asList( repository ), testContext );
        LocalArtifactResult result = manager.find( session, request );
        assertTrue( result.isAvailable() );
    }

    @Test
    public void testDoNotFindDifferentContext()
        throws IOException
    {
        addRemoteArtifact( artifact );

        LocalArtifactRequest request = new LocalArtifactRequest( artifact, Arrays.asList( repository ), "different" );
        LocalArtifactResult result = manager.find( session, request );
        assertFalse( result.isAvailable() );
    }

    @Test
    public void testDoNotFindNullFile()
        throws IOException
    {
        artifact = artifact.setFile( null );
        addLocalArtifact( artifact );

        LocalArtifactRequest request = new LocalArtifactRequest( artifact, Arrays.asList( repository ), testContext );
        LocalArtifactResult result = manager.find( session, request );
        assertFalse( result.isAvailable() );
    }

    @Test
    public void testDoNotFindDeletedFile()
        throws IOException
    {
        addLocalArtifact( artifact );
        assertTrue( "could not delete artifact file", artifactFile.delete() );

        LocalArtifactRequest request = new LocalArtifactRequest( artifact, Arrays.asList( repository ), testContext );
        LocalArtifactResult result = manager.find( session, request );
        assertFalse( result.isAvailable() );
    }

    @Test
    public void findUntrackedFile()
        throws IOException
    {
        copy( artifact, manager.getPathForLocalArtifact( artifact ) );

        LocalArtifactRequest request = new LocalArtifactRequest( artifact, Arrays.asList( repository ), testContext );
        LocalArtifactResult result = manager.find( session, request );
        assertTrue( result.isAvailable() );
    }

    private long addMetadata( Metadata metadata, RemoteRepository repo )
        throws IOException
    {
        String path;
        if ( repo == null )
        {
            path = manager.getPathForLocalMetadata( metadata );
        }
        else
        {
            path = manager.getPathForRemoteMetadata( metadata, repo, testContext );
        }
        System.err.println( path );

        return copy( metadata, path );
    }

    @Test
    public void findLocalMetadata()
        throws IOException
    {
        addMetadata( metadata, null );

        LocalMetadataRequest request = new LocalMetadataRequest( metadata, null, testContext );
        LocalMetadataResult result = manager.find( session, request );

        assertNotNull( result.getFile() );
    }

    @Test
    public void findLocalMetadataNoVersion()
        throws IOException
    {
        addMetadata( noVerMetadata, null );

        LocalMetadataRequest request = new LocalMetadataRequest( noVerMetadata, null, testContext );
        LocalMetadataResult result = manager.find( session, request );

        assertNotNull( result.getFile() );
    }

    @Test
    public void doNotFindRemoteMetadataDifferentContext()
        throws IOException
    {
        addMetadata( noVerMetadata, repository );
        addMetadata( metadata, repository );

        LocalMetadataRequest request = new LocalMetadataRequest( noVerMetadata, repository, "different" );
        LocalMetadataResult result = manager.find( session, request );
        assertNull( result.getFile() );

        request = new LocalMetadataRequest( metadata, repository, "different" );
        result = manager.find( session, request );
        assertNull( result.getFile() );
    }
}
