package org.sonatype.maven.repository.util;

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

import java.util.Collections;

import org.sonatype.maven.repository.MirrorSelector;
import org.sonatype.maven.repository.RemoteRepository;

/**
 * @author Benjamin Bentmann
 */
public class RepoManMirrorSelector
    implements MirrorSelector
{

    private String id;

    private String url;

    private String type;

    public RepoManMirrorSelector setRepositoryManager( String id, String url, String type )
    {
        this.id = id;
        this.url = url;
        this.type = type;

        return this;
    }

    public RemoteRepository getMirror( RemoteRepository repository )
    {
        RemoteRepository repo = new RemoteRepository( id, type, url );
        repo.setRepositoryManager( true );
        // TODO: what about the policies?
        repo.setMirroredRepositories( Collections.singletonList( repository ) );
        return repo;
    }

}
