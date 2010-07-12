package org.sonatype.maven.repository.spi;

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

import java.util.List;

import org.sonatype.maven.repository.NoRepositoryConnectorException;
import org.sonatype.maven.repository.RemoteRepository;
import org.sonatype.maven.repository.RepositoryPolicy;
import org.sonatype.maven.repository.RepositorySystemSession;

/**
 * @author Benjamin Bentmann
 */
public interface RemoteRepositoryManager
{

    List<RemoteRepository> aggregateRepositories( RepositorySystemSession session,
                                                  List<RemoteRepository> dominantRepositories,
                                                  List<RemoteRepository> recessiveRepositories, boolean recessiveIsRaw );

    RepositoryPolicy getPolicy( RepositorySystemSession session, RemoteRepository repository, boolean releases,
                                boolean snapshots );

    RepositoryConnector getRepositoryConnector( RepositorySystemSession session, RemoteRepository repository )
        throws NoRepositoryConnectorException;

}
