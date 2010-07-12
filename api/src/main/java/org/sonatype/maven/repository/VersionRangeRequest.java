package org.sonatype.maven.repository;

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
import java.util.Collections;
import java.util.List;

/**
 * A request to resolve a version range.
 * 
 * @author Benjamin Bentmann
 * @see RepositorySystem#resolveVersionRange(RepositorySession, VersionRangeRequest)
 */
public class VersionRangeRequest
{

    private Artifact artifact;

    private List<RemoteRepository> repositories = Collections.emptyList();

    private String context = "";

    /**
     * Creates an uninitialized request.
     */
    public VersionRangeRequest()
    {
        // enables default constructor
    }

    /**
     * Creates a request with the specified properties.
     * 
     * @param artifact The artifact whose version range should be resolved, may be {@code null}.
     * @param repositories The repositories to resolve the version from, may be {@code null}.
     * @param context The context in which this request is made, may be {@code null}.
     */
    public VersionRangeRequest( Artifact artifact, List<RemoteRepository> repositories, String context )
    {
        setArtifact( artifact );
        setRepositories( repositories );
        setRequestContext( context );
    }

    /**
     * Gets the artifact whose version range shall be resolved.
     * 
     * @return The artifact or {@code null} if not set.
     */
    public Artifact getArtifact()
    {
        return artifact;
    }

    /**
     * Sets the artifact whose version range shall be resolved.
     * 
     * @param artifact The artifact, may be {@code null}.
     * @return This request for chaining, never {@code null}.
     */
    public VersionRangeRequest setArtifact( Artifact artifact )
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
     * Sets the repositories to resolve the version range from.
     * 
     * @param repositories The repositories, may be {@code null}.
     * @return This request for chaining, never {@code null}.
     */
    public VersionRangeRequest setRepositories( List<RemoteRepository> repositories )
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
    public VersionRangeRequest addRepository( RemoteRepository repository )
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
    public String getRequestContext()
    {
        return context;
    }

    /**
     * Sets the context in which this request is made.
     * 
     * @param context The context, may be {@code null}.
     * @return This request for chaining, never {@code null}.
     */
    public VersionRangeRequest setRequestContext( String context )
    {
        this.context = ( context != null ) ? context : "";
        return this;
    }

}
