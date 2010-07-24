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

import java.util.Collection;
import java.util.HashSet;

/**
 * A constraint on versions for a dependency.
 * 
 * @author Benjamin Bentmann
 */
public class VersionConstraint
{

    private Collection<VersionRange> ranges = new HashSet<VersionRange>();

    private Version preferredVersion;

    /**
     * Adds the specified version range to this constraint. All versions matched by the given range satisfy this
     * constraint.
     * 
     * @param range The version range to add, may be {@code null}.
     * @return This constraint for chaining, never {@code null}.
     */
    public VersionConstraint addRange( VersionRange range )
    {
        if ( range != null )
        {
            ranges.add( range );
        }
        return this;
    }

    /**
     * Gets the version ranges of this constraint.
     * 
     * @return The version ranges, may be empty but never {@code null}.
     */
    public Collection<VersionRange> getRanges()
    {
        return ranges;
    }

    /**
     * Sets the preferred version to satisfy this constraint.
     * 
     * @param preferredVersion The preferred version for this constraint, may be {@code null} if none.
     * @return This constraint for chaining, never {@code null}.
     */
    public VersionConstraint setPreferredVersion( Version preferredVersion )
    {
        this.preferredVersion = preferredVersion;
        return this;
    }

    /**
     * Gets the preferred version to satisfiy this constraint.
     * 
     * @return The preferred version for this constraint or {@code null} if unknown.
     */
    public Version getPreferredVersion()
    {
        return preferredVersion;
    }

    /**
     * Determines whether the specified version satisfies this constraint. In more detail, a version satisfies this
     * constraint if it matches at least one version range or if this constraint has no version ranges at all.
     * 
     * @param version The version to test, must not be {@code null}.
     * @return {@code true} if the specified version satisfies this constraint, {@code false} otherwise.
     */
    public boolean containsVersion( Version version )
    {
        for ( VersionRange range : ranges )
        {
            if ( range.containsVersion( version ) )
            {
                return true;
            }
        }
        return ranges.isEmpty();
    }

    @Override
    public String toString()
    {
        StringBuilder buffer = new StringBuilder( 128 );

        for ( VersionRange range : getRanges() )
        {
            if ( buffer.length() > 0 )
            {
                buffer.append( "," );
            }
            buffer.append( range );
        }

        if ( buffer.length() <= 0 )
        {
            buffer.append( getPreferredVersion() );
        }

        return buffer.toString();
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj )
        {
            return true;
        }
        if ( obj == null || !getClass().equals( obj.getClass() ) )
        {
            return false;
        }

        VersionConstraint that = (VersionConstraint) obj;

        return ranges.equals( that.getRanges() ) && eq( preferredVersion, that.getPreferredVersion() );
    }

    private static <T> boolean eq( T s1, T s2 )
    {
        return s1 != null ? s1.equals( s2 ) : s2 == null;
    }

    @Override
    public int hashCode()
    {
        int hash = 17;
        hash = hash * 31 + hash( getRanges() );
        hash = hash * 31 + hash( getPreferredVersion() );
        return hash;
    }

    private static int hash( Object obj )
    {
        return obj != null ? obj.hashCode() : 0;
    }

}
