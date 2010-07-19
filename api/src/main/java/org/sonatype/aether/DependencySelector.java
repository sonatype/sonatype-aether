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
 * Decides what dependencies to include in the dependency graph. The dependency tree builder will maintain one
 * dependency selector for each node of the dependency tree.
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
     * Derives a dependency selector for the specified node. When calculating the child selector, implementors are
     * strongly advised to simply return the current instance if nothing changed to help save memory.
     * 
     * @param node The node to derive a filter for, must not be {@code null}.
     * @return The dependency filter for the node, must not be {@code null}.
     */
    DependencySelector deriveChildSelector( DependencyNode node );

}
