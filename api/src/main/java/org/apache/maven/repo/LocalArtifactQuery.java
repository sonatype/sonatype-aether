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

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * A query to the local repository for the existence of an artifact.
 * 
 * @author Benjamin Bentmann
 */
public class LocalArtifactQuery
{

    private Artifact artifact;

    private String context = "";

    private List<RemoteRepository> repositories = Collections.emptyList();

    private File file;

    private boolean available;

    /**
     * Creates an uninitialized query.
     */
    public LocalArtifactQuery()
    {
        // enables default constructor
    }

    /**
     * Creates a query with the specified properties.
     * 
     * @param artifact The artifact to query for, may be {@code null}.
     * @param repositories The remote repositories that should be considered as potential sources for the artifact, may
     *            be {@code null}.
     * @param context The resolution context for the artifact, may be {@code null}.
     */
    public LocalArtifactQuery( Artifact artifact, List<RemoteRepository> repositories, String context )
    {
        setArtifact( artifact );
        setRepositories( repositories );
        setContext( context );
    }

    /**
     * Gets the artifact to query for.
     * 
     * @return The artifact or {@code null} if not set.
     */
    public Artifact getArtifact()
    {
        return artifact;
    }

    /**
     * Sets the artifact to query for.
     * 
     * @param artifact The artifact, may be {@code null}.
     * @return This query for chaining, never {@code null}.
     */
    public LocalArtifactQuery setArtifact( Artifact artifact )
    {
        this.artifact = artifact;
        return this;
    }

    /**
     * Gets the resolution context.
     * 
     * @return The resolution context, never {@code null}.
     */
    public String getContext()
    {
        return context;
    }

    /**
     * Sets the resolution context.
     * 
     * @param context The resolution context, may be {@code null}.
     * @return This query for chaining, never {@code null}.
     */
    public LocalArtifactQuery setContext( String context )
    {
        this.context = ( context != null ) ? context : "";
        return this;
    }

    /**
     * Gets the remote repositories to consider as sources of the artifact.
     * 
     * @return The remote repositories, never {@code null}.
     */
    public List<RemoteRepository> getRepositories()
    {
        return repositories;
    }

    /**
     * Sets the remote repositories to consider as sources of the artifact.
     * 
     * @param repositories The remote repositories, may be {@code null}.
     * @return This query for chaining, never {@code null}.
     */
    public LocalArtifactQuery setRepositories( List<RemoteRepository> repositories )
    {
        if ( repositories != null )
        {
            this.repositories = repositories;
        }
        else
        {
            this.repositories = Collections.emptyList();
        }
        return this;
    }

    /**
     * Gets the file to the requested artifact. Note that this file must not be used unless {@link #isAvailable()}
     * returns {@code true}. An artifact file can be found but considered unavailable if the artifact was cached from a
     * remote repository that is not part of the list of remote repositories used for this query.
     * 
     * @return The file to the requested artifact.
     */
    public File getFile()
    {
        return file;
    }

    /**
     * Sets the file to requested artifact.
     * 
     * @param file The artifact file, may be {@code null}.
     * @return This query for chaining, never {@code null}.
     */
    public LocalArtifactQuery setFile( File file )
    {
        this.file = file;
        return this;
    }

    /**
     * Indicates whether the requested artifact is available for use. As a mininum, the file needs to be physically
     * existent in the local repository to be available. Additionally, a local repository manager can consider the list
     * of supplied remote repositories to determine whether the artifact is logically available.
     * 
     * @return {@code true} if the artifact is available, {@code false} otherwise.
     */
    public boolean isAvailable()
    {
        return available;
    }

    /**
     * Sets whether the artifact is available.
     * 
     * @param available {@code true} if the artifact is available, {@code false} otherwise.
     * @return This query for chaining, never {@code null}.
     */
    public LocalArtifactQuery setAvailable( boolean available )
    {
        this.available = available;
        return this;
    }

}
