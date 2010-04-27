package org.apache.maven.repository.internal;

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

import java.util.Collection;
import java.util.List;

import org.apache.maven.repository.ArtifactRequest;
import org.apache.maven.repository.RemoteRepository;
import org.apache.maven.repository.RepositoryContext;
import org.apache.maven.repository.RepositoryReader;
import org.apache.maven.repository.NoRepositoryReaderException;
import org.apache.maven.repository.RepositoryReaderFactory;

/**
 * @author Benjamin Bentmann
 * @plexus.component role="org.apache.maven.repository.RepositorySystem" role-hint="default"
 */
public class DefaultRepositorySystem
{

    private void getArtifacts( Collection<? extends ArtifactRequest> requests, List<? extends RemoteRepository> repos,
                               RemoteRepository context )
    {
        for ( RemoteRepository repo : repos )
        {

        }
    }

    private RepositoryReader getRepositoryReader( RemoteRepository repository, RepositoryContext context )
        throws NoRepositoryReaderException
    {
        List<? extends RepositoryReaderFactory> factories =
            context.getRepositoryProviderRegistry().getReaderFactories();

        for ( RepositoryReaderFactory factory : factories )
        {
            try
            {
                return factory.newInstance( repository, context );
            }
            catch ( NoRepositoryReaderException e )
            {
                // continue and try next factory
            }
        }

        throw new NoRepositoryReaderException( repository );
    }

}
