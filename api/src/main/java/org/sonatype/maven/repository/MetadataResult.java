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

/**
 * The result of a metadata resolution request.
 * 
 * @author Benjamin Bentmann
 * @see RepositorySystem#resolveMetadata(RepositorySession, java.util.Collection)
 */
public class MetadataResult
{

    private final MetadataRequest request;

    private Exception exception;

    private boolean updated;

    /**
     * Creates a new result for the specified request.
     * 
     * @param request The resolution request, must not be {@code null}.
     */
    public MetadataResult( MetadataRequest request )
    {
        if ( request == null )
        {
            throw new IllegalArgumentException( "metadata request has not been specified" );
        }
        this.request = request;
    }

    /**
     * Gets the resolution request that was made.
     * 
     * @return The resolution request, never {@code null}.
     */
    public MetadataRequest getRequest()
    {
        return request;
    }

    /**
     * Records the specified exception while resolving the metadata.
     * 
     * @param exception The exception to record, may be {@code null}.
     * @return This result for chaining, never {@code null}.
     */
    public MetadataResult setException( Exception exception )
    {
        this.exception = exception;
        return this;
    }

    /**
     * Gets the exception that occurred while resolving the metadata.
     * 
     * @return The exception that occurred or {@code null} if none.
     */
    public Exception getException()
    {
        return exception;
    }

    /**
     * Sets the updated flag for the metadata.
     * 
     * @param updated {@code true} if the metadata was actually fetched from the remote repository during the
     *            resolution, {@code false} if the metadata was resolved from a locally cached copy.
     * @return This result for chaining, never {@code null}.
     */
    public MetadataResult setUpdated( boolean updated )
    {
        this.updated = updated;
        return this;
    }

    /**
     * Indicates whether the metadata was actually fetched from the remote repository or resolved from the local cache.
     * If metadata has been locally cached during a previous resolution request and this local copy is still up-to-date
     * according to the remote repository's update policy, no remote access is made.
     * 
     * @return {@code true} if the metadata was actually fetched from the remote repository during the resolution,
     *         {@code false} if the metadata was resolved from a locally cached copy.
     */
    public boolean isUpdated()
    {
        return updated;
    }

    /**
     * Indicates whether the requested metadata was resolved. Note that the metadata might have been successfully
     * resolved (from the local cache) despite {@link #getException()} indicating a transfer error while trying to
     * refetch the metadata from the remote repository.
     * 
     * @return {@code true} if the metadata was resolved, {@code false} otherwise.
     * @see Metadata#getFile()
     */
    public boolean isResolved()
    {
        return getRequest().getMetadata().getFile() != null;
    }

    /**
     * Indicates whether the requested metadata is not present in the remote repository.
     * 
     * @return {@code true} if the metadata is not present in the remote repository, {@code false} otherwise.
     */
    public boolean isMissing()
    {
        return getException() instanceof MetadataNotFoundException;
    }

}
