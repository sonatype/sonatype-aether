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


/**
 * Thrown in case that a version range could not be parsed.
 * 
 * @author Alin Dreghiciu
 */
public class InvalidVersionRangeException
    extends RepositoryException
{

    private final String range;

    public InvalidVersionRangeException( String range, String message )
    {
        super( message );
        this.range = range;
    }

    public InvalidVersionRangeException( String range, Throwable cause )
    {
        super( "Could not parse range " + range + getMessage( ": ", cause ), cause );
        this.range = range;
    }

    public String getRange()
    {
        return range;
    }

}
