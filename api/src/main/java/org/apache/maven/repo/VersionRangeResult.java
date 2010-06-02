package org.apache.maven.repo;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Benjamin Bentmann
 */
public class VersionRangeResult
{

    private final VersionRangeRequest request;

    private final List<Exception> exceptions;

    private List<String> versions;

    private final Map<String, ArtifactRepository> repositories;

    private boolean range;

    public VersionRangeResult( VersionRangeRequest request )
    {
        if ( request == null )
        {
            throw new IllegalArgumentException( "version range request has not been specified" );
        }
        this.request = request;
        this.exceptions = new ArrayList<Exception>( 4 );
        versions = new ArrayList<String>();
        repositories = new HashMap<String, ArtifactRepository>();
    }

    public VersionRangeRequest getRequest()
    {
        return request;
    }

    public List<? extends Exception> getExceptions()
    {
        return exceptions;
    }

    public VersionRangeResult addException( Exception exception )
    {
        if ( exception != null )
        {
            this.exceptions.add( exception );
        }
        return this;
    }

    public List<String> getVersions()
    {
        return versions;
    }

    public VersionRangeResult addVersion( String version )
    {
        versions.add( version );
        return this;
    }

    public VersionRangeResult setVersions( List<String> versions )
    {
        this.versions = ( versions != null ) ? versions : new ArrayList<String>();
        return this;
    }

    public ArtifactRepository getRepository( String version )
    {
        return repositories.get( version );
    }

    public VersionRangeResult setRepository( String version, ArtifactRepository repository )
    {
        if ( repository != null )
        {
            this.repositories.put( version, repository );
        }
        return this;
    }

    public boolean isRange()
    {
        return range;
    }

    public VersionRangeResult setRange( boolean range )
    {
        this.range = range;
        return this;
    }

}
