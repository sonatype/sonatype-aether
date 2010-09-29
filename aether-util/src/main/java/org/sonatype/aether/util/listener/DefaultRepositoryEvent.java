package org.sonatype.aether.util.listener;

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

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.sonatype.aether.RepositoryEvent;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.metadata.Metadata;
import org.sonatype.aether.repository.ArtifactRepository;

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
        if ( exception != null )
        {
            this.exceptions = Collections.singletonList( exception );
        }
        else
        {
            this.exceptions = Collections.emptyList();
        }
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
