package org.sonatype.aether.connector.file;

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
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.metadata.Metadata.Nature;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.repository.RepositoryPolicy;
import org.sonatype.aether.spi.connector.ArtifactDownload;
import org.sonatype.aether.spi.connector.ArtifactUpload;
import org.sonatype.aether.spi.connector.MetadataDownload;
import org.sonatype.aether.spi.connector.MetadataUpload;
import org.sonatype.aether.test.impl.TestFileProcessor;
import org.sonatype.aether.test.impl.TestRepositorySystemSession;
import org.sonatype.aether.test.util.TestFileUtils;
import org.sonatype.aether.transfer.ArtifactTransferException;
import org.sonatype.aether.transfer.MetadataTransferException;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.util.metadata.DefaultMetadata;

public class ArtifactWorkerTest
{

    private static RemoteRepository repository;

    private static TestRepositorySystemSession session;

    private static DefaultLayout layout;

    @Before
    public void setup()
        throws MalformedURLException
    {
        repository = new RemoteRepository( "test", "default", new File( "target/test-repository" ).toURL().toString() );
        session = new TestRepositorySystemSession();
        layout = new DefaultLayout();
    }

    @After
    public void cleanup()
    {
        File dir = new File( "target/test-repository" );
        TestFileUtils.deleteDir( dir );
    }

    @Test
    public void testArtifactTransfer()
        throws IOException, ArtifactTransferException
    {
        DefaultArtifact artifact = new DefaultArtifact( "test", "artId1", "jar", "1" );
        String expectedContent = "Dies ist ein Test.";

        uploadArtifact( artifact, expectedContent );

        File file = downloadArtifact( artifact );

        assertContentEquals( file, expectedContent );
    }

    private File downloadArtifact( DefaultArtifact artifact )
        throws IOException, ArtifactTransferException
    {
        File file = TestFileUtils.createTempFile( "" );
        ArtifactDownload down = new ArtifactDownload( artifact, "", file, "" );
        down.setChecksumPolicy( RepositoryPolicy.CHECKSUM_POLICY_FAIL );
        FileRepositoryWorker worker = new FileRepositoryWorker( down, repository, session );
        worker.setFileProcessor( TestFileProcessor.INSTANCE );
        worker.run();
        if ( down.getException() != null )
        {
            throw down.getException();
        }
        return file;
    }

    private void uploadArtifact( Artifact artifact, String content )
        throws IOException, ArtifactTransferException
    {
        File file = TestFileUtils.createTempFile( content );

        ArtifactUpload transfer = new ArtifactUpload( artifact, file );
        FileRepositoryWorker worker = new FileRepositoryWorker( transfer, repository, session );
        worker.setFileProcessor( TestFileProcessor.INSTANCE );
        worker.run();

        file.delete();

        if ( transfer.getException() != null )
        {
            throw transfer.getException();
        }
    }

    @Test
    public void testMetadataTransfer()
        throws IOException, MetadataTransferException
    {
        File file = File.createTempFile( "ArtifactWorkerTest", ".jar" );
        file.deleteOnExit();
        FileWriter w = new FileWriter( file );
        String expectedContent = "Dies ist ein Test.";
        w.write( expectedContent );
        w.close();

        DefaultMetadata metadata = new DefaultMetadata( "test", "artId1", "1", "jar", Nature.RELEASE_OR_SNAPSHOT );
        MetadataUpload up = new MetadataUpload( metadata, file );
        FileRepositoryWorker worker = new FileRepositoryWorker( up, repository, session );
        worker.setFileProcessor( TestFileProcessor.INSTANCE );
        worker.run();
        if ( up.getException() != null )
        {
            throw up.getException();
        }

        file = File.createTempFile( "ArtifactWorkerTest", ".jar" );
        file.deleteOnExit();

        MetadataDownload down = new MetadataDownload();
        down.setChecksumPolicy( RepositoryPolicy.CHECKSUM_POLICY_FAIL );
        down.setMetadata( metadata ).setFile( file );
        worker = new FileRepositoryWorker( down, repository, session );
        worker.setFileProcessor( TestFileProcessor.INSTANCE );
        worker.run();

        if ( down.getException() != null )
        {
            throw down.getException();
        }

        assertTrue( file.exists() );

        assertContentEquals( file, expectedContent );
    }

    private void assertContentEquals( File file, String expectedContent )
        throws IOException
    {
        byte[] expected = expectedContent.getBytes( "UTF-8" );
        byte[] actual = TestFileUtils.getContent( file );

        assertArrayEquals( expected, actual );
    }

    @Test
    public void testDecodeURL()
        throws ArtifactTransferException, IOException
    {
        String repoDir = "target/%72%65%70%6F";
        repository = new RemoteRepository( "test", "default", repoDir );

        Artifact artifact = new DefaultArtifact( "gid", "aid", "jar", "ver" );
        String content = "test content";
        uploadArtifact( artifact, content );

        File repo = new File( "target/repo" );
        assertTrue( repo.exists() );
        assertTrue( new File( repo, layout.getPath( artifact ) ).exists() );
    }

}
