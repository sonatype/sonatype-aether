package org.apache.maven.repository.wagon;

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

import org.apache.maven.repository.NoRepositoryReaderException;
import org.apache.maven.repository.RemoteRepository;
import org.apache.maven.repository.RepositorySession;
import org.apache.maven.repository.spi.RepositoryReader;
import org.apache.maven.repository.spi.RepositoryReaderFactory;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

/**
 * @author Benjamin Bentmann
 */
@Component( role = RepositoryReaderFactory.class, hint = "wagon" )
public class WagonRepositoryReaderFactory
    implements RepositoryReaderFactory
{

    @Requirement
    private PlexusContainer container;

    public int getPriority()
    {
        return 0;
    }

    public RepositoryReader newInstance( RepositorySession session, RemoteRepository repository )
        throws NoRepositoryReaderException
    {
        return new WagonRepositoryReader( container, repository, session );
    }

}
