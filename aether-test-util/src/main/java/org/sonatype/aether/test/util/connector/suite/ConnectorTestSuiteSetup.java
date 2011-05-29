package org.sonatype.aether.test.util.connector.suite;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import java.util.Map;

import org.junit.After;
import org.junit.Before;
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

    private ConnectorTestSetup connectorSetup = null;

    protected RemoteRepository repository;

    protected TestRepositorySystemSession session;

    private Map<String, Object> context = null;

    private RepositoryConnectorFactory factory;

    /**
     * @param setup The connector-specific callback handler to use.
     */
    public ConnectorTestSuiteSetup( ConnectorTestSetup setup )
    {
        connectorSetup = setup;
        factory = setup.factory();
        try
        {
            context = connectorSetup.beforeClass( session );
        }
        catch ( Exception e )
        {
            throw new IllegalStateException( "Error while running ConnectorTestSetup#beforeClass.", e );
        }
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
        session = new TestRepositorySystemSession();
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
     * @return the factory as determined by {@link ConnectorTestSetup#factory()}.
     */
    protected RepositoryConnectorFactory factory()
    {
        return factory;
    }

}
