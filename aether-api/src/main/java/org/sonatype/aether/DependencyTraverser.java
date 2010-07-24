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
 * Decides whether the dependencies of a dependency node should be traversed as well. The dependency tree builder will
 * maintain one dependency traverser for each node of the dependency tree.
 * 
 * @author Benjamin Bentmann
 */
public interface DependencyTraverser
{

    /**
     * Decides whether the transitive dependencies of the specified dependency should be traversed.
     * 
     * @param dependency The dependency to check, must not be {@code null}.
     * @return {@code true} if the dependency tree builder should recurse into the specified dependency and process its
     *         dependencies, {@code false} otherwise.
     */
    boolean traverseDependency( Dependency dependency );

    /**
     * Derives a dependency traverser that will be used to decide whether the transitive dependencies of the specified
     * node's dependencies shall be processed. When calculating the child traverser, implementors are strongly advised
     * to simply return the current instance if nothing changed to help save memory.
     * 
     * @param node The node to derive a traverser for, must not be {@code null}.
     * @return The dependency traverser for the node, must not be {@code null}.
     */
    DependencyTraverser deriveChildTraverser( DependencyNode node );

}
