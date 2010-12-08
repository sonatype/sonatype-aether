package org.sonatype.aether.connector.async;

/*******************************************************************************
 * Copyright (c) 2010 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import org.junit.Test;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.metadata.Metadata;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.repository.RepositoryPolicy;
import org.sonatype.aether.spi.connector.ArtifactDownload;
import org.sonatype.aether.spi.connector.ArtifactUpload;
import org.sonatype.aether.spi.connector.MetadataDownload;
import org.sonatype.aether.spi.connector.MetadataUpload;
import org.sonatype.aether.spi.connector.RepositoryConnector;
import org.sonatype.aether.spi.connector.RepositoryConnectorFactory;
import org.sonatype.aether.spi.connector.Transfer;
import org.sonatype.aether.spi.log.NullLogger;
import org.sonatype.aether.test.impl.TestFileProcessor;
import org.sonatype.aether.test.util.TestFileUtils;
import org.sonatype.aether.test.util.connector.suite.ConnectorTestSetup.AbstractConnectorTestSetup;
import org.sonatype.aether.test.util.connector.suite.ConnectorTestSuite;
import org.sonatype.aether.test.util.impl.StubArtifact;
import org.sonatype.aether.test.util.impl.StubMetadata;
import org.sonatype.aether.transfer.NoRepositoryConnectorException;
import org.sonatype.tests.http.server.jetty.impl.JettyServerProvider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Benjamin Hanzelmann
 */
public class AetherDefaultTest
    extends ConnectorTestSuite
{

    private static class JettyConnectorTestSetup
        extends AbstractConnectorTestSetup
    {

        private JettyServerProvider provider;

        public RemoteRepository before( RepositorySystemSession session, Map<String, Object> context )
            throws Exception
        {
            provider = new JettyServerProvider();
            provider.initServer();
            provider.addBehaviour( "/*", new FileServer() );
            provider.start();
            return new RemoteRepository( "jetty-repo", "default", provider.getUrl().toString() + "/repo" );
        }

        public RepositoryConnectorFactory factory()
        {
            return new AsyncRepositoryConnectorFactory( NullLogger.INSTANCE, new TestFileProcessor() );
        }

        @Override
        public void after( RepositorySystemSession session, RemoteRepository repository, Map<String, Object> context )
            throws Exception
        {
            if ( provider != null )
            {
                provider.stop();
                provider = null;
            }
        }

    }

    /**
     * @param setup
     */
    public AetherDefaultTest()
    {
        super( new JettyConnectorTestSetup() );
    }

       @Test
    public void testMkdirConcurrencyBug()
        throws IOException, NoRepositoryConnectorException
    {
        RepositoryConnector connector = factory().newInstance( session, repository );
        File artifactFile = TestFileUtils.createTempFile("mkdirsBug0");
        File metadataFile = TestFileUtils.createTempFile( "mkdirsBug1" );

        int numTransfers = 2;

        ArtifactUpload[] artUps = new ArtifactUpload[numTransfers];
        MetadataUpload[] metaUps = new MetadataUpload[numTransfers];


        for ( int i = 0; i < numTransfers; i++ )
        {
            StubArtifact art = new StubArtifact( "testGroup", "testArtifact", "", "jar", i + "-test" );
            StubMetadata meta =
                new StubMetadata( "testGroup", "testArtifact", i + "-test", "maven-metadata.xml",
                                  Metadata.Nature.RELEASE_OR_SNAPSHOT );

            ArtifactUpload artUp = new ArtifactUpload( art, artifactFile );
            MetadataUpload metaUp = new MetadataUpload( meta, metadataFile );

            artUps[i] = artUp;
            metaUps[i] = metaUp;
        }

        connector.put( Arrays.asList(artUps), null );
        connector.put( null, Arrays.asList( metaUps ) );

        File localRepo = session.getLocalRepository().getBasedir();

        StringBuilder localPath = new StringBuilder( localRepo.getAbsolutePath() );

        for ( int i = 0; i < 50; i++ )
        {
            localPath.append( "/d" );
        }

        ArtifactDownload[] artDowns = new ArtifactDownload[numTransfers];
        MetadataDownload[] metaDowns = new MetadataDownload[numTransfers];

        for ( int m = 0; m < 30; m++ )
        {
            for ( int i = 0; i < numTransfers; i++ )
            {
                File artFile = new File( localPath.toString() + "/a" + i );
                File metaFile = new File( localPath.toString() + "/m" + i );

                StubArtifact art = new StubArtifact( "testGroup", "testArtifact", "", "jar", i + "-test" );
                StubMetadata meta =
                    new StubMetadata( "testGroup", "testArtifact", i + "-test", "maven-metadata.xml",
                                      Metadata.Nature.RELEASE_OR_SNAPSHOT );

                ArtifactDownload artDown =
                    new ArtifactDownload( art, null, artFile, RepositoryPolicy.CHECKSUM_POLICY_FAIL );
                MetadataDownload metaDown =
                    new MetadataDownload( meta, null, metaFile, RepositoryPolicy.CHECKSUM_POLICY_FAIL );

                artDowns[i] = artDown;
                metaDowns[i] = metaDown;
            }

            connector.get( Arrays.asList( artDowns ), Arrays.asList( metaDowns ) );

            for ( int j = 0; j < numTransfers; j++ )
            {
                ArtifactDownload artDown = artDowns[j];
                MetadataDownload metaDown = metaDowns[j];

                assertNull( "artifact download had exception: " + artDown.getException(), artDown.getException() );
                assertNull( "metadata download had exception: " + metaDown.getException(), metaDown.getException() );
                assertEquals( Transfer.State.DONE, artDown.getState() );
                assertEquals( Transfer.State.DONE, metaDown.getState() );
            }

            TestFileUtils.delete( localRepo );
        }

        connector.close();
    }

}
