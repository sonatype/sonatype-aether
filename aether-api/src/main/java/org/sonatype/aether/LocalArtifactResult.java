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

import java.io.File;

/**
 * A result from the local repository about the existence of an artifact.
 * 
 * @author Benjamin Bentmann
 * @see LocalRepositoryManager#find(RepositorySystemSession, LocalArtifactRequest)
 */
public class LocalArtifactResult
{

    private final LocalArtifactRequest request;

    private File file;

    private boolean available;

    /**
     * Creates a new result for the specified request.
     * 
     * @param request The local artifact request, must not be {@code null}.
     */
    public LocalArtifactResult( LocalArtifactRequest request )
    {
        if ( request == null )
        {
            throw new IllegalArgumentException( "local artifact request has not been specified" );
        }
        this.request = request;
    }

    /**
     * Gets the request corresponding to this result.
     * 
     * @return The corresponding request, never {@code null}.
     */
    public LocalArtifactRequest getRequest()
    {
        return request;
    }

    /**
     * Gets the file to the requested artifact. Note that this file must not be used unless {@link #isAvailable()}
     * returns {@code true}. An artifact file can be found but considered unavailable if the artifact was cached from a
     * remote repository that is not part of the list of remote repositories used for this query.
     * 
     * @return The file to the requested artifact or {@code null}.
     */
    public File getFile()
    {
        return file;
    }

    /**
     * Sets the file to requested artifact.
     * 
     * @param file The artifact file, may be {@code null}.
     * @return This result for chaining, never {@code null}.
     */
    public LocalArtifactResult setFile( File file )
    {
        this.file = file;
        return this;
    }

    /**
     * Indicates whether the requested artifact is available for use. As a mininum, the file needs to be physically
     * existent in the local repository to be available. Additionally, a local repository manager can consider the list
     * of supplied remote repositories to determine whether the artifact is logically available and mark an artifact
     * unavailable if it is not known to be hosted by any of the provided repositories.
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
    public LocalArtifactResult setAvailable( boolean available )
    {
        this.available = available;
        return this;
    }

}
