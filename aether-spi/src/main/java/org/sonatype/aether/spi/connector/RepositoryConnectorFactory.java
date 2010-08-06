package org.sonatype.aether.spi.connector;

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

import org.sonatype.aether.NoRepositoryConnectorException;
import org.sonatype.aether.RemoteRepository;
import org.sonatype.aether.RepositorySystemSession;

/**
 * A factory to create repository connectors. A repository connector is responsible for uploads/downloads to/from a
 * certain kind of remote repository. When the repository system needs a repository connector for a given remote
 * repository, it iterates the registered factories in descending order of their priority and calls
 * {@link #newInstance(RepositorySystemSession, RemoteRepository)} on them. The first connector returned by a factory
 * will then be used for the transfer.
 * 
 * @author Benjamin Bentmann
 */
public interface RepositoryConnectorFactory
{

    /**
     * Tries to create a repository connector for the specified remote repository. Typically, a factory will inspect
     * {@link RemoteRepository#getProtocol()} and {@link RemoteRepository#getContentType()} to determine whether it can
     * handle a repository.
     * 
     * @param session The repository system session from which to configure the connector, must not be {@code null}. In
     *            particular, a connector must notify any {@link RepositorySystemSession#getTransferListener()} set for
     *            the session and should obey the timeouts configured for the session.
     * @param repository The remote repository to create a connector for, must not be {@code null}.
     * @return The connector for the given repository, never {@code null}.
     * @throws NoRepositoryConnectorException If the factory cannot create a connector for the specified remote
     *             repository.
     */
    RepositoryConnector newInstance( RepositorySystemSession session, RemoteRepository repository )
        throws NoRepositoryConnectorException;

    /**
     * The priority of this factory. Factories with higher priority are preferred over those with lower priority.
     * 
     * @return The priority of this factory.
     */
    int getPriority();

}
