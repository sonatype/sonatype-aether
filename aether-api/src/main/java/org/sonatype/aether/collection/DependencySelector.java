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

import org.sonatype.aether.graph.Dependency;

/**
 * Decides what dependencies to include in the dependency graph.
 * 
 * @author Benjamin Bentmann
 */
public interface DependencySelector
{

    /**
     * Applies exclusions to the specified dependency.
     * 
     * @param dependency The dependency to filter, must not be {@code null}.
     * @return {@code false} if the dependency should be excluded from the children of the current node, {@code true}
     *         otherwise.
     */
    boolean selectDependency( Dependency dependency );

    /**
     * Derives a dependency selector for the specified collection context. When calculating the child selector,
     * implementors are strongly advised to simply return the current instance if nothing changed to help save memory.
     * 
     * @param context The dependency collection context, must not be {@code null}.
     * @return The dependency filter for the target node, must not be {@code null}.
     */
    DependencySelector deriveChildSelector( DependencyCollectionContext context );

}
