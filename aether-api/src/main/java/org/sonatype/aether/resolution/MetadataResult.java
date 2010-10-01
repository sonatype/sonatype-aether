package org.sonatype.aether.resolution;

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

import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.metadata.Metadata;
import org.sonatype.aether.transfer.MetadataNotFoundException;

/**
 * The result of a metadata resolution request.
 * 
 * @author Benjamin Bentmann
 * @see RepositorySystem#resolveMetadata(RepositorySystemSession, java.util.Collection)
 */
public class MetadataResult
{

    private final MetadataRequest request;

    private Exception exception;

    private boolean updated;

    private Metadata metadata;

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
     * Gets the resolved metadata (if any).
     * 
     * @return The resolved metadata or {@code null} if the resolution failed.
     */
    public Metadata getMetadata()
    {
        return metadata;
    }

    /**
     * Sets the resolved metadata.
     * 
     * @param metadata The resolved metadata, may be {@code null} if the resolution failed.
     * @return This result for chaining, never {@code null}.
     */
    public MetadataResult setMetadata( Metadata metadata )
    {
        this.metadata = metadata;
        return this;
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
        return getMetadata() != null && getMetadata().getFile() != null;
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

    @Override
    public String toString()
    {
        return getMetadata() + ( isUpdated() ? " (updated)" : " (cached)" );
    }

}
