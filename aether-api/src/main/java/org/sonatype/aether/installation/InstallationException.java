package org.sonatype.aether.installation;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import org.sonatype.aether.RepositoryException;

/**
 * Thrown in case of an installation error like an IO error.
 * 
 * @author Benjamin Bentmann
 */
public class InstallationException
    extends RepositoryException
{

    public InstallationException( String message )
    {
        super( message );
    }

    public InstallationException( String message, Throwable cause )
    {
        super( message, cause );
    }

}
