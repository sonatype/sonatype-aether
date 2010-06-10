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

import java.util.Collections;
import java.util.List;

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
            Artifact artifact = result.getRequest().getArtifact();
            if ( artifact.getFile() == null )
            {
                buffer.append( sep );
                buffer.append( artifact );
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
