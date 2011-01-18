package org.sonatype.aether.connector.async;

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

/**
 * Simple exception when a resource doesn't exist.
 *
 * @author Jeanfrancois Arcand
 */
class ResourceDoesNotExistException
    extends Exception
{

    public ResourceDoesNotExistException( final String message )
    {
        super( message );
    }

    public ResourceDoesNotExistException( final String message, final Throwable cause )
    {
        super( message, cause );
    }
}
