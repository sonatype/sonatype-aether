package org.sonatype.maven.repository;

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
 * The result of a version range resolution request.
 * 
 * @author Benjamin Bentmann
 * @see RepositorySystem#resolveVersionRange(RepositorySession, VersionRangeRequest)
 */
public class VersionRangeResult
{

    private final VersionRangeRequest request;

    private final List<Exception> exceptions;

    private List<String> versions;

    private final Map<String, ArtifactRepository> repositories;

    private boolean range;

    /**
     * Creates a new result for the specified request.
     * 
     * @param request The resolution request, must not be {@code null}.
     */
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

    /**
     * Gets the resolution request that was made.
     * 
     * @return The resolution request, never {@code null}.
     */
    public VersionRangeRequest getRequest()
    {
        return request;
    }

    /**
     * Gets the exceptions that occurred while resolving the version range.
     * 
     * @return The exceptions that occurred, never {@code null}.
     */
    public List<Exception> getExceptions()
    {
        return exceptions;
    }

    /**
     * Records the specified exception while resolving the version range.
     * 
     * @param exception The exception to record, may be {@code null}.
     * @return This result for chaining, never {@code null}.
     */
    public VersionRangeResult addException( Exception exception )
    {
        if ( exception != null )
        {
            this.exceptions.add( exception );
        }
        return this;
    }

    /**
     * Gets the versions (in ascending order) that matched the requested range.
     * 
     * @return The matching versions (if any), never {@code null}.
     */
    public List<String> getVersions()
    {
        return versions;
    }

    /**
     * Adds the specified version to the result.
     * 
     * @param version The version to add, must not be {@code null}.
     * @return This result for chaining, never {@code null}.
     */
    public VersionRangeResult addVersion( String version )
    {
        versions.add( version );
        return this;
    }

    /**
     * Sets the versions matching the requested range.
     * 
     * @param versions The matching versions, may be empty or {@code null} if none.
     * @return This result for chaining, never {@code null}.
     */
    public VersionRangeResult setVersions( List<String> versions )
    {
        this.versions = ( versions != null ) ? versions : new ArrayList<String>();
        return this;
    }

    /**
     * Gets the repository from which the specified version was resolved.
     * 
     * @param version The version whose source repository should be retrieved, must not be {@code null}.
     * @return The repository from which the version was resolved or {@code null} if unknown.
     */
    public ArtifactRepository getRepository( String version )
    {
        return repositories.get( version );
    }

    /**
     * Records the repository from which the specified version was resolved
     * 
     * @param version The version whose source repository is to be recorded, must not be {@code null}.
     * @param repository The repository from which the version was resolved, may be {@code null}.
     * @return This result for chaining, never {@code null}.
     */
    public VersionRangeResult setRepository( String version, ArtifactRepository repository )
    {
        if ( repository != null )
        {
            this.repositories.put( version, repository );
        }
        return this;
    }

    /**
     * Indicates whether the original request actually referred to a version range or just a simple version.
     * 
     * @return {@code true} if the request referred to a version range, {@code false} if the request referred only to a
     *         simple version.
     */
    public boolean isRange()
    {
        return range;
    }

    /**
     * Sets the version range indicator.
     * 
     * @param range {@code true} if the request referred to a version range, {@code false} if the request referred only
     *            to a simple version.
     * @return This result for chaining, never {@code null}.
     */
    public VersionRangeResult setRange( boolean range )
    {
        this.range = range;
        return this;
    }

}
