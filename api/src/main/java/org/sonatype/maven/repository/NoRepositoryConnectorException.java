package org.sonatype.maven.repository;

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
 * @author Benjamin Bentmann
 */
public class NoRepositoryConnectorException
    extends RepositoryException
{

    private final RemoteRepository repository;

    public NoRepositoryConnectorException( RemoteRepository repository )
    {
        super( toMessage( repository ) );

        this.repository = repository;
    }

    private static String toMessage( RemoteRepository repository )
    {
        if ( repository != null )
        {
            return "No connector available to access repository '" + repository.getId() + "' (" + repository.getUrl()
                + ") of type '" + repository.getContentType() + "'";
        }
        else
        {
            return "No connector available to access repository";
        }
    }

    public RemoteRepository getRepository()
    {
        return repository;
    }

}
