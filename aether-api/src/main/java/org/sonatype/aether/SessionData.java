package org.sonatype.aether;

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
 * A container for data that is specific to a repository system session. Both components within the repository system
 * and clients of the system may use this storage to associate arbitrary data with a session. Unlike a cache, this
 * session data is not subject to purging. For this same reason, session data should also not be abused as a cache (i.e.
 * for storing values that can be re-calculated) to avoid memory exhaustion. <strong>Note:</strong> Actual implementations
 * must be thread-safe.
 * 
 * @author Benjamin Bentmann
 * @see RepositorySystemSession#getData()
 */
public interface SessionData
{

    /**
     * Associates the specified session data with the given key.
     * 
     * @param key The key under which to store the session data, must not be {@code null}.
     * @param value The data to associate with the key, may be {@code null} to remove the mapping.
     */
    void set( Object key, Object value );

    /**
     * Gets the session data associated with the specified key.
     * 
     * @param key The key for which to retrieve the session data, must not be {@code null}.
     * @return The session date associated with the key or {@code null} if none.
     */
    Object get( Object key );

}
