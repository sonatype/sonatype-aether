package org.sonatype.aether.impl;

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

import org.sonatype.aether.RepositoryException;
import org.sonatype.aether.repository.RemoteRepository;

/**
 * A request to check if an update from a remote repository is needed.
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

    private boolean required;

    private E exception;

    /**
     * Get the alternative reference time to use by the {@link UpdateCheckManager}.
     * 
     * @return the alternative reference time to use by the {@link UpdateCheckManager}.
     */
    public long getLocalLastUpdated()
    {
        return localLastUpdated;
    }

    /**
     * Sets the alternative reference time to use by the {@link UpdateCheckManager}.
     * 
     * @param localLastUpdated the reference time to use for the check.
     * @return this object for chaining.
     */
    public UpdateCheck<T, E> setLocalLastUpdated( long localLastUpdated )
    {
        this.localLastUpdated = localLastUpdated;
        return this;
    }

    /**
     * Returns the item of the check.
     * 
     * @return the item of the check, never @ null} .
     */
    public T getItem()
    {
        return item;
    }

    /**
     * Sets the item of the check.
     * 
     * @param item the item of the check, must not be {@code null}.
     * @return this object for chaining.
     */
    public UpdateCheck<T, E> setItem( T item )
    {
        this.item = item;
        return this;
    }

    /**
     * Returns the local file of the item.
     * 
     * @return the local file of the item.
     */
    public File getFile()
    {
        return file;
    }

    /**
     * Sets the local file of the item.
     * 
     * @param file the file of the item, never {@code null} .
     * @return this object for chaining.
     */
    public UpdateCheck<T, E> setFile( File file )
    {
        this.file = file;
        return this;
    }

    /**
     * Returns the policy to use for the check.
     * 
     * @return the policy to use for the check.
     * @see org.sonatype.aether.repository.RepositoryPolicy
     */
    public String getPolicy()
    {
        return policy;
    }

    /**
     * Sets the policy to use for the check.
     * 
     * @param policy the policy to use for the check, may be {@code null}.
     * @return this object for chaining.
     * @see org.sonatype.aether.repository.RepositoryPolicy
     */
    public UpdateCheck<T, E> setPolicy( String policy )
    {
        this.policy = policy;
        return this;
    }

    /**
     * Gets the repository to use for the check.
     * 
     * @return the repository to use for the check.
     */
    public RemoteRepository getRepository()
    {
        return repository;
    }

    /**
     * Sets the repository to use for the check.
     * 
     * @param repository the repository to use for the check, must not be {@code null}.
     * @return this object for chaining.
     */
    public UpdateCheck<T, E> setRepository( RemoteRepository repository )
    {
        this.repository = repository;
        return this;
    }

    /**
     * Returns the result of a check, denoting whether the remote repository should be checked for updates.
     * 
     * @return the result of a check.
     */
    public boolean isRequired()
    {
        return required;
    }

    /**
     * Sets the result of an update check.
     * 
     * @param required the result of an update check.
     * @return this object for chaining.
     */
    public UpdateCheck<T, E> setRequired( boolean required )
    {
        this.required = required;
        return this;
    }

    /**
     * Returns the exception if an exception occured during the check, {@code null} otherwise.
     * 
     * @return the occured exception, may be {@code null}.
     */
    public E getException()
    {
        return exception;
    }

    /**
     * Sets the exception for this update check.
     * 
     * @param exception the exception for this update check.
     * @return this object for chaining.
     */
    public UpdateCheck<T, E> setException( E exception )
    {
        this.exception = exception;
        return this;
    }

}
