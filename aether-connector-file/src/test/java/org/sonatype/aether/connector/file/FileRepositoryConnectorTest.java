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

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.sonatype.aether.DefaultArtifact;
import org.sonatype.aether.DefaultMetadata;
import org.sonatype.aether.Metadata;
import org.sonatype.aether.NoRepositoryConnectorException;
import org.sonatype.aether.spi.connector.ArtifactDownload;
import org.sonatype.aether.spi.connector.ArtifactUpload;
import org.sonatype.aether.spi.connector.MetadataDownload;
import org.sonatype.aether.spi.connector.MetadataUpload;
import org.sonatype.aether.spi.connector.RepositoryConnectorFactory;
import org.sonatype.aether.spi.connector.Transfer;
import org.sonatype.aether.test.util.FileUtil;
import org.sonatype.aether.test.util.connector.TransferEventTester;
import org.sonatype.aether.test.util.connector.TransferEventTester.TestContext;

public class FileRepositoryConnectorTest
{

    @Test
    public void testBlocking()
        throws NoRepositoryConnectorException, IOException
    {
        TestContext ctx = TransferEventTester.setupTestContext();
        FileRepositoryConnector connector = new FileRepositoryConnector( ctx.getSession(), ctx.getRepository() );

        int count = 1000;

        byte[] pattern = "tmpFile".getBytes();
        File tmpFile = FileUtil.createTempFile( pattern, 10000 );

        ArtifactUpload[] artUps = new ArtifactUpload[count];
        MetadataUpload[] metaUps = new MetadataUpload[count];

        for ( int i = 0; i < count; i++ )
        {
            ArtifactUpload artUp =
                new ArtifactUpload( new DefaultArtifact( "testGroup", "testArtifact", "jar", i + "-test" ), tmpFile );
            MetadataUpload metaUp =
                new MetadataUpload( new DefaultMetadata( "testGroup", "testArtifact", i + "-test", "jar",
                                                         Metadata.Nature.RELEASE_OR_SNAPSHOT ), tmpFile );

            artUps[i] = artUp;
            metaUps[i] = metaUp;
        }

        // this should block until all transfers are done - racing condition, better way to test this?
        connector.put( Arrays.asList( artUps ), Arrays.asList( metaUps ) );

        verifyDone( artUps, metaUps );

        ArtifactDownload[] artDowns = new ArtifactDownload[count];
        MetadataDownload[] metaDowns = new MetadataDownload[count];
        for ( int i = 0; i < count; i++ )
        {
            ArtifactDownload artDown =
                new ArtifactDownload( new DefaultArtifact( "testGroup", "testArtifact", "jar", "1-test" ), null, null,
                                      null );
            MetadataDownload metaDown = new MetadataDownload();
            metaDown.setMetadata( new DefaultMetadata( "default", Metadata.Nature.RELEASE_OR_SNAPSHOT ) );

            artDowns[i] = artDown;
            metaDowns[i] = metaDown;
        }

        // this should block until all transfers are done - racing condition, better way to test this?
        connector.get( Arrays.asList( artDowns ), Arrays.asList( metaDowns ) );
    }

    private void verifyDone( Transfer[] artUps, Transfer[] metaUps )
    {
        for ( int i = 0; i < artUps.length; i++ )
        {
            Transfer artUp = artUps[i];
            assertTrue( Transfer.State.DONE.equals( artUp.getState() ) );
        }

        for ( int i = 0; i < metaUps.length; i++ )
        {
            Transfer metaUp = metaUps[i];
            assertTrue( Transfer.State.DONE.equals( metaUp.getState() ) );
        }
    }

    @Test
    public void testSuccededTransferEvents()
        throws IOException, NoRepositoryConnectorException
    {
        RepositoryConnectorFactory factory = new FileRepositoryConnectorFactory();

        TransferEventTester.testTransferEvents( factory );
    }

}
