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
package org.sonatype.aether.test.util.connector.suite;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.sonatype.aether.metadata.Metadata;
import org.sonatype.aether.repository.RepositoryPolicy;
import org.sonatype.aether.spi.connector.ArtifactDownload;
import org.sonatype.aether.spi.connector.ArtifactUpload;
import org.sonatype.aether.spi.connector.MetadataDownload;
import org.sonatype.aether.spi.connector.MetadataUpload;
import org.sonatype.aether.spi.connector.RepositoryConnector;
import org.sonatype.aether.spi.connector.Transfer;
import org.sonatype.aether.spi.connector.Transfer.State;
import org.sonatype.aether.test.util.FileUtil;
import org.sonatype.aether.test.util.impl.StubArtifact;
import org.sonatype.aether.test.util.impl.StubMetadata;
import org.sonatype.aether.transfer.NoRepositoryConnectorException;

/**
 * The ConnectorTestSuite bundles standard tests for {@link RepositoryConnector}s.
 * <p>
 * To use these tests, provide a (Junit4-)class extending this class, and provide a default constructor calling
 * {@link ConnectorTestSuite#ConnectorTestSuite(ConnectorTestSetup)} with a self-implemented {@link ConnectorTestSetup}.
 * 
 * @author Benjamin Hanzelmann
 */
public abstract class ConnectorTestSuite
    extends ConnectorTestSuiteSetup
{

    public ConnectorTestSuite( ConnectorTestSetup setup )
    {
        super( setup );
    }

    @Test
    public void testSuccessfulEvents()
        throws NoRepositoryConnectorException, IOException
    {
        TransferEventTester.testSuccessfulTransferEvents( factory(), session, repository );
    }

    @Test
    public void testFailedEvents()
        throws NoRepositoryConnectorException, IOException
    {
        TransferEventTester.testFailedTransferEvents( factory(), session, repository );
    }

    @Test
    public void testFileHandleLeakage()
        throws IOException, NoRepositoryConnectorException
    {

        StubArtifact artifact = new StubArtifact( "testGroup", "testArtifact", "jar", "", "1-test" );
        StubMetadata metadata =
            new StubMetadata( "testGroup", "testArtifact", "1-test", "maven-metadata.xml",
                              Metadata.Nature.RELEASE_OR_SNAPSHOT );

        RepositoryConnector connector = factory().newInstance( session, repository );

        File tmpFile = FileUtil.createTempFile( "testFileHandleLeakage" );
        ArtifactUpload artUp = new ArtifactUpload( artifact, tmpFile );
        connector.put( Arrays.asList( artUp ), null );
        assertTrue( "Leaking file handle in artifact upload", tmpFile.delete() );

        tmpFile = FileUtil.createTempFile( "testFileHandleLeakage" );
        MetadataUpload metaUp = new MetadataUpload( metadata, tmpFile );
        connector.put( null, Arrays.asList( metaUp ) );
        assertTrue( "Leaking file handle in metadata upload", tmpFile.delete() );

        tmpFile = FileUtil.createTempFile( "testFileHandleLeakage" );
        ArtifactDownload artDown = new ArtifactDownload( artifact, null, tmpFile, null );
        connector.get( Arrays.asList( artDown ), null );
        assertTrue( "Leaking file handle in artifact download", tmpFile.delete() );

        tmpFile = FileUtil.createTempFile( "testFileHandleLeakage" );
        MetadataDownload metaDown = new MetadataDownload( metadata, null, tmpFile, null );
        connector.get( null, Arrays.asList( metaDown ) );
        assertTrue( "Leaking file handle in metadata download", tmpFile.delete() );

    }

    @Test
    public void testBlocking()
        throws NoRepositoryConnectorException, IOException
    {

        RepositoryConnector connector = factory().newInstance( session, repository );

        int count = 10;

        byte[] pattern = "tmpFile".getBytes();
        File tmpFile = FileUtil.createTempFile( pattern, 100000 );

        List<ArtifactUpload> artUps = ConnectorTestUtil.createTransfers( ArtifactUpload.class, count, tmpFile );
        List<MetadataUpload> metaUps = ConnectorTestUtil.createTransfers( MetadataUpload.class, count, tmpFile );
        List<ArtifactDownload> artDowns = ConnectorTestUtil.createTransfers( ArtifactDownload.class, count, null );
        List<MetadataDownload> metaDowns = ConnectorTestUtil.createTransfers( MetadataDownload.class, count, null );

        // this should block until all transfers are done - racing condition, better way to test this?
        connector.put( artUps, metaUps );
        connector.get( artDowns, metaDowns );

        for ( int i = 0; i < count; i++ )
        {
            ArtifactUpload artUp = artUps.get( i );
            MetadataUpload metaUp = metaUps.get( i );
            ArtifactDownload artDown = artDowns.get( i );
            MetadataDownload metaDown = metaDowns.get( i );

            assertTrue( Transfer.State.DONE.equals( artUp.getState() ) );
            assertTrue( Transfer.State.DONE.equals( artDown.getState() ) );
            assertTrue( Transfer.State.DONE.equals( metaUp.getState() ) );
            assertTrue( Transfer.State.DONE.equals( metaDown.getState() ) );
        }

    }

    @Test
    public void testMkdirConcurrencyBug()
        throws IOException, NoRepositoryConnectorException
    {
        RepositoryConnector connector = factory().newInstance( session, repository );
        File tmpFile = FileUtil.createTempFile( "mkdirsBug" );
        StubArtifact art = new StubArtifact( "testGroup", "testArtifact", "jar", "", 1 + "-test" );
        StubMetadata meta =
            new StubMetadata( "testGroup", "testArtifact", 1 + "-test", "maven-metadata.xml",
                              Metadata.Nature.RELEASE_OR_SNAPSHOT );

        ArtifactUpload artUp = new ArtifactUpload( art, tmpFile );
        MetadataUpload metaUp = new MetadataUpload( meta, tmpFile );

        Collection<ArtifactUpload> artUps = Arrays.asList( artUp );
        Collection<MetadataUpload> metaUps = Arrays.asList( metaUp );
        connector.put( artUps, null );
        connector.put( null, metaUps );

        File artFile =
            new File(
                      "target/connector-test-suite/dir/with/many/sub/directories/to/test/concurrent/directory/creation/artifact.file" );
        File metaFile =
            new File(
                      "target/connector-test-suite/dir/with/many/sub/directories/to/test/concurrent/directory/creation/metadata.file" );

        for ( int i = 0; i < 100; i++ )
        {
            ArtifactDownload artDown = new ArtifactDownload( art, null, artFile, RepositoryPolicy.CHECKSUM_POLICY_FAIL );
            MetadataDownload metaDown =
                new MetadataDownload( meta, null, metaFile, RepositoryPolicy.CHECKSUM_POLICY_FAIL );
            Collection<ArtifactDownload> artDowns = Arrays.asList( artDown );
            Collection<MetadataDownload> metaDowns = Arrays.asList( metaDown );

            connector.get( artDowns, metaDowns );

            assertNull( "artifact download had exception: " + artDown.getException(), artDown.getException() );
            assertNull( "metadata download had exception: " + metaDown.getException(), metaDown.getException() );
            assertEquals( State.DONE, artDown.getState() );
            assertEquals( State.DONE, metaDown.getState() );

            FileUtil.deleteDir( new File( "target/connector-test-suite" ) );
        }

    }
}
