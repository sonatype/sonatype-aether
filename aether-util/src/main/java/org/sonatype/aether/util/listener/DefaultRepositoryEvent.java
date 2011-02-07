package org.sonatype.aether.util.listener;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *******************************************************************************/

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.sonatype.aether.RepositoryEvent;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.metadata.Metadata;
import org.sonatype.aether.repository.ArtifactRepository;

/**
 * A simple repository event.
 * 
 * @author Benjamin Bentmann
 */
public class DefaultRepositoryEvent
    implements RepositoryEvent
{

    private EventType type;

    private RepositorySystemSession session;

    private Artifact artifact;

    private Metadata metadata;

    private ArtifactRepository repository;

    private File file;

    private List<Exception> exceptions = Collections.emptyList();

    public DefaultRepositoryEvent( EventType type, RepositorySystemSession session )
    {
        setType( type );
        setSession( session );
    }

    public EventType getType()
    {
        return type;
    }

    /**
     * Sets the type of the event.
     * 
     * @param type The type of the event, must not be {@code null}.
     * @return This event for chaining, never {@code null}.
     */
    private DefaultRepositoryEvent setType( EventType type )
    {
        if ( type == null )
        {
            throw new IllegalArgumentException( "event type not specified" );
        }
        this.type = type;
        return this;
    }

    public RepositorySystemSession getSession()
    {
        return session;
    }

    private DefaultRepositoryEvent setSession( RepositorySystemSession session )
    {
        if ( session == null )
        {
            throw new IllegalArgumentException( "session not specified" );
        }
        this.session = session;
        return this;
    }

    public Artifact getArtifact()
    {
        return artifact;
    }

    /**
     * Sets the artifact involved in the event.
     * 
     * @param artifact The involved artifact, may be {@code null}.
     * @return This event for chaining, never {@code null}.
     */
    public DefaultRepositoryEvent setArtifact( Artifact artifact )
    {
        this.artifact = artifact;
        return this;
    }

    public Metadata getMetadata()
    {
        return metadata;
    }

    /**
     * Sets the metadata involved in the event.
     * 
     * @param metadata The involved metadata, may be {@code null}.
     * @return This event for chaining, never {@code null}.
     */
    public DefaultRepositoryEvent setMetadata( Metadata metadata )
    {
        this.metadata = metadata;
        return this;
    }

    public ArtifactRepository getRepository()
    {
        return repository;
    }

    /**
     * Sets the repository involved in the event.
     * 
     * @param repository The involved repository, may be {@code null}.
     * @return This event for chaining, never {@code null}.
     */
    public DefaultRepositoryEvent setRepository( ArtifactRepository repository )
    {
        this.repository = repository;
        return this;
    }

    public File getFile()
    {
        return file;
    }

    /**
     * Sets the file involved in the event.
     * 
     * @param file The involved file, may be {@code null}.
     * @return This event for chaining, never {@code null}.
     */
    public DefaultRepositoryEvent setFile( File file )
    {
        this.file = file;
        return this;
    }

    public Exception getException()
    {
        return exceptions.isEmpty() ? null : exceptions.get( 0 );
    }

    /**
     * Sets the exception causing the event.
     * 
     * @param exception The exception causing the event, may be {@code null}.
     * @return This event for chaining, never {@code null}.
     */
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

    /**
     * Sets the exceptions causing the event.
     * 
     * @param exceptions The exceptions causing the event, may be {@code null}.
     * @return This event for chaining, never {@code null}.
     */
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

    @Override
    public String toString()
    {
        StringBuilder buffer = new StringBuilder( 256 );
        buffer.append( getType() );
        if ( getArtifact() != null )
        {
            buffer.append( " " ).append( getArtifact() );
        }
        if ( getMetadata() != null )
        {
            buffer.append( " " ).append( getMetadata() );
        }
        if ( getFile() != null )
        {
            buffer.append( " (" ).append( getFile() ).append( ")" );
        }
        if ( getRepository() != null )
        {
            buffer.append( " @ " ).append( getRepository() );
        }
        return buffer.toString();
    }

}
