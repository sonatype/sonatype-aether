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
public class DependencyResolutionException
    extends RepositoryException
{

    private final DependencyResult result;

    public DependencyResolutionException( DependencyResult result, Throwable cause )
    {
        super( getMessage( cause ), cause );
        this.result = result;
    }

    private static String getMessage( Throwable cause )
    {
        String msg = null;
        if ( cause != null )
        {
            msg = cause.getMessage();
        }
        if ( msg == null || msg.length() <= 0 )
        {
            msg = "Could not resolve transitive dependencies";
        }
        return msg;
    }

    public DependencyResult getResult()
    {
        return result;
    }

}
