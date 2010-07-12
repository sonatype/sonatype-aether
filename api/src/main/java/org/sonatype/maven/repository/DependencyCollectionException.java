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
public class DependencyCollectionException
    extends RepositoryException
{

    private final CollectResult result;

    public DependencyCollectionException( CollectResult result )
    {
        super( "Failed to collect dependencies for " + getSource( result ), getCause( result ) );
        this.result = result;
    }

    public CollectResult getResult()
    {
        return result;
    }

    private static String getSource( CollectResult result )
    {
        if ( result == null )
        {
            return "";
        }

        CollectRequest request = result.getRequest();
        if ( request.getRoot() != null )
        {
            return request.getRoot().toString();
        }

        return request.getDependencies().toString();
    }

    private static Throwable getCause( CollectResult result )
    {
        Throwable cause = null;
        if ( result != null && !result.getExceptions().isEmpty() )
        {
            cause = result.getExceptions().get( 0 );
        }
        return cause;
    }

}
