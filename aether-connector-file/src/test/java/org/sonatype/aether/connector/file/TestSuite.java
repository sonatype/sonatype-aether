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
package org.sonatype.aether.connector.file;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Map;

import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.spi.connector.RepositoryConnectorFactory;
import org.sonatype.aether.test.util.FileUtil;
import org.sonatype.aether.test.util.connector.suite.ConnectorTestSuite;

/**
 * @author Benjamin Hanzelmann
 */
public class TestSuite
    extends ConnectorTestSuite
{

    static ConnectorTestSetup setup = new AbstractConnectorTestSetup()
    {
        private File repoFile = new File( "target/test-repo" );

        public RepositoryConnectorFactory factory()
        {
            return new FileRepositoryConnectorFactory();
        }

        public void after( RepositorySystemSession session, RemoteRepository repository, Map<String, Object> context )
        {
            FileUtil.deleteDir( repoFile );
        }

        public RemoteRepository before( RepositorySystemSession session, Map<String, Object> context )
        {
            RemoteRepository repo = null;
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

    };

    public TestSuite()
    {
        super( setup );
    }

}
