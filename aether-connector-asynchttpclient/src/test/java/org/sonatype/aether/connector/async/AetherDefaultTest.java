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
            TestFileUtils.deleteTempFiles();
        }

    }

    /**
     * @param setup
     */
    public AetherDefaultTest()
    {
        super( new JettyConnectorTestSetup() );
    }

}
