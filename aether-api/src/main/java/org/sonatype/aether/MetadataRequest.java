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

/**
 * A request to resolve metadata from either a remote repository or the local repository.
 * 
 * @author Benjamin Bentmann
 * @see RepositorySystem#resolveMetadata(RepositorySystemSession, java.util.Collection)
 * @see Metadata#getFile()
 */
public class MetadataRequest
{

    private Metadata metadata;

    private RemoteRepository repository;

    private String context = "";

    private boolean deleteLocalCopyIfMissing;

    private boolean favorLocalRepository;

    /**
     * Creates an unitialized request.
     */
    public MetadataRequest()
    {
        // enables default constructor
    }

    /**
     * Creates a request to resolve the specified metadata from the local repository.
     * 
     * @param metadata The metadata to resolve, may be {@code null}.
     */
    public MetadataRequest( Metadata metadata )
    {
        setMetadata( metadata );
    }

    /**
     * Creates a request with the specified properties.
     * 
     * @param metadata The metadata to resolve, may be {@code null}.
     * @param repository The repository to resolve the metadata from, may be {@code null} to resolve from the local
     *            repository.
     * @param context The context in which this request is made, may be {@code null}.
     */
    public MetadataRequest( Metadata metadata, RemoteRepository repository, String context )
    {
        setMetadata( metadata );
        setRepository( repository );
        setRequestContext( context );
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
     * @return The repository or {@code null} to resolve from the local repository.
     */
    public RemoteRepository getRepository()
    {
        return repository;
    }

    /**
     * Sets the repository from which the metadata should be resolved.
     * 
     * @param repository The repository, may be {@code null} to resolve from the local repository.
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
    public MetadataRequest setRequestContext( String context )
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

    /**
     * Indicates whether the metadata resolution should be suppressed if the corresponding metadata of the local
     * repository is up-to-date according to the update policy of the remote repository. In this case, the metadata
     * resolution will even be suppressed if no local copy of the remote metadata exists yet.
     * 
     * @return {@code true} to suppress resolution of remote metadata if the corresponding metadata of the local
     *         repository is up-to-date, {@code false} to resolve the remote metadata normally according to the update
     *         policy.
     */
    public boolean isFavorLocalRepository()
    {
        return favorLocalRepository;
    }

    /**
     * Controls resolution of remote metadata when already corresponding metadata of the local repository exists. In
     * cases where the local repository's metadata is sufficient and going to be preferred, resolution of the remote
     * metadata can be suppressed to avoid unneccessary network access.
     * 
     * @param favorLocalRepository {@code true} to suppress resolution of remote metadata if the corresponding metadata
     *            of the local repository is up-to-date, {@code false} to resolve the remote metadata normally according
     *            to the update policy.
     * @return This request for chaining, never {@code null}.
     */
    public MetadataRequest setFavorLocalRepository( boolean favorLocalRepository )
    {
        this.favorLocalRepository = favorLocalRepository;
        return this;
    }

}
