package org.sonatype.aether.impl.internal;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
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

    private File basedir;

    private EnhancedLocalRepositoryManager manager;

    private File artifactFile;

    private RemoteRepository repository;

    private String testContext = "project/compile";

    private RepositorySystemSession session;

    private Metadata metadata;

    private Metadata noVerMetadata;

    @Before
    public void setup()
        throws Exception
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

        basedir = TestFileUtils.createTempDir( "enhanced-repo" );
        manager = new EnhancedLocalRepositoryManager( basedir );

        artifactFile = new File( basedir, manager.getPathForLocalArtifact( artifact ) );

        session = new TestRepositorySystemSession();
    }

    @After
    public void tearDown()
        throws Exception
    {
        TestFileUtils.delete( basedir );
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
        return TestFileUtils.copy( metadata.getFile(), new File( basedir, path ) );
    }

    private long copy( Artifact artifact, String path )
        throws IOException
    {
        if ( artifact.getFile() == null )
        {
            return -1;
        }
        File artifactFile = new File( basedir, path );
        return TestFileUtils.copy( artifact.getFile(), artifactFile );
    }

    @Test
    public void testGetPathForLocalArtifact()
    {
        Artifact artifact = new DefaultArtifact( "g.i.d:a.i.d:1.0-SNAPSHOT" );
        assertEquals( "1.0-SNAPSHOT", artifact.getBaseVersion() );
        assertEquals( "g/i/d/a.i.d/1.0-SNAPSHOT/a.i.d-1.0-SNAPSHOT.jar", manager.getPathForLocalArtifact( artifact ) );

        artifact = new DefaultArtifact( "g.i.d:a.i.d:1.0-20110329.221805-4" );
        assertEquals( "1.0-SNAPSHOT", artifact.getBaseVersion() );
        assertEquals( "g/i/d/a.i.d/1.0-SNAPSHOT/a.i.d-1.0-SNAPSHOT.jar", manager.getPathForLocalArtifact( artifact ) );
    }

    @Test
    public void testGetPathForRemoteArtifact()
    {
        RemoteRepository remoteRepo = new RemoteRepository( "repo", "default", "ram:/void" );

        Artifact artifact = new DefaultArtifact( "g.i.d:a.i.d:1.0-SNAPSHOT" );
        assertEquals( "1.0-SNAPSHOT", artifact.getBaseVersion() );
        assertEquals( "g/i/d/a.i.d/1.0-SNAPSHOT/a.i.d-1.0-SNAPSHOT.jar",
                      manager.getPathForRemoteArtifact( artifact, remoteRepo, "" ) );

        artifact = new DefaultArtifact( "g.i.d:a.i.d:1.0-20110329.221805-4" );
        assertEquals( "1.0-SNAPSHOT", artifact.getBaseVersion() );
        assertEquals( "g/i/d/a.i.d/1.0-SNAPSHOT/a.i.d-1.0-20110329.221805-4.jar",
                      manager.getPathForRemoteArtifact( artifact, remoteRepo, "" ) );
    }

    @Test
    public void testFindLocalArtifact()
        throws Exception
    {
        addLocalArtifact( artifact );

        LocalArtifactRequest request = new LocalArtifactRequest( artifact, null, null );
        LocalArtifactResult result = manager.find( session, request );
        assertTrue( result.isAvailable() );
    }

    @Test
    public void testFindRemoteArtifact()
        throws Exception
    {
        addRemoteArtifact( artifact );

        LocalArtifactRequest request = new LocalArtifactRequest( artifact, Arrays.asList( repository ), testContext );
        LocalArtifactResult result = manager.find( session, request );
        assertTrue( result.isAvailable() );
    }

    @Test
    public void testDoNotFindDifferentContext()
        throws Exception
    {
        addRemoteArtifact( artifact );

        LocalArtifactRequest request = new LocalArtifactRequest( artifact, Arrays.asList( repository ), "different" );
        LocalArtifactResult result = manager.find( session, request );
        assertFalse( result.isAvailable() );
    }

    @Test
    public void testDoNotFindNullFile()
        throws Exception
    {
        artifact = artifact.setFile( null );
        addLocalArtifact( artifact );

        LocalArtifactRequest request = new LocalArtifactRequest( artifact, Arrays.asList( repository ), testContext );
        LocalArtifactResult result = manager.find( session, request );
        assertFalse( result.isAvailable() );
    }

    @Test
    public void testDoNotFindDeletedFile()
        throws Exception
    {
        addLocalArtifact( artifact );
        assertTrue( "could not delete artifact file", artifactFile.delete() );

        LocalArtifactRequest request = new LocalArtifactRequest( artifact, Arrays.asList( repository ), testContext );
        LocalArtifactResult result = manager.find( session, request );
        assertFalse( result.isAvailable() );
    }

    @Test
    public void testFindUntrackedFile()
        throws Exception
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
    public void testFindLocalMetadata()
        throws Exception
    {
        addMetadata( metadata, null );

        LocalMetadataRequest request = new LocalMetadataRequest( metadata, null, testContext );
        LocalMetadataResult result = manager.find( session, request );

        assertNotNull( result.getFile() );
    }

    @Test
    public void testFindLocalMetadataNoVersion()
        throws Exception
    {
        addMetadata( noVerMetadata, null );

        LocalMetadataRequest request = new LocalMetadataRequest( noVerMetadata, null, testContext );
        LocalMetadataResult result = manager.find( session, request );

        assertNotNull( result.getFile() );
    }

    @Test
    public void testDoNotFindRemoteMetadataDifferentContext()
        throws Exception
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

    @Test
    public void testFindArtifactUsesTimestampedVersion()
        throws Exception
    {
        Artifact artifact = new DefaultArtifact( "g.i.d:a.i.d:1.0-SNAPSHOT" );
        File file = new File( basedir, manager.getPathForLocalArtifact( artifact ) );
        TestFileUtils.write( "test", file );
        addLocalArtifact( artifact );

        artifact = artifact.setVersion( "1.0-20110329.221805-4" );
        LocalArtifactRequest request = new LocalArtifactRequest();
        request.setArtifact( artifact );
        LocalArtifactResult result = manager.find( session, request );
        assertNull( result.toString(), result.getFile() );
        assertFalse( result.toString(), result.isAvailable() );
    }

}
