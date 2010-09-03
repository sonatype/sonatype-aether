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
package org.sonatype.aether.connector.wagon;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Map;

import org.apache.maven.wagon.ConnectionException;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.providers.file.FileWagon;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.spi.connector.RepositoryConnector;
import org.sonatype.aether.spi.connector.RepositoryConnectorFactory;
import org.sonatype.aether.spi.log.NullLogger;
import org.sonatype.aether.test.util.connector.suite.ConnectorTestSuite;
import org.sonatype.aether.transfer.NoRepositoryConnectorException;

/**
 * @author Benjamin Hanzelmann
 *
 */
public class FileWagonTestSuite
    extends ConnectorTestSuite
{

    private static AbstractConnectorTestSetup setup = new AbstractConnectorTestSetup()
    {

        public RepositoryConnectorFactory factory()
        {
            return new RepositoryConnectorFactory()
            {

                public RepositoryConnector newInstance( RepositorySystemSession session, RemoteRepository repository )
                    throws NoRepositoryConnectorException
                {

                    return new WagonRepositoryConnector( new WagonProvider()
                    {

                        public void release( Wagon wagon )
                        {
                            try
                            {
                                wagon.disconnect();
                            }
                            catch ( ConnectionException e )
                            {
                                throw new RuntimeException( e.getMessage(), e );
                            }
                        }

                        public Wagon lookup( String roleHint )
                            throws Exception
                        {
                            return new FileWagon();
                        }
                    }, repository, session, NullLogger.INSTANCE );
                }

                public int getPriority()
                {
                    return 1;
                }
            };
        }

        public RemoteRepository before( RepositorySystemSession session, Map<String, Object> context )
        {
            try
            {
                return new RemoteRepository( "test-filewagon", "default",
                                             new File( "target/test-filewagon" ).toURI().toURL().toString() );
            }
            catch ( MalformedURLException e )
            {
                throw new RuntimeException( e.getMessage(), e );
            }
        }
    };

    public FileWagonTestSuite()
    {
        super( setup );
    }

}
