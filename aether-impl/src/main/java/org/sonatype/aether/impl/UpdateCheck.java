package org.sonatype.aether.impl;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *******************************************************************************/

import java.io.File;

import org.sonatype.aether.RepositoryException;
import org.sonatype.aether.repository.RemoteRepository;

/**
 * A request to check if an update of an artifact/metadata from a remote repository is needed.
 * 
 * @see UpdateCheckManager
 * @author Benjamin Bentmann
 */
public class UpdateCheck<T, E extends RepositoryException>
{

    private long localLastUpdated;

    private T item;

    private File file;

    private String policy;

    private RemoteRepository repository;

    private RemoteRepository authoritativeRepository;

    private boolean required;

    private E exception;

    /**
     * Gets the last-modified timestamp of the corresponding item produced by a local installation. If non-zero, a
     * remote update will be surpressed if the local item is up-to-date, even if the remote item has not been cached
     * locally.
     * 
     * @return The last-modified timestamp of the corresponding item produced by a local installation or {@code 0} to
     *         ignore any local item.
     */
    public long getLocalLastUpdated()
    {
        return localLastUpdated;
    }

    /**
     * Sets the last-modified timestamp of the corresponding item produced by a local installation. If non-zero, a
     * remote update will be surpressed if the local item is up-to-date, even if the remote item has not been cached
     * locally.
     * 
     * @param localLastUpdated The last-modified timestamp of the corresponding item produced by a local installation or
     *            {@code 0} to ignore any local item.
     * @return This object for chaining.
     */
    public UpdateCheck<T, E> setLocalLastUpdated( long localLastUpdated )
    {
        this.localLastUpdated = localLastUpdated;
        return this;
    }

    /**
     * Gets the item of the check.
     * 
     * @return The item of the check, never {@code null}.
     */
    public T getItem()
    {
        return item;
    }

    /**
     * Sets the item of the check.
     * 
     * @param item The item of the check, must not be {@code null}.
     * @return This object for chaining.
     */
    public UpdateCheck<T, E> setItem( T item )
    {
        this.item = item;
        return this;
    }

    /**
     * Returns the local file of the item.
     * 
     * @return The local file of the item.
     */
    public File getFile()
    {
        return file;
    }

    /**
     * Sets the local file of the item.
     * 
     * @param file The file of the item, never {@code null} .
     * @return This object for chaining.
     */
    public UpdateCheck<T, E> setFile( File file )
    {
        this.file = file;
        return this;
    }

    /**
     * Gets the policy to use for the check.
     * 
     * @return The policy to use for the check.
     * @see org.sonatype.aether.repository.RepositoryPolicy
     */
    public String getPolicy()
    {
        return policy;
    }

    /**
     * Sets the policy to use for the check.
     * 
     * @param policy The policy to use for the check, may be {@code null}.
     * @return This object for chaining.
     * @see org.sonatype.aether.repository.RepositoryPolicy
     */
    public UpdateCheck<T, E> setPolicy( String policy )
    {
        this.policy = policy;
        return this;
    }

    /**
     * Gets the repository from which a potential update/download will performed.
     * 
     * @return The repository to use for the check.
     */
    public RemoteRepository getRepository()
    {
        return repository;
    }

    /**
     * Sets the repository from which a potential update/download will performed.
     * 
     * @param repository The repository to use for the check, must not be {@code null}.
     * @return This object for chaining.
     */
    public UpdateCheck<T, E> setRepository( RemoteRepository repository )
    {
        this.repository = repository;
        return this;
    }

    /**
     * Gets the repository which ultimately hosts the metadata to update. This will be different from the repository
     * given by {@link #getRepository()} in case the latter denotes a repository manager.
     * 
     * @return The actual repository hosting the authoritative copy of the metadata to update, never {@code null} for a
     *         metadata update check.
     */
    public RemoteRepository getAuthoritativeRepository()
    {
        return authoritativeRepository != null ? authoritativeRepository : repository;
    }

    /**
     * Sets the repository which ultimately hosts the metadata to update. This will be different from the repository
     * given by {@link #getRepository()} in case the latter denotes a repository manager.
     * 
     * @param authoritativeRepository The actual repository hosting the authoritative copy of the metadata to update,
     *            must not be {@code null} for a metadata update check.
     * @return This object for chaining.
     */
    public UpdateCheck<T, E> setAuthoritativeRepository( RemoteRepository authoritativeRepository )
    {
        this.authoritativeRepository = authoritativeRepository;
        return this;
    }

    /**
     * Gets the result of a check, denoting whether the remote repository should be checked for updates.
     * 
     * @return The result of a check.
     */
    public boolean isRequired()
    {
        return required;
    }

    /**
     * Sets the result of an update check.
     * 
     * @param required the result of an update check.
     * @return This object for chaining.
     */
    public UpdateCheck<T, E> setRequired( boolean required )
    {
        this.required = required;
        return this;
    }

    /**
     * Gets the exception that occurred during the update check.
     * 
     * @return The occurred exception or {@code null} if the update check was successful.
     */
    public E getException()
    {
        return exception;
    }

    /**
     * Sets the exception for this update check.
     * 
     * @param exception The exception for this update check, may be {@code null} if the check was successful.
     * @return This object for chaining.
     */
    public UpdateCheck<T, E> setException( E exception )
    {
        this.exception = exception;
        return this;
    }

}
