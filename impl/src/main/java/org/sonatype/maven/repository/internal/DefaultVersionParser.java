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

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.maven.repository.Artifact;
import org.sonatype.maven.repository.InvalidVersionException;
import org.sonatype.maven.repository.Version;
import org.sonatype.maven.repository.VersionConstraint;
import org.sonatype.maven.repository.VersionRange;
import org.sonatype.maven.repository.spi.VersionParser;

/**
 * @author Benjamin Bentmann
 */
@Component( role = VersionParser.class )
public class DefaultVersionParser
    implements VersionParser
{

    private VersionScheme getScheme( Artifact artifact )
    {
        return new MavenVersionScheme();
    }

    public Version parseVersion( Artifact artifact )
        throws InvalidVersionException
    {
        return getScheme( artifact ).parseVersion( artifact.getVersion() );
    }

    public VersionConstraint parseConstraint( Artifact artifact )
        throws InvalidVersionException
    {
        VersionConstraint constraint = new VersionConstraint();

        String version = artifact.getVersion();

        VersionScheme scheme = getScheme( artifact );

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
            constraint.addRange( range );

            process = process.substring( index + 1 ).trim();

            if ( process.length() > 0 && process.startsWith( "," ) )
            {
                process = process.substring( 1 ).trim();
            }
        }

        if ( process.length() > 0 && !constraint.getRanges().isEmpty() )
        {
            throw new InvalidVersionException( version, "Invalid version range " + version
                + ", expected [ or ( but got " + process );
        }

        if ( constraint.getRanges().isEmpty() )
        {
            constraint.setPreferredVersion( scheme.parseVersion( version ) );
        }

        return constraint;
    }

    private VersionRange parseRange( String range, VersionScheme scheme )
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

            Version version = scheme.parseVersion( process.trim() );

            versionRange = new MavenVersionRange( version, lowerBoundInclusive, version, upperBoundInclusive );
        }
        else
        {
            String lowerBound = process.substring( 0, index ).trim();
            String upperBound = process.substring( index + 1 ).trim();

            Version lowerVersion = null;
            if ( lowerBound.length() > 0 )
            {
                lowerVersion = scheme.parseVersion( lowerBound );
            }

            Version upperVersion = null;
            if ( upperBound.length() > 0 )
            {
                upperVersion = scheme.parseVersion( upperBound );
            }

            if ( upperVersion != null && lowerVersion != null && upperVersion.compareTo( lowerVersion ) < 0 )
            {
                throw new InvalidVersionException( range, "Invalid version range " + range
                    + ", lower bound must not be greater than upper bound" );
            }

            versionRange = new MavenVersionRange( lowerVersion, lowerBoundInclusive, upperVersion, upperBoundInclusive );
        }

        return versionRange;
    }

}
