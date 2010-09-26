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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.LocalArtifactRegistration;
import org.sonatype.aether.repository.LocalArtifactRequest;
import org.sonatype.aether.repository.LocalArtifactResult;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.test.impl.TestRepositorySystemSession;
import org.sonatype.aether.test.util.TestFileUtils;
import org.sonatype.aether.util.artifact.DefaultArtifact;

public class EnhancedLocalRepositoryManagerTest
{

    private Artifact artifact;

    private File baseDir;

    private EnhancedLocalRepositoryManager manager;

    private File artifactFile;

    private RemoteRepository repository;

    private String testContext = "project/compile";

    private RepositorySystemSession session;

    @Before
    public void setup()
        throws IOException
    {
        repository =
            new RemoteRepository( "enhanced-remote-repo", "default",
                                  new File( "target/enhanced-remote-repo" ).toURI().toURL().toString() );
        repository.setRepositoryManager( true );

        artifact =
            new DefaultArtifact( "gid", "aid", "", "jar", "1-test", Collections.<String, String> emptyMap(),
                                 TestFileUtils.createTempFile( "artifact".getBytes(), 1 ) );
        baseDir = new File( "target/enhanced-repo" );
        manager = new EnhancedLocalRepositoryManager( baseDir );

        artifactFile = new File( baseDir, manager.getPathForLocalArtifact( artifact ) );

        session = new TestRepositorySystemSession();
    }

    @After
    public void tearDown()
    {
        TestFileUtils.deleteDir( baseDir );

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
}
