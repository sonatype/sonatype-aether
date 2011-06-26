package org.sonatype.aether.impl.internal;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import java.io.File;

import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.impl.LocalRepositoryEvent;
import org.sonatype.aether.repository.LocalRepository;

/**
 * @author Benjamin Bentmann
 */
@SuppressWarnings( "deprecation" )
public class DefaultLocalRepositoryEvent
    implements LocalRepositoryEvent
{

    private final EventType type;

    private final RepositorySystemSession session;

    private final LocalRepository repository;

    private final Artifact artifact;

    private final File file;

    public DefaultLocalRepositoryEvent( EventType type, RepositorySystemSession session, Artifact artifact, File file )
    {
        if ( type == null )
        {
            throw new IllegalArgumentException( "event type missing" );
        }
        if ( session == null )
        {
            throw new IllegalArgumentException( "repository system session missing" );
        }
        if ( artifact == null )
        {
            throw new IllegalArgumentException( "artifact missing" );
        }
        this.type = type;
        this.session = session;
        this.repository = session.getLocalRepository();
        this.artifact = artifact;
        this.file = file;
    }

    public EventType getType()
    {
        return type;
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

    public File getFile()
    {
        return file;
    }

    @Override
    public String toString()
    {
        return getType() + " " + getArtifact() + " > " + getFile();
    }

}
