package org.sonatype.aether.connector.file;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;

import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.spi.connector.RepositoryConnectorFactory;
import org.sonatype.aether.test.impl.TestFileProcessor;
import org.sonatype.aether.test.util.TestFileUtils;
import org.sonatype.aether.test.util.connector.suite.ConnectorTestSetup.AbstractConnectorTestSetup;
import org.sonatype.aether.test.util.connector.suite.ConnectorTestSuite;

/**
 * @author Benjamin Hanzelmann
 */
public class TestSuite
    extends ConnectorTestSuite
{

    /**
     * @author Benjamin Hanzelmann
     */
    private static final class FileConnectorTestSetup
        extends AbstractConnectorTestSetup
    {
        private File repoFile;

        public RepositoryConnectorFactory factory()
        {
            return new FileRepositoryConnectorFactory().setFileProcessor( TestFileProcessor.INSTANCE );
        }

        @Override
        public void after( RepositorySystemSession session, RemoteRepository repository, Map<String, Object> context )
            throws Exception
        {
            TestFileUtils.delete( repoFile );
        }

        public RemoteRepository before( RepositorySystemSession session, Map<String, Object> context )
            throws IOException
        {
            RemoteRepository repo = null;
            repoFile = TestFileUtils.createTempDir( "test-repo" );
            try
            {
                repo =
                    new RemoteRepository( "test-file", "default",
                                          repoFile.toURI().toURL().toString() );
            }
            catch ( MalformedURLException e )
            {
                throw new UnsupportedOperationException( "File.toURI().toURL() failed" );
            }
            return repo;
        }
    }

    public TestSuite()
    {
        super( new FileConnectorTestSetup() );
    }

}
