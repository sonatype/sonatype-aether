package org.sonatype.aether.connector.wagon;

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

import org.apache.maven.wagon.Wagon;

/**
 * A component to acquire and release wagon instances for uploads/downloads.
 * 
 * @author Benjamin Bentmann
 */
public interface WagonProvider
{

    /**
     * Acquires a wagon instance that matches the specified role hint. The role hint is derived from the URI scheme,
     * e.g. "http" or "file".
     * 
     * @param roleHint The role hint to get a wagon for, must not be {@code null}.
     * @return The requested wagon instance, never {@code null}.
     * @throws Exception If no wagon could be retrieved for the specified role hint.
     */
    Wagon lookup( String roleHint )
        throws Exception;

    /**
     * Releases the specified wagon. A wagon provider may either free any resources allocated for the wagon instance or
     * return the instance back to a pool for future use.
     * 
     * @param wagon The wagon to release, may be {@code null}.
     */
    void release( Wagon wagon );

}
