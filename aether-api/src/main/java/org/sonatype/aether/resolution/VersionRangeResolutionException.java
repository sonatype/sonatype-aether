package org.sonatype.aether.resolution;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *******************************************************************************/

import org.sonatype.aether.RepositoryException;

/**
 * @author Benjamin Bentmann
 */
public class VersionRangeResolutionException
    extends RepositoryException
{

    private final VersionRangeResult result;

    public VersionRangeResolutionException( VersionRangeResult result )
    {
        super( getMessage( result ), getCause( result ) );
        this.result = result;
    }

    private static String getMessage( VersionRangeResult result )
    {
        StringBuilder buffer = new StringBuilder( 256 );
        buffer.append( "Failed to resolve version range" );
        if ( result != null )
        {
            buffer.append( " for " ).append( result.getRequest().getArtifact() );
            if ( !result.getExceptions().isEmpty() )
            {
                buffer.append( ": " ).append( result.getExceptions().iterator().next().getMessage() );
            }
        }
        return buffer.toString();
    }

    private static Throwable getCause( VersionRangeResult result )
    {
        Throwable cause = null;
        if ( result != null && !result.getExceptions().isEmpty() )
        {
            cause = result.getExceptions().get( 0 );
        }
        return cause;
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
