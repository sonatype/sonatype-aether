package org.sonatype.aether.spi.log;

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

/**
 * A simple logger to facilitate emission of debug messages.
 * 
 * @author Benjamin Bentmann
 */
public interface Logger
{

    /**
     * Indicates whether debug logging is enabled.
     * 
     * @return {@code true} if debug logging is enabled, {@code false} otherwise.
     */
    boolean isDebugEnabled();

    /**
     * Emits the specified message.
     * 
     * @param msg The message to log, must not be {@code null}.
     */
    void debug( String msg );

    /**
     * Emits the specified message along with a stack trace of the given exception.
     * 
     * @param msg The message to log, must not be {@code null}.
     * @param error The exception to log, may be {@code null}.
     */
    void debug( String msg, Throwable error );

}
