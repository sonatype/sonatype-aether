package org.sonatype.aether.connector.wagon;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import org.sonatype.aether.transfer.TransferCancelledException;

/**
 * @author Benjamin Bentmann
 */
class WagonCancelledException
    extends RuntimeException
{

    public WagonCancelledException( TransferCancelledException cause )
    {
        super( cause );
    }

    public static Exception unwrap( Exception e )
    {
        if ( e instanceof WagonCancelledException )
        {
            e = (Exception) e.getCause();
        }
        return e;
    }

}
