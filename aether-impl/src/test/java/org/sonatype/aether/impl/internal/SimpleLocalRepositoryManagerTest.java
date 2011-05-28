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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.LocalArtifactRequest;
import org.sonatype.aether.repository.LocalArtifactResult;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.test.impl.TestRepositorySystemSession;
import org.sonatype.aether.test.util.TestFileUtils;
import org.sonatype.aether.util.artifact.DefaultArtifact;

/**
 * @author Benjamin Bentmann
 */
public class SimpleLocalRepositoryManagerTest
{

    private File basedir;

    private SimpleLocalRepositoryManager manager;

    private RepositorySystemSession session;

    @Before
    public void setup()
        throws IOException
    {
        basedir = TestFileUtils.createTempDir( "simple-repo" );
        manager = new SimpleLocalRepositoryManager( basedir );
        session = new TestRepositorySystemSession();
    }

    @After
    public void tearDown()
        throws Exception
    {
        TestFileUtils.delete( basedir );
        manager = null;
        session = null;
    }

    @Test
    public void testGetPathForLocalArtifact()
        throws Exception
    {
        Artifact artifact = new DefaultArtifact( "g.i.d:a.i.d:1.0-SNAPSHOT" );
        assertEquals( "1.0-SNAPSHOT", artifact.getBaseVersion() );
        assertEquals( "g/i/d/a.i.d/1.0-SNAPSHOT/a.i.d-1.0-SNAPSHOT.jar", manager.getPathForLocalArtifact( artifact ) );

        artifact = new DefaultArtifact( "g.i.d:a.i.d:1.0-20110329.221805-4" );
        assertEquals( "1.0-SNAPSHOT", artifact.getBaseVersion() );
        assertEquals( "g/i/d/a.i.d/1.0-SNAPSHOT/a.i.d-1.0-SNAPSHOT.jar", manager.getPathForLocalArtifact( artifact ) );

        artifact = new DefaultArtifact( "g.i.d", "a.i.d", "", "", "1.0-SNAPSHOT" );
        assertEquals( "g/i/d/a.i.d/1.0-SNAPSHOT/a.i.d-1.0-SNAPSHOT", manager.getPathForLocalArtifact( artifact ) );
    }

    @Test
    public void testGetPathForRemoteArtifact()
        throws Exception
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
    public void testFindArtifactUsesTimestampedVersion()
        throws Exception
    {
        Artifact artifact = new DefaultArtifact( "g.i.d:a.i.d:1.0-SNAPSHOT" );
        File file = new File( basedir, manager.getPathForLocalArtifact( artifact ) );
        TestFileUtils.write( "test", file );

        artifact = artifact.setVersion( "1.0-20110329.221805-4" );
        LocalArtifactRequest request = new LocalArtifactRequest();
        request.setArtifact( artifact );
        LocalArtifactResult result = manager.find( session, request );
        assertNull( result.toString(), result.getFile() );
        assertFalse( result.toString(), result.isAvailable() );
    }

}
