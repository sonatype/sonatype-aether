package org.sonatype.aether;

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

import java.util.ArrayList;
import java.util.Collection;

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

}
