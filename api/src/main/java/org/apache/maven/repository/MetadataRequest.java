package org.apache.maven.repository;

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
 * A request to resolve metadata.
 * 
 * @author Benjamin Bentmann
 * @see RepositorySystem#resolveMetadata(RepositorySession, java.util.Collection)
 * @see Metadata#getFile()
 */
public class MetadataRequest
{

    private Metadata metadata;

    private RemoteRepository repository;

    private String context = "";

    private boolean deleteLocalCopyIfMissing;

    /**
     * Creates an unitialized request.
     */
    public MetadataRequest()
    {
        // enables default constructor
    }

    /**
     * Creates a request with the specified properties.
     * 
     * @param metadata The metadata to resolve, may be {@code null}.
     * @param repository The repository to resolve the metadata from, may be {@code null}.
     * @param context The context in which this request is made, may be {@code null}.
     */
    public MetadataRequest( Metadata metadata, RemoteRepository repository, String context )
    {
        setMetadata( metadata );
        setRepository( repository );
        setContext( context );
    }

    /**
     * Gets the metadata to resolve.
     * 
     * @return The metadata or {@code null} if not set.
     */
    public Metadata getMetadata()
    {
        return metadata;
    }

    /**
     * Sets the metadata to resolve.
     * 
     * @param metadata The metadata, may be {@code null}.
     * @return This request for chaining, never {@code null}.
     */
    public MetadataRequest setMetadata( Metadata metadata )
    {
        this.metadata = metadata;
        return this;
    }

    /**
     * Gets the repository from which the metadata should be resolved.
     * 
     * @return The repository or {@code null} if not set.
     */
    public RemoteRepository getRepository()
    {
        return repository;
    }

    /**
     * Sets the repository from which the metadata should be resolved.
     * 
     * @param repository The repository, may be {@code null}.
     * @return This request for chaining, never {@code null}.
     */
    public MetadataRequest setRepository( RemoteRepository repository )
    {
        this.repository = repository;
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
    public MetadataRequest setContext( String context )
    {
        this.context = ( context != null ) ? context : "";
        return this;
    }

    /**
     * Indicates whether the locally cached copy of the metadata should be removed if the corresponding file does not
     * exist (any more) in the remote repository.
     * 
     * @return {@code true} if locally cached metadata should be deleted if no corresponding remote file exists, {@code
     *         false} to keep the local copy.
     */
    public boolean isDeleteLocalCopyIfMissing()
    {
        return deleteLocalCopyIfMissing;
    }

    /**
     * Controls whether the locally cached copy of the metadata should be removed if the corresponding file does not
     * exist (any more) in the remote repository.
     * 
     * @param deleteLocalCopyIfMissing {@code true} if locally cached metadata should be deleted if no corresponding
     *            remote file exists, {@code false} to keep the local copy.
     * @return This request for chaining, never {@code null}.
     */
    public MetadataRequest setDeleteLocalCopyIfMissing( boolean deleteLocalCopyIfMissing )
    {
        this.deleteLocalCopyIfMissing = deleteLocalCopyIfMissing;
        return this;
    }

}
