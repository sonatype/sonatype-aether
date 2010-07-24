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
 * @author Benjamin Bentmann
 */
public class VersionRangeResolutionException
    extends RepositoryException
{

    private final VersionRangeResult result;

    public VersionRangeResolutionException( VersionRangeResult result )
    {
        super( "Failed to resolve version range" + ( result != null ? " for " + result.getRequest().getArtifact() : "" ) );
        this.result = result;
    }

    public VersionRangeResolutionException( VersionRangeResult result, String message )
    {
        super( message );
        this.result = result;
    }

    public VersionRangeResult getResult()
    {
        return result;
    }

}
