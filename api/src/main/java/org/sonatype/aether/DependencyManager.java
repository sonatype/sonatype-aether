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

import java.util.List;

/**
 * Applies dependency management to the dependencies of a dependency node. The dependency tree builder will maintain one
 * dependency manager for each node of the dependency tree.
 * 
 * @author Benjamin Bentmann
 */
public interface DependencyManager
{

    /**
     * Applies dependency management to the specified dependency.
     * 
     * @param dependency The dependency to manage, must not be {@code null}.
     * @return The management update to apply to the dependency or {@code null} if the dependency is not managed at all.
     */
    DependencyManagement manageDependency( Dependency dependency );

    /**
     * Derives a dependency manager for the specified node. When calculating the child manager, implementors are
     * strongly advised to simply return the current instance if nothing changed to help save memory.
     * 
     * @param node The node to derive a manager for, must not be {@code null}.
     * @param managedDependencies The dependency management to consider for the child, must not be {@code null}.
     * @return The dependency manager for the dependencies of the node, must not be {@code null}.
     */
    DependencyManager deriveChildManager( DependencyNode node, List<? extends Dependency> managedDependencies );

}
