package org.apache.maven.repo;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A request to resolve an artifact.
 * 
 * @author Benjamin Bentmann
 * @see RepositorySystem#resolveArtifacts(RepositorySession, java.util.Collection)
 * @see Artifact#getFile()
 */
public class ArtifactRequest
{

    private Artifact artifact;

    private List<RemoteRepository> repositories = Collections.emptyList();

    private String context = "";

    /**
     * Creates an unitialized request.
     */
    public ArtifactRequest()
    {
        // enables default constructor
    }

    /**
     * Creates a request with the specified properties.
     * 
     * @param artifact The artifact to resolve, may be {@code null}.
     * @param repositories The repositories to resolve the artifact from, may be {@code null}.
     * @param context The context in which this request is made, may be {@code null}.
     */
    public ArtifactRequest( Artifact artifact, List<RemoteRepository> repositories, String context )
    {
        setArtifact( artifact );
        setRepositories( repositories );
        setContext( context );
    }

    /**
     * Gets the artifact to resolve.
     * 
     * @return The artifact to resolve or {@code null}
     */
    public Artifact getArtifact()
    {
        return artifact;
    }

    /**
     * Sets the artifact to resolve.
     * 
     * @param artifact The artifact to resolve, may be {@code null}.
     * @return This request for chaining, never {@code null}.
     */
    public ArtifactRequest setArtifact( Artifact artifact )
    {
        this.artifact = artifact;
        return this;
    }

    /**
     * Gets the repositories to resolve the version range from.
     * 
     * @return The repositories, never {@code null}.
     */
    public List<RemoteRepository> getRepositories()
    {
        return repositories;
    }

    /**
     * Sets the repositories to resolve the artifact from.
     * 
     * @param repositories The repositories, may be {@code null}.
     * @return This request for chaining, never {@code null}.
     */
    public ArtifactRequest setRepositories( List<RemoteRepository> repositories )
    {
        if ( repositories == null )
        {
            this.repositories = Collections.emptyList();
        }
        else
        {
            this.repositories = repositories;
        }
        return this;
    }

    /**
     * Adds the specified repository for the resolution.
     * 
     * @param repository The repository to add, may be {@code null}.
     * @return This request for chaining, never {@code null}.
     */
    public ArtifactRequest addRepository( RemoteRepository repository )
    {
        if ( repository != null )
        {
            if ( this.repositories.isEmpty() )
            {
                this.repositories = new ArrayList<RemoteRepository>();
            }
            this.repositories.add( repository );
        }
        return this;
    }

    /**
     * Gets the context in which this request is made.
     * 
     * @return The context, never {@code null}.
     */
    public String getContext()
    {
        return context;
    }

    /**
     * Sets the context in which this request is made.
     * 
     * @param context The context, may be {@code null}.
     * @return This request for chaining, never {@code null}.
     */
    public ArtifactRequest setContext( String context )
    {
        this.context = ( context != null ) ? context : "";
        return this;
    }

}
