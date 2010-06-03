package org.sonatype.maven.repository.connector.wagon;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.maven.repository.NoRepositoryConnectorException;
import org.sonatype.maven.repository.RemoteRepository;
import org.sonatype.maven.repository.RepositorySession;
import org.sonatype.maven.repository.spi.Logger;
import org.sonatype.maven.repository.spi.NullLogger;
import org.sonatype.maven.repository.spi.RepositoryConnector;
import org.sonatype.maven.repository.spi.RepositoryConnectorFactory;

/**
 * @author Benjamin Bentmann
 */
@Component( role = RepositoryConnectorFactory.class, hint = "wagon" )
public class WagonRepositoryConnectorFactory
    implements RepositoryConnectorFactory
{

    @Requirement
    private Logger logger = NullLogger.INSTANCE;

    @Requirement
    private WagonProvider wagonProvider;

    private int priority;

    public WagonRepositoryConnectorFactory setLogger( Logger logger )
    {
        this.logger = ( logger != null ) ? logger : NullLogger.INSTANCE;
        return this;
    }

    public WagonRepositoryConnectorFactory setWagonProvider( WagonProvider wagonProvider )
    {
        this.wagonProvider = wagonProvider;
        return this;
    }

    public int getPriority()
    {
        return priority;
    }

    public WagonRepositoryConnectorFactory setPriority( int priority )
    {
        this.priority = priority;
        return this;
    }

    public RepositoryConnector newInstance( RepositorySession session, RemoteRepository repository )
        throws NoRepositoryConnectorException
    {
        return new WagonRepositoryConnector( wagonProvider, repository, session, logger );
    }

}
