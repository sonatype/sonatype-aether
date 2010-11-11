package org.sonatype.aether.connector.async;

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

import java.util.HashMap;
import java.util.Map;

import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.spi.connector.RepositoryConnectorFactory;
import org.sonatype.aether.spi.log.NullLogger;
import org.sonatype.aether.test.impl.TestFileProcessor;
import org.sonatype.aether.test.util.connector.suite.ConnectorTestSetup.AbstractConnectorTestSetup;
import org.sonatype.aether.test.util.connector.suite.ConnectorTestSuite;
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
            provider.addBehaviour( "/*", new FileServer() );
            provider.start();
            return new RemoteRepository( "jetty-repo", "default", provider.getUrl().toString() + "/repo" );
        }

        public RepositoryConnectorFactory factory()
        {
            return new AsyncRepositoryConnectorFactory( NullLogger.INSTANCE, new TestFileProcessor() );
        }

        @Override
        public Map<String, Object> beforeClass( RepositorySystemSession session )
            throws Exception
        {
            return new HashMap<String, Object>();
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

        @Override
        public void afterClass( RepositorySystemSession session, Map<String, Object> context )
            throws Exception
        {
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
