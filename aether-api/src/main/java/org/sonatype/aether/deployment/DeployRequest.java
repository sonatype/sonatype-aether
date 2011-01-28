package org.sonatype.aether.deployment;

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

import java.util.ArrayList;
import java.util.Collection;

import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.metadata.Metadata;
import org.sonatype.aether.repository.RemoteRepository;

/**
 * A request to deploy artifacts and their accompanying metadata into the a remote repository.
 * 
 * @author Benjamin Bentmann
 * @see RepositorySystem#deploy(RepositorySystemSession, DeployRequest)
 */
public class DeployRequest
{

    private Collection<Artifact> artifacts = new ArrayList<Artifact>();

    private Collection<Metadata> metadata = new ArrayList<Metadata>();

    private RemoteRepository repository;

    /**
     * Gets the artifact to deploy.
     * 
     * @return The artifacts to deploy, never {@code null}.
     */
    public Collection<Artifact> getArtifacts()
    {
        return artifacts;
    }

    /**
     * Sets the artifacts to deploy.
     * 
     * @param artifacts The artifacts to deploy, may be {@code null}.
     * @return This request for chaining, never {@code null}.
     */
    public DeployRequest setArtifacts( Collection<Artifact> artifacts )
    {
        if ( artifacts == null )
        {
            this.artifacts = new ArrayList<Artifact>();
        }
        else
        {
            this.artifacts = artifacts;
        }
        return this;
    }

    /**
     * Adds the specified artifacts for deployment.
     * 
     * @param artifact The artifact to add, may be {@code null}.
     * @return This request for chaining, never {@code null}.
     */
    public DeployRequest addArtifact( Artifact artifact )
    {
        if ( artifact != null )
        {
            artifacts.add( artifact );
        }
        return this;
    }

    /**
     * Gets the metadata to deploy.
     * 
     * @return The metadata to deploy, never {@code null}.
     */
    public Collection<Metadata> getMetadata()
    {
        return metadata;
    }

    /**
     * Sets the metadata to deploy.
     * 
     * @param metadata The metadata to deploy, may be {@code null}.
     * @return This request for chaining, never {@code null}.
     */
    public DeployRequest setMetadata( Collection<Metadata> metadata )
    {
        if ( metadata == null )
        {
            this.metadata = new ArrayList<Metadata>();
        }
        else
        {
            this.metadata = metadata;
        }
        return this;
    }

    /**
     * Adds the specified metadata for deployment.
     * 
     * @param metadata The metadata to add, may be {@code null}.
     * @return This request for chaining, never {@code null}.
     */
    public DeployRequest addMetadata( Metadata metadata )
    {
        if ( metadata != null )
        {
            this.metadata.add( metadata );
        }
        return this;
    }

    /**
     * Gets the repository to deploy to.
     * 
     * @return The repository to deploy to or {@code null} if not set.
     */
    public RemoteRepository getRepository()
    {
        return repository;
    }

    /**
     * Sets the repository to deploy to.
     * 
     * @param repository The repository to deploy to, may be {@code null}.
     * @return This request for chaining, never {@code null}.
     */
    public DeployRequest setRepository( RemoteRepository repository )
    {
        this.repository = repository;
        return this;
    }

    @Override
    public String toString()
    {
        return getArtifacts() + ", " + getMetadata() + " > " + getRepository();
    }

}
