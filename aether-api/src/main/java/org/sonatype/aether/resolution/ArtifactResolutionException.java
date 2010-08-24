package org.sonatype.aether.resolution;

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

import java.util.Collections;
import java.util.List;

import org.sonatype.aether.RepositoryException;
import org.sonatype.aether.transfer.ArtifactNotFoundException;

/**
 * @author Benjamin Bentmann
 */
public class ArtifactResolutionException
    extends RepositoryException
{

    private final List<ArtifactResult> results;

    public ArtifactResolutionException( List<ArtifactResult> results )
    {
        super( getMessage( results ), getCause( results ) );
        this.results = ( results != null ) ? results : Collections.<ArtifactResult> emptyList();
    }

    public List<ArtifactResult> getResults()
    {
        return results;
    }

    private static String getMessage( List<? extends ArtifactResult> results )
    {
        StringBuilder buffer = new StringBuilder( 256 );

        buffer.append( "The following artifacts could not be resolved: " );

        String sep = "";
        for ( ArtifactResult result : results )
        {
            if ( !result.isResolved() )
            {
                buffer.append( sep );
                buffer.append( result.getRequest().getArtifact() );
                sep = ", ";
            }
        }

        Throwable cause = getCause( results );
        if ( cause != null )
        {
            buffer.append( ": " ).append( cause.getMessage() );
        }

        return buffer.toString();
    }

    private static Throwable getCause( List<? extends ArtifactResult> results )
    {
        for ( ArtifactResult result : results )
        {
            if ( !result.isResolved() )
            {
                Throwable nf = null;
                for ( Throwable t : result.getExceptions() )
                {
                    if ( t instanceof ArtifactNotFoundException )
                    {
                        if ( nf == null )
                        {
                            nf = t;
                        }
                    }
                    else
                    {
                        return t;
                    }

                }
                if ( nf != null )
                {
                    return nf;
                }
            }
        }
        return null;
    }

}
