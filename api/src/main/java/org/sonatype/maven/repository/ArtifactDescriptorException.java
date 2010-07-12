package org.sonatype.maven.repository;

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
public class ArtifactDescriptorException
    extends RepositoryException
{

    private final ArtifactDescriptorResult result;

    public ArtifactDescriptorException( ArtifactDescriptorResult result, String message )
    {
        super( message, getCause( result ) );
        this.result = result;
    }

    public ArtifactDescriptorException( ArtifactDescriptorResult result )
    {
        super( "Failed to read artifact descriptor"
            + ( result != null ? " for " + result.getRequest().getArtifact() : "" ), getCause( result ) );
        this.result = result;
    }

    public ArtifactDescriptorResult getResult()
    {
        return result;
    }

    private static Throwable getCause( ArtifactDescriptorResult result )
    {
        Throwable cause = null;
        if ( result != null && !result.getExceptions().isEmpty() )
        {
            cause = result.getExceptions().get( 0 );
        }
        return cause;
    }

}
