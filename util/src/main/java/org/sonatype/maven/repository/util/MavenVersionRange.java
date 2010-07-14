package org.sonatype.maven.repository.util;

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

import org.sonatype.maven.repository.Version;
import org.sonatype.maven.repository.VersionRange;

/**
 * @author Benjamin Bentmann
 */
public final class MavenVersionRange
    implements VersionRange
{

    private final Version lowerBound;

    private final boolean lowerBoundInclusive;

    private final Version upperBound;

    private final boolean upperBoundInclusive;

    public MavenVersionRange( Version lowerBound, boolean lowerBoundInclusive, Version upperBound,
                              boolean upperBoundInclusive )
    {
        this.lowerBound = lowerBound;
        this.lowerBoundInclusive = lowerBoundInclusive;
        this.upperBound = upperBound;
        this.upperBoundInclusive = upperBoundInclusive;
    }

    public Version getLowerBound()
    {
        return lowerBound;
    }

    public boolean isLowerBoundInclusive()
    {
        return lowerBoundInclusive;
    }

    public Version getUpperBound()
    {
        return upperBound;
    }

    public boolean isUpperBoundInclusive()
    {
        return upperBoundInclusive;
    }

    public boolean containsSnapshots()
    {
        return isSnapshot( lowerBound ) || isSnapshot( upperBound );
    }

    public boolean containsVersion( Version version )
    {
        boolean snapshot = isSnapshot( version );

        if ( lowerBound != null )
        {
            int comparison = lowerBound.compareTo( version );

            if ( snapshot && comparison == 0 )
            {
                return true;
            }

            if ( comparison == 0 && !lowerBoundInclusive )
            {
                return false;
            }
            if ( comparison > 0 )
            {
                return false;
            }
        }

        if ( upperBound != null )
        {
            int comparison = upperBound.compareTo( version );

            if ( snapshot && comparison == 0 )
            {
                return true;
            }

            if ( comparison == 0 && !upperBoundInclusive )
            {
                return false;
            }
            if ( comparison < 0 )
            {
                return false;
            }
        }

        if ( lowerBound != null || upperBound != null )
        {
            return !snapshot;
        }

        return true;
    }

    private boolean isSnapshot( Version version )
    {
        return version != null && version.toString().endsWith( "SNAPSHOT" );
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

        MavenVersionRange that = (MavenVersionRange) obj;

        return upperBoundInclusive == that.upperBoundInclusive && lowerBoundInclusive == that.lowerBoundInclusive
            && eq( upperBound, that.upperBound ) && eq( lowerBound, that.lowerBound );
    }

    private static <T> boolean eq( T s1, T s2 )
    {
        return s1 != null ? s1.equals( s2 ) : s2 == null;
    }

    @Override
    public int hashCode()
    {
        int hash = 17;
        hash = hash * 31 + hash( upperBound );
        hash = hash * 31 + ( upperBoundInclusive ? 1 : 0 );
        hash = hash * 31 + hash( lowerBound );
        hash = hash * 31 + ( lowerBoundInclusive ? 1 : 0 );
        return hash;
    }

    private static int hash( Object obj )
    {
        return obj != null ? obj.hashCode() : 0;
    }

    @Override
    public String toString()
    {
        StringBuilder buffer = new StringBuilder( 64 );
        buffer.append( lowerBoundInclusive ? '[' : '(' );
        if ( lowerBound != null )
        {
            buffer.append( lowerBound );
        }
        buffer.append( ',' );
        if ( upperBound != null )
        {
            buffer.append( upperBound );
        }
        buffer.append( upperBoundInclusive ? ']' : ')' );
        return buffer.toString();
    }

}
