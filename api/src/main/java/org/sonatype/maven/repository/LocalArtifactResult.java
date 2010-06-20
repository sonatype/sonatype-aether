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

import java.io.File;

/**
 * @author Benjamin Bentmann
 */
public class LocalArtifactResult
{

    private final LocalArtifactRequest request;

    private File file;

    private boolean available;

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
