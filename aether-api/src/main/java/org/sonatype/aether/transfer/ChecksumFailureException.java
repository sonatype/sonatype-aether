package org.sonatype.aether.transfer;

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
public class ChecksumFailureException
    extends RepositoryException
{

    private final String expected;

    private final String actual;

    public ChecksumFailureException( String expected, String actual )
    {
        super( "Checksum validation failed, expected " + expected + " but is " + actual );

        this.expected = expected;
        this.actual = actual;
    }

    public ChecksumFailureException( String message )
    {
        super( message );
        expected = actual = "";
    }

    public ChecksumFailureException( Throwable cause )
    {
        super( "Checksum validation failed, could not read expected checksum" + getMessage( ": ", cause ), cause );
        expected = actual = "";
    }

    public ChecksumFailureException( String message, Throwable cause )
    {
        super( message, cause );
        expected = actual = "";
    }

    public String getExpected()
    {
        return expected;
    }

    public String getActual()
    {
        return actual;
    }

}
