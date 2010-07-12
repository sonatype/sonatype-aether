package org.sonatype.maven.repository.util;

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
