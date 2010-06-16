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

import org.sonatype.maven.repository.Version;
import org.sonatype.maven.repository.VersionRange;

/**
 * @author Benjamin Bentmann
 */
class MavenVersionRange
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
