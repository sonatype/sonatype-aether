package org.sonatype.aether.deployment;

/*******************************************************************************
 * Copyright (c) 2010 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import org.sonatype.aether.RepositoryException;

/**
 * @author Benjamin Bentmann
 */
public class DeploymentException
    extends RepositoryException
{

    public DeploymentException( String message )
    {
        super( message );
    }

    public DeploymentException( String message, Throwable cause )
    {
        super( message, cause );
    }

}
