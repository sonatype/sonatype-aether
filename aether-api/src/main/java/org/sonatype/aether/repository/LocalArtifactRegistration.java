package org.sonatype.aether.repository;

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

import java.util.Collection;
import java.util.Collections;

import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;

/**
 * A request to register an artifact within the local repository. Certain local repository implementations can refuse to
 * serve physically present artifacts if those haven't been previously registered to them.
 * 
 * @author Benjamin Bentmann
 * @see LocalRepositoryManager#add(RepositorySystemSession, LocalArtifactRegistration)
 */
public class LocalArtifactRegistration
{

    private Artifact artifact;

    private RemoteRepository repository;

    private Collection<String> contexts = Collections.emptyList();

    /**
     * Creates an uninitialized registration.
     */
    public LocalArtifactRegistration()
    {
        // enables default constructor
    }

    /**
     * Creates a registration request for the specified (locally built) artifact.
     * 
     * @param artifact The artifact to register, may be {@code null}.
     */
    public LocalArtifactRegistration( Artifact artifact )
    {
        setArtifact( artifact );
    }

    /**
     * Creates a registration request for the specified (remotely resolved) artifact.
     * 
     * @param artifact The artifact to register, may be {@code null}.
     * @param repository The remote repository from which the artifact was resolved or {@code null} if the artifact was
     *            locally built.
     * @param contexts The resolution contexts, may be {@code null}.
     */
    public LocalArtifactRegistration( Artifact artifact, RemoteRepository repository, Collection<String> contexts )
    {
        setArtifact( artifact );
        setRepository( repository );
        setContexts( contexts );
    }

    /**
     * Gets the artifact to register.
     * 
     * @return The artifact or {@code null} if not set.
     */
    public Artifact getArtifact()
    {
        return artifact;
    }

    /**
     * Sets the artifact to register.
     * 
     * @param artifact The artifact, may be {@code null}.
     * @return This request for chaining, never {@code null}.
     */
    public LocalArtifactRegistration setArtifact( Artifact artifact )
    {
        this.artifact = artifact;
        return this;
    }

    /**
     * Gets the remote repository from which the artifact was resolved.
     * 
     * @return The remote repositories or {@code null} if the artifact was locally built.
     */
    public RemoteRepository getRepository()
    {
        return repository;
    }

    /**
     * Sets the remote repository from which the artifact was resolved.
     * 
     * @param repository The remote repository or {@code null} if the artifact was locally built.
     * @return This request for chaining, never {@code null}.
     */
    public LocalArtifactRegistration setRepository( RemoteRepository repository )
    {
        this.repository = repository;
        return this;
    }

    /**
     * Gets the resolution contexts in which the artifact is available.
     * 
     * @return The resolution contexts in which the artifact is available, never {@code null}.
     */
    public Collection<String> getContexts()
    {
        return contexts;
    }

    /**
     * Sets the resolution contexts in which the artifact is available.
     * 
     * @param contexts The resolution contexts, may be {@code null}.
     * @return This request for chaining, never {@code null}.
     */
    public LocalArtifactRegistration setContexts( Collection<String> contexts )
    {
        if ( contexts != null )
        {
            this.contexts = contexts;
        }
        else
        {
            this.contexts = Collections.emptyList();
        }
        return this;
    }

}
