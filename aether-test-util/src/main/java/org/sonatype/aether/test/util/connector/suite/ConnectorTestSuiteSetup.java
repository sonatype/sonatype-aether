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

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.spi.connector.RepositoryConnectorFactory;
import org.sonatype.aether.test.impl.TestRepositorySystemSession;

/**
 * Provides the Junit-callback methods to configure the {@link ConnectorTestSuite} per connector.
 * 
 * @author Benjamin Hanzelmann
 */
public abstract class ConnectorTestSuiteSetup
{

    private static ConnectorTestSetup connectorSetup;

    protected static RemoteRepository repository;

    protected static TestRepositorySystemSession session;

    private static Map<String, Object> context;

    private static RepositoryConnectorFactory factory;

    private boolean doClassInit = true;

    /**
     * @param setup The connector-specific callback handler to use.
     */
    public ConnectorTestSuiteSetup( ConnectorTestSetup setup )
    {
        super();
        connectorSetup = setup;
        factory = setup.factory();
    }

    /**
     * Creates a new {@link TestRepositorySystemSession} to use in the tests.
     */
    @BeforeClass
    public static void beforeClass()
    {
        session = new TestRepositorySystemSession();
    }

    /**
     * If called for the first time, calls
     * {@link ConnectorTestSetup#beforeClass(org.sonatype.aether.RepositorySystemSession)}. Always calls
     * {@link ConnectorTestSetup#before(org.sonatype.aether.RepositorySystemSession, Map)}.
     */
    @Before
    public void before()
    {
        if ( doClassInit )
        {
            context = connectorSetup.beforeClass( session );
            doClassInit = false;
        }
        repository = connectorSetup.before( session, context );
    }

    /**
     * Calls {@link ConnectorTestSetup#after(org.sonatype.aether.RepositorySystemSession, RemoteRepository, Map)}.
     */
    @After
    public void after()
    {
        connectorSetup.after( session, repository, context );
    }


    /**
     * Calls {@link ConnectorTestSetup#afterClass(org.sonatype.aether.RepositorySystemSession, RemoteRepository, Map)}.
     */
    @AfterClass
    public static void afterClass()
    {
        connectorSetup.afterClass( session, context );
    }

    /**
     * @return the factory as determined by {@link ConnectorTestSetup#factory()}.
     */
    protected static RepositoryConnectorFactory factory()
    {
        return factory;
    }

}
