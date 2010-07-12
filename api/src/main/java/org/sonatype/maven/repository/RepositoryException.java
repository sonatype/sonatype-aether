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
public class RepositoryException
    extends Exception
{

    public RepositoryException( String message )
    {
        super( message );
    }

    public RepositoryException( String message, Throwable cause )
    {
        super( message, cause );
    }

    public static String getMessage( String prefix, Throwable cause )
    {
        String msg = "";
        if ( cause != null )
        {
            msg = cause.getMessage();
            if ( msg == null || msg.length() <= 0 )
            {
                msg = cause.getClass().getSimpleName();
            }
            msg = prefix + msg;
        }
        return msg;
    }

}
