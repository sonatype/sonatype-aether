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

    private List<Version> versions;

    private final Map<Version, ArtifactRepository> repositories;

    private VersionConstraint versionConstraint;

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
        versions = new ArrayList<Version>();
        repositories = new HashMap<Version, ArtifactRepository>();
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
    public List<Version> getVersions()
    {
        return versions;
    }

    /**
     * Adds the specified version to the result.
     * 
     * @param version The version to add, must not be {@code null}.
     * @return This result for chaining, never {@code null}.
     */
    public VersionRangeResult addVersion( Version version )
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
    public VersionRangeResult setVersions( List<Version> versions )
    {
        this.versions = ( versions != null ) ? versions : new ArrayList<Version>();
        return this;
    }

    /**
     * Gets the repository from which the specified version was resolved.
     * 
     * @param version The version whose source repository should be retrieved, must not be {@code null}.
     * @return The repository from which the version was resolved or {@code null} if unknown.
     */
    public ArtifactRepository getRepository( Version version )
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
    public VersionRangeResult setRepository( Version version, ArtifactRepository repository )
    {
        if ( repository != null )
        {
            this.repositories.put( version, repository );
        }
        return this;
    }

    /**
     * Gets the version constraint that was parsed from the artifact's version string.
     * 
     * @return The parsed version constraint or {@code null}.
     */
    public VersionConstraint getVersionConstraint()
    {
        return versionConstraint;
    }

    /**
     * Sets the version constraint that was parsed from the artifact's version string.
     * 
     * @param versionConstraint The parsed version constraint, may be {@code null}.
     * @return This result for chaining, never {@code null}.
     */
    public VersionRangeResult setVersionConstraint( VersionConstraint versionConstraint )
    {
        this.versionConstraint = versionConstraint;
        return this;
    }

}
