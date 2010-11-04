package org.sonatype.aether.transfer;

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
public class TransferCancelledException
    extends RepositoryException
{

    public TransferCancelledException()
    {
        super( "The operation was cancelled." );
    }

    public TransferCancelledException( String message )
    {
        super( message );
    }

    public TransferCancelledException( String message, Throwable cause )
    {
        super( message, cause );
    }

}
