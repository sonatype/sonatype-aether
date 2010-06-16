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

import java.util.Collection;
import java.util.HashSet;

/**
 * @author Benjamin Bentmann
 */
public class VersionConstraint
{

    private Collection<VersionRange> ranges = new HashSet<VersionRange>();

    private Version preferredVersion;

    public VersionConstraint addRange( VersionRange range )
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

    public VersionConstraint setPreferredVersion( Version preferredVersion )
    {
        this.preferredVersion = preferredVersion;
        return this;
    }

    public Version getPreferredVersion()
    {
        return preferredVersion;
    }

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
