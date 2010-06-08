package org.sonatype.maven.repository;

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
import java.util.List;

/**
 * The result of an artifact resolution request.
 * 
 * @author Benjamin Bentmann
 * @see RepositorySystem#resolveArtifacts(RepositorySession, java.util.Collection)
 */
public class ArtifactResult
{

    private final ArtifactRequest request;

    private final List<Exception> exceptions;

    private ArtifactRepository repository;

    /**
     * Creates a new result for the specified request.
     * 
     * @param request The resolution request, must not be {@code null}.
     */
    public ArtifactResult( ArtifactRequest request )
    {
        if ( request == null )
        {
            throw new IllegalArgumentException( "resolution request has not been specified" );
        }
        this.request = request;
        this.exceptions = new ArrayList<Exception>( 4 );
    }

    /**
     * Gets the resolution request that was made.
     * 
     * @return The resolution request, never {@code null}.
     */
    public ArtifactRequest getRequest()
    {
        return request;
    }

    /**
     * Gets the exceptions that occurred while resolving the artifact.
     * 
     * @return The exceptions that occurred, never {@code null}.
     */
    public List<Exception> getExceptions()
    {
        return exceptions;
    }

    /**
     * Records the specified exception while resolving the artifact.
     * 
     * @param exception The exception to record, may be {@code null}.
     * @return This result for chaining, never {@code null}.
     */
    public ArtifactResult addException( Exception exception )
    {
        if ( exception != null )
        {
            this.exceptions.add( exception );
        }
        return this;
    }

    /**
     * Gets the repository from which the artifact was eventually resolved.
     * 
     * @return The repository from which the artifact was resolved or {@code null} if unknown.
     */
    public ArtifactRepository getRepository()
    {
        return repository;
    }

    /**
     * Sets the repository from which the artifact was resolved.
     * 
     * @param repository The repository from which the artifact was resolved, may be {@code null}.
     * @return This result for chaining, never {@code null}.
     */
    public ArtifactResult setRepository( ArtifactRepository repository )
    {
        this.repository = repository;
        return this;
    }

    /**
     * Indicates whether the requested artifact was resolved. Note that the artifact might have been successfully
     * resolved despite {@link #getExceptions()} indicating transfer errors while trying to fetch the artifact from some
     * of the specified remote repositories.
     * 
     * @return {@code true} if the artifact was resolved, {@code false} otherwise.
     * @see Artifact#getFile()
     */
    public boolean isResolved()
    {
        return getRequest().getArtifact().getFile() != null;
    }

    /**
     * Indicates whether the requested artifact is not present in any of the specified repositories.
     * 
     * @return {@code true} if the artifact is not present in any repository, {@code false} otherwise.
     */
    public boolean isMissing()
    {
        for ( Exception e : getExceptions() )
        {
            if ( !( e instanceof ArtifactNotFoundException ) )
            {
                return false;
            }
        }
        return !isResolved();
    }

}
