package org.sonatype.aether.repository;

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

import org.sonatype.aether.RepositoryException;

public class NoLocalRepositoryManagerException
    extends RepositoryException
{

    private final LocalRepository repository;

    public NoLocalRepositoryManagerException( LocalRepository repository )
    {
        this( repository, toMessage( repository ) );
    }

    public NoLocalRepositoryManagerException( LocalRepository repository, String message )
    {
        super( message );

        this.repository = repository;
    }

    public NoLocalRepositoryManagerException( LocalRepository repository, Throwable cause )
    {
        this( repository, toMessage( repository ), cause );
    }

    public NoLocalRepositoryManagerException( LocalRepository repository, String message, Throwable cause )
    {
        super( message, cause );

        this.repository = repository;
    }

    private static String toMessage( LocalRepository repository )
    {
        if ( repository != null )
        {
            return "No manager available for local repository (" + repository.getBasedir().getAbsolutePath()
                + ") of type " + repository.getContentType();
        }
        else
        {
            return "No connector available to access repository";
        }
    }

    public LocalRepository getRepository()
    {
        return repository;
    }

}
