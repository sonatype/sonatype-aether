package org.sonatype.aether.test.util.connector;

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

import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.test.impl.RecordingTransferListener;

public class ConnectorTestContext
{

    private RemoteRepository repository;

    private RepositorySystemSession session;

    public ConnectorTestContext( RemoteRepository repository, RepositorySystemSession session )
    {
        super();
        this.repository = repository;
        this.session = session;
    }

    public ConnectorTestContext()
    {
        super();
    }

    public RemoteRepository getRepository()
    {
        return repository;
    }

    public RepositorySystemSession getSession()
    {
        return session;
    }

    public void setRepository( RemoteRepository repository )
    {
        this.repository = repository;
    }

    public void setSession( RepositorySystemSession session )
    {
        this.session = session;
    }

    public RecordingTransferListener getRecordingTransferListener()
    {
        if ( session.getTransferListener() instanceof RecordingTransferListener )
        {
            return (RecordingTransferListener) session.getTransferListener();
        }
        else
        {
            return new RecordingTransferListener( session.getTransferListener() );
        }
    }

}