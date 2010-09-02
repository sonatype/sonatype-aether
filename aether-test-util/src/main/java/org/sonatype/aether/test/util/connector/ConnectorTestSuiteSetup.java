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
package org.sonatype.aether.test.util.connector;

import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.spi.connector.RepositoryConnectorFactory;
import org.sonatype.aether.test.impl.TestRepositorySystemSession;

/**
 * @author Benjamin Hanzelmann
 */
public class ConnectorTestSuiteSetup
{

    private static ConnectorTestSetup connectorSetup;

    protected static RemoteRepository repository;

    protected static TestRepositorySystemSession session;

    private static Map<String, Object> context;

    protected static RepositoryConnectorFactory factory;

    private boolean doClassInit = true;

    public ConnectorTestSuiteSetup( ConnectorTestSetup setup )
    {
        super();
        connectorSetup = setup;
        factory = setup.factory();
    }

    public static interface ConnectorTestSetup
    {
        public RemoteRepository before( RepositorySystemSession session, Map<String, Object> context );

        public Map<String, Object> beforeClass( RepositorySystemSession session );

        public void after( RepositorySystemSession session, RemoteRepository repository, Map<String, Object> context );

        public void afterClass( RepositorySystemSession session, RemoteRepository repository,
                                Map<String, Object> context );

        public RepositoryConnectorFactory factory();
    }

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

        public void afterClass( RepositorySystemSession session, RemoteRepository repository,
                                Map<String, Object> context )
        {
        }

    }

    @BeforeClass
    public static void beforeClass()
    {
        session = new TestRepositorySystemSession();
    }

    @Before
    public void before()
    {
        if ( doClassInit )
        {
            context = connectorSetup.beforeClass( session );
            doClassInit = false;
        }
        this.repository = connectorSetup.before( session, context );
    }

    @After
    public void after()
    {
        connectorSetup.after( session, repository, context );
    }

    @AfterClass
    public static void afterClass()
    {
        connectorSetup.afterClass( session, repository, context );
    }

    protected static RepositoryConnectorFactory factory()
    {
        return factory;
    }

}