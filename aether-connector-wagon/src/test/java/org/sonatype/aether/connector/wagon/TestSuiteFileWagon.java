package org.sonatype.aether.connector.wagon;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *******************************************************************************/

import java.io.File;
import java.io.IOException;
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
import org.sonatype.aether.test.impl.TestFileProcessor;
import org.sonatype.aether.test.util.TestFileUtils;
import org.sonatype.aether.test.util.connector.suite.ConnectorTestSetup.AbstractConnectorTestSetup;
import org.sonatype.aether.test.util.connector.suite.ConnectorTestSuite;
import org.sonatype.aether.transfer.NoRepositoryConnectorException;

/**
 * @author Benjamin Hanzelmann
 *
 */
public class TestSuiteFileWagon
    extends ConnectorTestSuite
{

    /**
     * @author Benjamin Hanzelmann
     *
     */
    private static final class FileWagonConnectorTestSetup
        extends AbstractConnectorTestSetup
    {
        private File basedir;

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
                    }, null, repository, session, TestFileProcessor.INSTANCE, NullLogger.INSTANCE );
                }

                public int getPriority()
                {
                    return 1;
                }
            };
        }

        public RemoteRepository before( RepositorySystemSession session, Map<String, Object> context )
            throws IOException
        {
            try
            {
                basedir = TestFileUtils.createTempDir( getClass().getSimpleName() );
                return new RemoteRepository( "test-filewagon", "default",
                                             basedir.toURI().toURL().toString() );
            }
            catch ( MalformedURLException e )
            {
                throw new RuntimeException( e.getMessage(), e );
            }
        }

        @Override
        public void after( RepositorySystemSession session, RemoteRepository repository, Map<String, Object> context )
            throws Exception

        {
            TestFileUtils.delete( basedir );
        }
    }

    public TestSuiteFileWagon()
    {
        super( new FileWagonConnectorTestSetup() );
    }

}
