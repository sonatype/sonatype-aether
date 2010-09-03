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

import java.util.Map;

import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.spi.connector.RepositoryConnectorFactory;
import org.sonatype.aether.test.util.connector.suite.ConnectorTestSetup.AbstractConnectorTestSetup;

public interface ConnectorTestSetup
{
    public RemoteRepository before( RepositorySystemSession session, Map<String, Object> context );

    public Map<String, Object> beforeClass( RepositorySystemSession session );

    public void after( RepositorySystemSession session, RemoteRepository repository, Map<String, Object> context );

    public void afterClass( RepositorySystemSession session, Map<String, Object> context );

    public RepositoryConnectorFactory factory();

    public static abstract class AbstractConnectorTestSetup
        implements ConnectorTestSetup
    {

        public Map<String, Object> beforeClass( RepositorySystemSession session )
        {
            return null;
        }

        public void after( RepositorySystemSession session, RemoteRepository repository, Map<String, Object> context )
        {
        }

        public void afterClass( RepositorySystemSession session, Map<String, Object> context )
        {
        }

    }
}