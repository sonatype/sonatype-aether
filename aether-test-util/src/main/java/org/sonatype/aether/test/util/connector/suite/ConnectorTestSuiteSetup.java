package org.sonatype.aether.test.util.connector.suite;

/*******************************************************************************
 * Copyright (c) 2010 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

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
        throws Exception
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
        throws Exception
    {
        connectorSetup.after( session, repository, context );
    }

    /**
     * Calls {@link ConnectorTestSetup#afterClass(org.sonatype.aether.RepositorySystemSession, RemoteRepository, Map)}.
     */
    @AfterClass
    public static void afterClass()
        throws Exception
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
