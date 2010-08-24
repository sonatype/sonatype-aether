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

    public long getLocalLastUpdated()
    {
        return localLastUpdated;
    }

    public UpdateCheck<T, E> setLocalLastUpdated( long localLastUpdated )
    {
        this.localLastUpdated = localLastUpdated;
        return this;
    }

    public T getItem()
    {
        return item;
    }

    public UpdateCheck<T, E> setItem( T item )
    {
        this.item = item;
        return this;
    }

    public File getFile()
    {
        return file;
    }

    public UpdateCheck<T, E> setFile( File file )
    {
        this.file = file;
        return this;
    }

    public String getPolicy()
    {
        return policy;
    }

    public UpdateCheck<T, E> setPolicy( String policy )
    {
        this.policy = policy;
        return this;
    }

    public RemoteRepository getRepository()
    {
        return repository;
    }

    public UpdateCheck<T, E> setRepository( RemoteRepository repository )
    {
        this.repository = repository;
        return this;
    }

    public boolean isRequired()
    {
        return required;
    }

    public UpdateCheck<T, E> setRequired( boolean required )
    {
        this.required = required;
        return this;
    }

    public E getException()
    {
        return exception;
    }

    public UpdateCheck<T, E> setException( E exception )
    {
        this.exception = exception;
        return this;
    }

}
