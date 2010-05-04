package org.apache.maven.repository;

import java.util.ArrayList;
import java.util.List;

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
 * @author Benjamin Bentmann
 */
public class VersionResult
{

    private final VersionRequest request;

    private final List<RepositoryException> exceptions;

    private String version;

    private ArtifactRepository repository;

    public VersionResult( VersionRequest request )
    {
        if ( request == null )
        {
            throw new IllegalArgumentException( "version request has not been specified" );
        }
        this.request = request;
        this.exceptions = new ArrayList<RepositoryException>( 4 );
    }

    public VersionResult( VersionRequest request, String version, ArtifactRepository repository )
    {
        this( request );
        setVersion( version );
        setRepository( repository );
    }

    public VersionRequest getRequest()
    {
        return request;
    }

    public List<? extends RepositoryException> getExceptions()
    {
        return exceptions;
    }

    public VersionResult addException( RepositoryException exception )
    {
        if ( exception != null )
        {
            this.exceptions.add( exception );
        }
        return this;
    }

    public String getVersion()
    {
        return version;
    }

    public VersionResult setVersion( String version )
    {
        this.version = version;
        return this;
    }

    public ArtifactRepository getRepository()
    {
        return repository;
    }

    public VersionResult setRepository( ArtifactRepository repository )
    {
        this.repository = repository;
        return this;
    }

}
