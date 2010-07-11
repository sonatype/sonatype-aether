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

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.sonatype.maven.repository.Artifact;
import org.sonatype.maven.repository.ArtifactRepository;
import org.sonatype.maven.repository.Metadata;
import org.sonatype.maven.repository.RepositoryEvent;
import org.sonatype.maven.repository.RepositorySystemSession;

/**
 * @author Benjamin Bentmann
 */
public class DefaultRepositoryEvent
    implements RepositoryEvent
{

    private RepositorySystemSession session;

    private Artifact artifact;

    private Metadata metadata;

    private ArtifactRepository repository;

    private File file;

    private List<Exception> exceptions = Collections.emptyList();

    public DefaultRepositoryEvent( RepositorySystemSession session, Artifact artifact )
    {
        this.session = session;
        this.artifact = artifact;
    }

    public DefaultRepositoryEvent( RepositorySystemSession session, Metadata metadata )
    {
        this.session = session;
        this.metadata = metadata;
    }

    public RepositorySystemSession getSession()
    {
        return session;
    }

    public Artifact getArtifact()
    {
        return artifact;
    }

    public Metadata getMetadata()
    {
        return metadata;
    }

    public ArtifactRepository getRepository()
    {
        return repository;
    }

    public DefaultRepositoryEvent setRepository( ArtifactRepository repository )
    {
        this.repository = repository;
        return this;
    }

    public File getFile()
    {
        return file;
    }

    public DefaultRepositoryEvent setFile( File file )
    {
        this.file = file;
        return this;
    }

    public Exception getException()
    {
        return exceptions.isEmpty() ? null : exceptions.get( 0 );
    }

    public DefaultRepositoryEvent setException( Exception exception )
    {
        this.exceptions = Collections.singletonList( exception );
        return this;
    }

    public List<Exception> getExceptions()
    {
        return exceptions;
    }

    public DefaultRepositoryEvent setExceptions( List<Exception> exceptions )
    {
        if ( exceptions != null )
        {
            this.exceptions = exceptions;
        }
        else
        {
            this.exceptions = Collections.emptyList();
        }
        return this;
    }

}
