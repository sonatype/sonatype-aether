package org.sonatype.maven.repository.internal;

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

import org.sonatype.maven.repository.Artifact;
import org.sonatype.maven.repository.LocalRepository;
import org.sonatype.maven.repository.RepositorySession;
import org.sonatype.maven.repository.spi.LocalRepositoryEvent;

/**
 * @author Benjamin Bentmann
 */
public class DefaultLocalRepositoryEvent
    implements LocalRepositoryEvent
{

    private final RepositorySession session;

    private final LocalRepository repository;

    private final Artifact artifact;

    public DefaultLocalRepositoryEvent( RepositorySession session, Artifact artifact )
    {
        this.session = session;
        this.repository = session.getLocalRepository();
        this.artifact = artifact;
    }

    public RepositorySession getSession()
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
