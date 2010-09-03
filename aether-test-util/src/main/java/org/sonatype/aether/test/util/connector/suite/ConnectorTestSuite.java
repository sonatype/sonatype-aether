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

import org.junit.Test;
import org.sonatype.aether.metadata.Metadata;
import org.sonatype.aether.spi.connector.ArtifactDownload;
import org.sonatype.aether.spi.connector.ArtifactUpload;
import org.sonatype.aether.spi.connector.MetadataDownload;
import org.sonatype.aether.spi.connector.MetadataUpload;
import org.sonatype.aether.spi.connector.RepositoryConnector;
import org.sonatype.aether.spi.connector.Transfer;
import org.sonatype.aether.spi.connector.Transfer.State;
import org.sonatype.aether.test.util.FileUtil;
import org.sonatype.aether.test.util.connector.TestConnectorPathUtil;
import org.sonatype.aether.test.util.impl.StubArtifact;
import org.sonatype.aether.test.util.impl.StubMetadata;
import org.sonatype.aether.transfer.NoRepositoryConnectorException;

/**
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
    public void testBlocking()
        throws NoRepositoryConnectorException, IOException
    {
    
        RepositoryConnector connector = factory().newInstance( session, repository );
    
        int count = 10;
    
        byte[] pattern = "tmpFile".getBytes();
        File tmpFile = FileUtil.createTempFile( pattern, 100000 );
    
        ArtifactUpload[] artUps = new ArtifactUpload[count];
        MetadataUpload[] metaUps = new MetadataUpload[count];
        ArtifactDownload[] artDowns = new ArtifactDownload[count];
        MetadataDownload[] metaDowns = new MetadataDownload[count];
    
        for ( int i = 0; i < count; i++ )
        {
            StubArtifact artifact = new StubArtifact( "testGroup", "testArtifact", "jar", "", i + "-test" );
            StubMetadata metadata =
                new StubMetadata( "testGroup", "testArtifact", i + "-test", "maven-metadata.xml",
                                  Metadata.Nature.RELEASE_OR_SNAPSHOT );
    
            ArtifactUpload artUp = new ArtifactUpload( artifact, tmpFile );
            MetadataUpload metaUp = new MetadataUpload( metadata, tmpFile );
            ArtifactDownload artDown = new ArtifactDownload( artifact, null, FileUtil.createTempFile( "" ), null );
            MetadataDownload metaDown = new MetadataDownload( metadata, null, FileUtil.createTempFile( "" ), null );
    
            artUps[i] = artUp;
            metaUps[i] = metaUp;
            artDowns[i] = artDown;
            metaDowns[i] = metaDown;
        }
    
        // this should block until all transfers are done - racing condition, better way to test this?
        connector.put( Arrays.asList( artUps ), Arrays.asList( metaUps ) );
    
        // this should block until all transfers are done - racing condition, better way to test this?
        connector.get( Arrays.asList( artDowns ), Arrays.asList( metaDowns ) );
    
        for ( int i = 0; i < count; i++ )
        {
            ArtifactUpload artUp = artUps[i];
            MetadataUpload metaUp = metaUps[i];
            ArtifactDownload artDown = artDowns[i];
            MetadataDownload metaDown = metaDowns[i];
    
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
        ArtifactUpload artUp =
            new ArtifactUpload( new StubArtifact( "testGroup", "testArtifact", "jar", "", 1 + "-test" ), tmpFile );
        MetadataUpload metaUp =
            new MetadataUpload( new StubMetadata( "testGroup", "testArtifact", 1 + "-test", "maven-metadata.xml",
                                                     Metadata.Nature.RELEASE_OR_SNAPSHOT ), tmpFile );
    
        for ( int i = 0; i < 100; i++ )
        {
            Collection<ArtifactUpload> artUps = Arrays.asList( artUp );
            Collection<MetadataUpload> metaUps = Arrays.asList( metaUp );
    
            connector.put( artUps, metaUps );
    
            assertNull( artUp.getException() );
            assertNull( metaUp.getException() );
            assertEquals( State.DONE, artUp.getState() );
            assertEquals( State.DONE, metaUp.getState() );
    
            FileUtil.deleteDir( new File( TestConnectorPathUtil.basedir( repository.getUrl() ) ) );
        }
    
    }
}
