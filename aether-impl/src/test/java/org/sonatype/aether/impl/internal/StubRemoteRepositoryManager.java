package org.sonatype.aether.impl.internal;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import java.util.List;

import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.impl.RemoteRepositoryManager;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.repository.RepositoryPolicy;
import org.sonatype.aether.spi.connector.RepositoryConnector;
import org.sonatype.aether.transfer.NoRepositoryConnectorException;
import org.sonatype.aether.util.StringUtils;

class StubRemoteRepositoryManager
    implements RemoteRepositoryManager
{

    public StubRemoteRepositoryManager( RepositoryConnector connector )
    {
        setConnector( connector );
    }

    public StubRemoteRepositoryManager()
    {
    }

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
        return dominantRepositories;
    }

    public RepositoryPolicy getPolicy( RepositorySystemSession session, RemoteRepository repository, boolean releases,
                                       boolean snapshots )
    {
        RepositoryPolicy policy = repository.getPolicy( snapshots );

        if ( !StringUtils.isEmpty( session.getChecksumPolicy() ) )
        {
            policy = policy.setChecksumPolicy( session.getChecksumPolicy() );
        }
        if ( !StringUtils.isEmpty( session.getUpdatePolicy() ) )
        {
            policy = policy.setUpdatePolicy( session.getUpdatePolicy() );
        }

        return policy;
    }

    public RepositoryConnector getRepositoryConnector( RepositorySystemSession session, RemoteRepository repository )
        throws NoRepositoryConnectorException
    {
        return connector;
    }

}
