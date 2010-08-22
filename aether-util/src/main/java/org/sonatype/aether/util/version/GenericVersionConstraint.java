package org.sonatype.aether.util.version;

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

import org.sonatype.aether.Version;
import org.sonatype.aether.VersionConstraint;
import org.sonatype.aether.VersionRange;

/**
 * A constraint on versions for a dependency.
 * 
 * @author Benjamin Bentmann
 */
final class GenericVersionConstraint
    implements VersionConstraint
{

    private Collection<VersionRange> ranges = new HashSet<VersionRange>();

    private Version version;

    /**
     * Adds the specified version range to this constraint. All versions matched by the given range satisfy this
     * constraint.
     * 
     * @param range The version range to add, may be {@code null}.
     * @return This constraint for chaining, never {@code null}.
     */
    public GenericVersionConstraint addRange( VersionRange range )
    {
        if ( range != null )
        {
            ranges.add( range );
        }
        return this;
    }

    public Collection<VersionRange> getRanges()
    {
        return ranges;
    }

    /**
     * Sets the recommended version to satisfy this constraint.
     * 
     * @param version The recommended version for this constraint, may be {@code null} if none.
     * @return This constraint for chaining, never {@code null}.
     */
    public GenericVersionConstraint setVersion( Version version )
    {
        this.version = version;
        return this;
    }

    public Version getVersion()
    {
        return version;
    }

    public boolean containsVersion( Version version )
    {
        if ( ranges.isEmpty() )
        {
            return version.equals( this.version );
        }
        else
        {
            for ( VersionRange range : ranges )
            {
                if ( range.containsVersion( version ) )
                {
                    return true;
                }
            }
            return false;
        }
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
            buffer.append( getVersion() );
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

        GenericVersionConstraint that = (GenericVersionConstraint) obj;

        return ranges.equals( that.getRanges() ) && eq( version, that.getVersion() );
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
        hash = hash * 31 + hash( getVersion() );
        return hash;
    }

    private static int hash( Object obj )
    {
        return obj != null ? obj.hashCode() : 0;
    }

}
