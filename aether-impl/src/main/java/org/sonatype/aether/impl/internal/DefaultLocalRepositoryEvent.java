package org.sonatype.aether.impl.internal;

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

import org.sonatype.aether.Artifact;
import org.sonatype.aether.LocalRepository;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.impl.LocalRepositoryEvent;

/**
 * @author Benjamin Bentmann
 */
class DefaultLocalRepositoryEvent
    implements LocalRepositoryEvent
{

    private final RepositorySystemSession session;

    private final LocalRepository repository;

    private final Artifact artifact;

    public DefaultLocalRepositoryEvent( RepositorySystemSession session, Artifact artifact )
    {
        this.session = session;
        this.repository = session.getLocalRepository();
        this.artifact = artifact;
    }

    public RepositorySystemSession getSession()
    {
        return session;
    }

    public LocalRepository getRepository()
    {
        return repository;
    }

    public Artifact getArtifact()
    {
        return artifact;
    }

}
