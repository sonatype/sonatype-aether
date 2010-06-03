package org.sonatype.maven.repository.internal;

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

import org.sonatype.maven.repository.InvalidVersionException;

/**
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @author Benjamin Bentmann
 */
class VersionRange
{

    private final Comparable<Object> lowerBound;

    private final boolean lowerBoundInclusive;

    private final Comparable<Object> upperBound;

    private final boolean upperBoundInclusive;

    private VersionRange( Comparable<Object> lowerBound, boolean lowerBoundInclusive, Comparable<Object> upperBound,
                          boolean upperBoundInclusive )
    {
        this.lowerBound = lowerBound;
        this.lowerBoundInclusive = lowerBoundInclusive;
        this.upperBound = upperBound;
        this.upperBoundInclusive = upperBoundInclusive;
    }

    public boolean containsSnapshots()
    {
        return isSnapshot( lowerBound ) || isSnapshot( upperBound );
    }

    public boolean containsVersion( Comparable<Object> version )
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

    private boolean isSnapshot( Comparable<Object> version )
    {
        return version != null && version.toString().endsWith( "SNAPSHOT" );
    }

    private static VersionRange parseRange( String range, VersionScheme scheme )
        throws InvalidVersionException
    {
        String process = range;

        boolean lowerBoundInclusive;
        if ( range.startsWith( "[" ) )
        {
            lowerBoundInclusive = true;
        }
        else if ( range.startsWith( "(" ) )
        {
            lowerBoundInclusive = false;
        }
        else
        {
            throw new InvalidVersionException( range, "Invalid version range " + range
                + ", a range must start with either [ or (" );
        }

        boolean upperBoundInclusive;
        if ( range.endsWith( "]" ) )
        {
            upperBoundInclusive = true;
        }
        else if ( range.endsWith( ")" ) )
        {
            upperBoundInclusive = false;
        }
        else
        {
            throw new InvalidVersionException( range, "Invalid version range " + range
                + ", a range must end with either [ or (" );
        }

        process = process.substring( 1, process.length() - 1 );

        VersionRange versionRange;

        int index = process.indexOf( "," );

        if ( index < 0 )
        {
            if ( !lowerBoundInclusive || !upperBoundInclusive )
            {
                throw new InvalidVersionException( range, "Invalid version range " + range
                    + ", single version must be surrounded by []" );
            }

            Comparable<Object> version = scheme.parseVersion( process.trim() );

            versionRange = new VersionRange( version, lowerBoundInclusive, version, upperBoundInclusive );
        }
        else
        {
            String lowerBound = process.substring( 0, index ).trim();
            String upperBound = process.substring( index + 1 ).trim();

            Comparable<Object> lowerVersion = null;
            if ( lowerBound.length() > 0 )
            {
                lowerVersion = scheme.parseVersion( lowerBound );
            }

            Comparable<Object> upperVersion = null;
            if ( upperBound.length() > 0 )
            {
                upperVersion = scheme.parseVersion( upperBound );
            }

            if ( upperVersion != null && lowerVersion != null && upperVersion.compareTo( lowerVersion ) < 0 )
            {
                throw new InvalidVersionException( range, "Invalid version range " + range
                    + ", lower bound must not be greater than upper bound" );
            }

            versionRange = new VersionRange( lowerVersion, lowerBoundInclusive, upperVersion, upperBoundInclusive );
        }

        return versionRange;
    }

    public static List<VersionRange> parseRanges( String version, VersionScheme scheme )
        throws InvalidVersionException
    {
        List<VersionRange> ranges = new ArrayList<VersionRange>();

        String process = version;

        while ( process.startsWith( "[" ) || process.startsWith( "(" ) )
        {
            int index1 = process.indexOf( ")" );
            int index2 = process.indexOf( "]" );

            int index = index2;
            if ( index2 < 0 || ( index1 >= 0 && index1 < index2 ) )
            {
                index = index1;
            }

            if ( index < 0 )
            {
                throw new InvalidVersionException( version, "Unbounded version range " + version );
            }

            VersionRange range = parseRange( process.substring( 0, index + 1 ), scheme );
            ranges.add( range );

            process = process.substring( index + 1 ).trim();

            if ( process.length() > 0 && process.startsWith( "," ) )
            {
                process = process.substring( 1 ).trim();
            }
        }

        if ( process.length() > 0 && !ranges.isEmpty() )
        {
            throw new InvalidVersionException( version, "Invalid version range " + version
                + ", expected [ or ( but got " + process );
        }

        return ranges;
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
