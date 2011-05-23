package org.sonatype.aether.spi.log;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

/**
 * A logger that disables any logging.
 * 
 * @author Benjamin Bentmann
 */
public class NullLogger
    implements Logger
{

    public static final Logger INSTANCE = new NullLogger();

    public boolean isDebugEnabled()
    {
        return false;
    }

    public void debug( String msg )
    {
    }

    public void debug( String msg, Throwable error )
    {
    }

    public boolean isWarnEnabled()
    {
        return false;
    }

    public void warn( String msg )
    {
    }

    public void warn( String msg, Throwable error )
    {
    }

}
