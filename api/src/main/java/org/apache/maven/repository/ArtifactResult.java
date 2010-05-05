package org.apache.maven.repository;

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

import java.util.ArrayList;
import java.util.List;

/**
 * @author Benjamin Bentmann
 */
public class ArtifactResult
{

    private final ArtifactRequest request;

    private final List<Exception> exceptions;

    private ArtifactRepository repository;

    public ArtifactResult( ArtifactRequest request )
    {
        if ( request == null )
        {
            throw new IllegalArgumentException( "resolution request has not been specified" );
        }
        this.request = request;
        this.exceptions = new ArrayList<Exception>( 4 );
    }

    public ArtifactRequest getRequest()
    {
        return request;
    }

    public List<? extends Exception> getExceptions()
    {
        return exceptions;
    }

    public ArtifactResult addException( Exception exception )
    {
        if ( exception != null )
        {
            this.exceptions.add( exception );
        }
        return this;
    }

    public ArtifactRepository getRepository()
    {
        return repository;
    }

    public ArtifactResult setRepository( ArtifactRepository repository )
    {
        this.repository = repository;
        return this;
    }

}
