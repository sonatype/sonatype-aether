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

import java.util.Map;

import org.apache.maven.repository.RemoteRepository;
import org.apache.maven.repository.RepositoryContext;
import org.apache.maven.repository.RepositoryReader;
import org.apache.maven.repository.NoRepositoryReaderException;

/**
 * @author Benjamin Bentmann
 * @plexus.component role="org.apache.maven.repository.RepositorySystem" role-hint="default"
 */
public class DefaultRepositorySystem
{

    /**
     * @plexus.requirement role="org.apache.maven.repository.internal.RepositoryReaderFactory"
     */
    private Map<String, RepositoryReaderFactory> readerFactories;

    private RepositoryReader getRepositoryReader( RemoteRepository remoteRepository, RepositoryContext context )
        throws NoRepositoryReaderException
    {
        RepositoryReaderFactory factory = readerFactories.get( remoteRepository.getType() );

        if ( factory == null )
        {
            throw new NoRepositoryReaderException( remoteRepository );
        }

        return factory.newInstance( remoteRepository, context );
    }

}
