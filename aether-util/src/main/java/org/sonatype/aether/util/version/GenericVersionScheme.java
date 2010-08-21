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

import org.sonatype.aether.InvalidVersionSpecificationException;
import org.sonatype.aether.VersionScheme;

/**
 * A version scheme using {@link GenericVersion}.
 * 
 * @author Benjamin Bentmann
 * @author Alin Dreghiciu
 */
public class GenericVersionScheme
    implements VersionScheme
{

    public GenericVersion parseVersion( final String version )
        throws InvalidVersionSpecificationException
    {
        return new GenericVersion( version );
    }

    public GenericVersionRange parseVersionRange( final String range )
        throws InvalidVersionSpecificationException
    {
        return new GenericVersionRange( range );
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
