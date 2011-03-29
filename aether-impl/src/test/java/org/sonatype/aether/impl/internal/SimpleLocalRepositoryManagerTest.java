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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.test.util.TestFileUtils;
import org.sonatype.aether.util.artifact.DefaultArtifact;

/**
 * @author Benjamin Bentmann
 */
public class SimpleLocalRepositoryManagerTest
{

    private File basedir;

    private SimpleLocalRepositoryManager manager;

    @Before
    public void setup()
        throws IOException
    {
        basedir = TestFileUtils.createTempDir( "simple-repo" );
        manager = new SimpleLocalRepositoryManager( basedir );
    }

    @After
    public void tearDown()
        throws Exception
    {
        manager = null;
        TestFileUtils.delete( basedir );
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

}
