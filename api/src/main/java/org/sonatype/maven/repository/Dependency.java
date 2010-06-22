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
import java.util.List;

/**
 * A dependency to some artifact.
 * 
 * @author Benjamin Bentmann
 */
public class Dependency
{

    private Artifact artifact;

    private String scope = "";

    private boolean optional;

    private List<Exclusion> exclusions = new ArrayList<Exclusion>( 0 );

    /**
     * Creates an uninitialized dependency.
     */
    public Dependency()
    {
        // enables default constructor
    }

    /**
     * Creates a dependency on the specified artifact with the given scope.
     * 
     * @param artifact The artifact being depended on, may be {@code null}.
     * @param scope The scope of the dependency, may be {@code null}.
     */
    public Dependency( Artifact artifact, String scope )
    {
        this( artifact, scope, false );
    }

    /**
     * Creates a dependency on the specified artifact with the given scope.
     * 
     * @param artifact The artifact being depended on, may be {@code null}.
     * @param scope The scope of the dependency, may be {@code null}.
     * @param optional A flag whether the dependency is optional or mandatory.
     */
    public Dependency( Artifact artifact, String scope, boolean optional )
    {
        setArtifact( artifact );
        setScope( scope );
        setOptional( optional );
    }

    /**
     * Gets the artifact being depended on.
     * 
     * @return The artifact or {@code null} if not set.
     */
    public Artifact getArtifact()
    {
        return artifact;
    }

    /**
     * Sets the artifact being depended on.
     * 
     * @param artifact The artifact, may be {@code null}.
     * @return This dependency for chaining, never {@code null}.
     */
    public Dependency setArtifact( Artifact artifact )
    {
        this.artifact = artifact;
        return this;
    }

    /**
     * Gets the scope of this dependency. The scope defines in which context this dependency is relevant.
     * 
     * @return The scope or an empty string if not set, never {@code null}.
     */
    public String getScope()
    {
        return scope;
    }

    /**
     * Sets the scope of this dependency, e.g. "compile".
     * 
     * @param scope The scope of this dependency, may be {@code null}.
     * @return This dependency for chaining, never {@code null}.
     */
    public Dependency setScope( String scope )
    {
        this.scope = ( scope != null ) ? scope.intern() : "";
        return this;
    }

    /**
     * Indicates whether this dependency is optional or not. Optional dependencies can usually be igored during
     * transitive dependency resolution.
     * 
     * @return {@code true} if the dependency is optional, {@code false} otherwise.
     */
    public boolean isOptional()
    {
        return optional;
    }

    /**
     * Sets the optional flag for this dependency.
     * 
     * @param optional {@code true} if the dependency is optional, {@code false} if the dependency is mandatory.
     * @return This dependency for chaining, never {@code null}.
     */
    public Dependency setOptional( boolean optional )
    {
        this.optional = optional;
        return this;
    }

    /**
     * Gets the exclusions for this dependency. Exclusions can be used to remove transitive dependencies during
     * resolution.
     * 
     * @return The exclusions, never {@code null}.
     */
    public List<Exclusion> getExclusions()
    {
        return exclusions;
    }

    /**
     * Sets the exclusions for this dependency.
     * 
     * @param exclusions The exclusions, may be {@code null}.
     * @return This dependency for chaining, never {@code null}.
     */
    public Dependency setExclusions( List<Exclusion> exclusions )
    {
        this.exclusions = ( exclusions != null ) ? exclusions : new ArrayList<Exclusion>( 4 );
        return this;
    }

    /**
     * Adds the specified exclusion to this dependency.
     * 
     * @param exclusion The exclusion to add, may be {@code null}.
     * @return This dependency for chaining, never {@code null}.
     */
    public Dependency addExclusion( Exclusion exclusion )
    {
        if ( exclusion != null )
        {
            this.exclusions.add( exclusion );
        }
        return this;
    }

    @Override
    public String toString()
    {
        return String.valueOf( getArtifact() ) + " (" + getScope() + ( isOptional() ? "?" : "" ) + ")";
    }

}
