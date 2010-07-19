package org.sonatype.aether;

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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

/**
 * A dependency to some artifact. <em>Note:</em> Instances of this class are immutable and the exposed mutators return
 * new objects rather than changing the current instance.
 * 
 * @author Benjamin Bentmann
 */
public final class Dependency
{

    private static final Exclusion[] NO_EXCLUSIONS = new Exclusion[0];

    private final Artifact artifact;

    private final String scope;

    private final boolean optional;

    private final Exclusion[] exclusions;

    /**
     * Creates a mandatory dependency on the specified artifact with the given scope.
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
        this( artifact, scope, optional, NO_EXCLUSIONS );
    }

    /**
     * Creates a dependency on the specified artifact with the given scope and exclusions.
     * 
     * @param artifact The artifact being depended on, may be {@code null}.
     * @param scope The scope of the dependency, may be {@code null}.
     * @param optional A flag whether the dependency is optional or mandatory.
     * @param exclusions The exclusions that apply to transitive dependencies, may be {@code null} if none.
     */
    public Dependency( Artifact artifact, String scope, boolean optional, Collection<Exclusion> exclusions )
    {
        this( artifact, scope, optional, toArray( exclusions ) );
    }

    private static Exclusion[] toArray( Collection<Exclusion> exclusions )
    {
        if ( exclusions != null && !exclusions.isEmpty() )
        {
            return exclusions.toArray( new Exclusion[exclusions.size()] );
        }
        return NO_EXCLUSIONS;
    }

    private Dependency( Artifact artifact, String scope, boolean optional, Exclusion[] exclusions )
    {
        if ( artifact == null )
        {
            throw new IllegalArgumentException( "no artifact specified for dependency" );
        }
        this.artifact = artifact;
        this.scope = ( scope != null ) ? scope : "";
        this.optional = optional;
        this.exclusions = exclusions;
    }

    /**
     * Gets the artifact being depended on.
     * 
     * @return The artifact, never {@code null}.
     */
    public Artifact getArtifact()
    {
        return artifact;
    }

    /**
     * Sets the artifact being depended on.
     * 
     * @param artifact The artifact, must not be {@code null}.
     * @return The new dependency, never {@code null}.
     */
    public Dependency setArtifact( Artifact artifact )
    {
        if ( this.artifact.equals( artifact ) )
        {
            return this;
        }
        return new Dependency( artifact, scope, optional, exclusions );
    }

    /**
     * Gets the scope of the dependency. The scope defines in which context this dependency is relevant.
     * 
     * @return The scope or an empty string if not set, never {@code null}.
     */
    public String getScope()
    {
        return scope;
    }

    /**
     * Sets the scope of the dependency, e.g. "compile".
     * 
     * @param scope The scope of the dependency, may be {@code null}.
     * @return The new dependency, never {@code null}.
     */
    public Dependency setScope( String scope )
    {
        if ( this.scope.equals( scope ) || ( scope == null && this.scope.length() <= 0 ) )
        {
            return this;
        }
        return new Dependency( artifact, scope, optional, exclusions );
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
     * Sets the optional flag for the dependency.
     * 
     * @param optional {@code true} if the dependency is optional, {@code false} if the dependency is mandatory.
     * @return The new dependency, never {@code null}.
     */
    public Dependency setOptional( boolean optional )
    {
        if ( this.optional == optional )
        {
            return this;
        }
        return new Dependency( artifact, scope, optional, exclusions );
    }

    /**
     * Gets the exclusions for this dependency. Exclusions can be used to remove transitive dependencies during
     * resolution.
     * 
     * @return The (read-only) exclusions, never {@code null}.
     */
    public Collection<Exclusion> getExclusions()
    {
        return Collections.unmodifiableCollection( Arrays.asList( exclusions ) );
    }

    /**
     * Sets the exclusions for the dependency.
     * 
     * @param exclusions The exclusions, may be {@code null}.
     * @return The new dependency, never {@code null}.
     */
    public Dependency setExclusions( Collection<Exclusion> exclusions )
    {
        if ( getExclusions().equals( exclusions ) || ( exclusions == null && this.exclusions.length <= 0 ) )
        {
            return this;
        }
        return new Dependency( artifact, scope, optional, exclusions );
    }

    @Override
    public String toString()
    {
        return String.valueOf( getArtifact() ) + " (" + getScope() + ( isOptional() ? "?" : "" ) + ")";
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( obj == this )
        {
            return true;
        }
        else if ( obj == null || !getClass().equals( obj.getClass() ) )
        {
            return false;
        }

        Dependency that = (Dependency) obj;

        return artifact.equals( that.artifact )
            && scope.equals( that.scope )
            && optional == that.optional
            && new HashSet<Object>( Arrays.asList( exclusions ) ).equals( new HashSet<Object>(
                                                                                               Arrays.asList( that.exclusions ) ) );
    }

    @Override
    public int hashCode()
    {
        int hash = 17;
        hash = hash * 31 + artifact.hashCode();
        hash = hash * 31 + scope.hashCode();
        hash = hash * 31 + ( optional ? 1 : 0 );
        hash = hash * 31 + exclusions.length;
        return hash;
    }

}
