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

import org.sonatype.aether.InvalidVersionException;
import org.sonatype.aether.InvalidVersionRangeException;
import org.sonatype.aether.VersionScheme;

/**
 * Maven specific {@link VersionScheme}.
 * 
 * @author Benjamin Bentmann
 * @author Alin Dreghiciu
 */
public class MavenVersionScheme
    implements VersionScheme
{

    public MavenVersion parseVersion( final String version )
        throws InvalidVersionException
    {
        return new MavenVersion( version );
    }

    public MavenVersionRange parseVersionRange( final String range )
        throws InvalidVersionRangeException
    {
        return new MavenVersionRange( range );
    }

    @Override
    public boolean equals( final Object obj )
    {
        if ( this == obj )
        {
            return true;
        }

        return obj != null && getClass().equals( obj.getClass() );
    }

    @Override
    public int hashCode()
    {
        return getClass().hashCode();
    }

}
