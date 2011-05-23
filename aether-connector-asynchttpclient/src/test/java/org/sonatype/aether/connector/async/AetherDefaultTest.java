package org.sonatype.aether.connector.async;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import java.util.Map;

import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.spi.connector.RepositoryConnectorFactory;
import org.sonatype.aether.spi.log.NullLogger;
import org.sonatype.aether.test.impl.TestFileProcessor;
import org.sonatype.aether.test.util.connector.suite.ConnectorTestSetup.AbstractConnectorTestSetup;
import org.sonatype.aether.test.util.connector.suite.ConnectorTestSuite;
import org.sonatype.tests.http.server.jetty.behaviour.ResourceServer;
import org.sonatype.tests.http.server.jetty.impl.JettyServerProvider;

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
            provider.addBehaviour( "/*", new ResourceServer() );
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

}
