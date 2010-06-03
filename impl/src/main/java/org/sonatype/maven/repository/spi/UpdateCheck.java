package org.sonatype.maven.repository.spi;

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

import org.sonatype.maven.repository.RemoteRepository;
import org.sonatype.maven.repository.RepositoryException;

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
