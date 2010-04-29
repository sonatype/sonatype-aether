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
import org.apache.maven.repository.RepositoryContext;
import org.apache.maven.repository.RepositoryReader;
import org.apache.maven.repository.RepositoryReaderFactory;
import org.codehaus.plexus.PlexusContainer;

/**
 * @author Benjamin Bentmann
 * @plexus.component role="org.apache.maven.repository.internal.RepositoryReaderFactory" role-hint="default"
 */
public class WagonRepositoryReaderFactory
    implements RepositoryReaderFactory
{

    /**
     * @plexus.requirement
     */
    private PlexusContainer container;

    public int getPriority()
    {
        return 0;
    }

    public RepositoryReader newInstance( RepositoryContext context, RemoteRepository repository )
        throws NoRepositoryReaderException
    {
        return new WagonRepositoryReader( container, repository, context );
    }

}
