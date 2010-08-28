package org.sonatype.aether.impl.internal;

/*
 * Copyright (c) 2010 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0, 
 * and you may not use this file except in compliance with the Apache License Version 2.0. 
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, 
 * software distributed under the Apache License Version 2.0 is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */

import org.sonatype.aether.spi.log.Logger;

/**
 * A logger that delegates to Slf4J logging.
 * 
 * @author Benjamin Bentmann
 */
public class Slf4jLogger
    implements Logger
{

    private org.slf4j.Logger logger;

    public Slf4jLogger()
    {
        // enables default constructor
    }

    public Slf4jLogger( org.slf4j.Logger logger )
    {
        setLogger( logger );
    }

    public void setLogger( org.slf4j.Logger logger )
    {
        this.logger = logger;
    }

    public boolean isDebugEnabled()
    {
        return logger.isDebugEnabled();
    }

    public void debug( String msg )
    {
        logger.debug( msg );
    }

    public void debug( String msg, Throwable error )
    {
        logger.debug( msg, error );
    }

}
