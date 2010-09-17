package org.sonatype.aether.test.util.connector.suite;

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

import java.util.Map;

import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.spi.connector.RepositoryConnectorFactory;

/**
 * The callback handler used to configure {@link ConnectorTestSuite}.
 * <p>
 * The specified methods have the same meaning as the corresponding JUnit4-annotations.
 * 
 * @see org.sonatype.aether.test.util.connector.suite.ConnectorTestSetup.AbstractConnectorTestSetup
 * @author Benjamin Hanzelmann
 */
public interface ConnectorTestSetup
{
    /**
     * This method is called before each test run.
     * 
     * @param session The session used for the test bundle.
     * @param context The context provided by {@link #beforeClass(RepositorySystemSession)}.
     * @return The repository to use in the next test.
     */
    public RemoteRepository before( RepositorySystemSession session, Map<String, Object> context );

    /**
     * This method is called before the first test of the bundle is executed.
     * 
     * @param session The session used for the test bundle.
     * @return A map acting as a context for the current test bundle run. This context will not be used by the test
     *         cases in the suite, and is only used to provide context for the before/after/afterClass-method calls.
     *         This might be used to save setup-specific values (port numbers, directories to clean up after tests, ...)
     */
    public Map<String, Object> beforeClass( RepositorySystemSession session );

    /**
     * This method is called after each test of the bundle is executed. Repositories should be cleaned after each test,
     * as previous uploads may influence test results.
     * 
     * @param session The session used for the test bundle.
     * @param context The context provided by {@link #beforeClass(RepositorySystemSession)}.
     * @param repository the repository used in the test run.
     */
    public void after( RepositorySystemSession session, RemoteRepository repository, Map<String, Object> context );

    /**
     * This method is called after all tests of the bundle were run.
     * 
     * @param session The session used for the test bundle.
     * @param context The context provided by {@link #beforeClass(RepositorySystemSession)}.
     */
    public void afterClass( RepositorySystemSession session, Map<String, Object> context );

    /**
     * @return the factory to use for the tests.
     */
    public RepositoryConnectorFactory factory();

    /**
     * Empty implementation of {@link ConnectorTestSetup}.
     * 
     * @author Benjamin Hanzelmann
     */
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