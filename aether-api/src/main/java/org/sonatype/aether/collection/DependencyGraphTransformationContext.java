package org.sonatype.aether.collection;

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

import org.sonatype.aether.RepositorySystemSession;

/**
 * A context used to exchange information within a chain of dependency graph transformers.
 * 
 * @author Benjamin Bentmann
 * @see DependencyGraphTransformer
 */
public interface DependencyGraphTransformationContext
{

    /**
     * Gets the repository system session during which the graph transformation happens.
     * 
     * @return The repository system session, never {@code null}.
     */
    RepositorySystemSession getSession();

    /**
     * Gets a keyed value from the context.
     * 
     * @param key The key used to query the value, must not be {@code null}.
     * @return The queried value or {@code null} if none.
     */
    Object get( Object key );

    /**
     * Puts a keyed value into the context.
     * 
     * @param key The key used to store the value, must not be {@code null}.
     * @param value The value to store, may be {@code null}.
     * @return The previous value associated with the key or {@code null} if none.
     */
    Object put( Object key, Object value );

}
