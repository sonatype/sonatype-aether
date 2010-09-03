package org.sonatype.aether.impl.internal;

import java.util.List;

import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.impl.RemoteRepositoryManager;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.repository.RepositoryPolicy;
import org.sonatype.aether.spi.connector.RepositoryConnector;
import org.sonatype.aether.transfer.NoRepositoryConnectorException;

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

class StubRemoteRepositoryManager
    implements RemoteRepositoryManager
{

    private RepositoryConnector connector;

    public void setConnector( RepositoryConnector connector )
    {
        this.connector = connector;
    }

    public List<RemoteRepository> aggregateRepositories( RepositorySystemSession session,
                                                         List<RemoteRepository> dominantRepositories,
                                                         List<RemoteRepository> recessiveRepositories,
                                                         boolean recessiveIsRaw )
    {
        throw new UnsupportedOperationException( "aggregateRepositories" );
    }

    public RepositoryPolicy getPolicy( RepositorySystemSession session, RemoteRepository repository,
                                       boolean releases, boolean snapshots )
    {
        return new RepositoryPolicy( true, RepositoryPolicy.UPDATE_POLICY_ALWAYS,
                                     RepositoryPolicy.CHECKSUM_POLICY_FAIL );
    }

    public RepositoryConnector getRepositoryConnector( RepositorySystemSession session, RemoteRepository repository )
        throws NoRepositoryConnectorException
    {
        return connector;
    }

}