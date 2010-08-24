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

import org.junit.Before;
import org.junit.Test;
import org.sonatype.aether.RepositoryEvent;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.installation.InstallRequest;
import org.sonatype.aether.installation.InstallResult;
import org.sonatype.aether.installation.InstallationException;
import org.sonatype.aether.metadata.Metadata.Nature;
import org.sonatype.aether.test.impl.TestRepositorySystemSession;
import org.sonatype.aether.test.util.FileUtil;
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

    @Before
    public void setup()
        throws IOException
    {
        artifact = new DefaultArtifact( "gid", "aid", "jar", "ver" );
        artifact = artifact.setFile( FileUtil.createTempFile( "artifact".getBytes(), 1 ) );
        metadata =
            new DefaultMetadata( "gid", "aid", "ver", "type", Nature.RELEASE_OR_SNAPSHOT,
                                 FileUtil.createTempFile( "metadata".getBytes(), 1 ) );

        session = new TestRepositorySystemSession();
        localArtifactPath = session.getLocalRepositoryManager().getPathForLocalArtifact( artifact );
        localMetadataPath = session.getLocalRepositoryManager().getPathForLocalMetadata( metadata );

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

        InstallRequest request = new InstallRequest();
        request.addArtifact( artifact );
        request.addMetadata( metadata );

        DefaultInstaller installer = new DefaultInstaller();
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

    @Test
    public void testEvents()
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
            }

            @Override
            public void artifactInstalling( RepositoryEvent event )
            {
                this.seenArtifactInstalling = true;
                File artifactFile =
                    new File( session.getLocalRepositoryManager().getRepository().getBasedir(), localArtifactPath );

                assertEquals( artifactFile, event.getFile() );
                assertEquals( event.getArtifact(), artifact );
            }

            @Override
            public void metadataInstalled( RepositoryEvent event )
            {
                File metadataFile =
                    new File( session.getLocalRepositoryManager().getRepository().getBasedir(), localMetadataPath );

                assertTrue( seenMetadataInstalling );
                assertEquals( metadataFile, event.getFile() );
                assertEquals( event.getMetadata(), metadata );
            }

            @Override
            public void metadataInstalling( RepositoryEvent event )
            {
                this.seenMetadataInstalling = true;
                File metadataFile =
                    new File( session.getLocalRepositoryManager().getRepository().getBasedir(), localMetadataPath );

                assertEquals( metadataFile, event.getFile() );
                assertEquals( event.getMetadata(), metadata );
            }

        } );

        DefaultInstaller installer = new DefaultInstaller();
        installer.install( session, request );
    }
}
