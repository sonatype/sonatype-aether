package org.sonatype.aether.collection;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import org.sonatype.aether.graph.Dependency;

/**
 * Decides whether the dependencies of a dependency node should be traversed as well. <em>Note:</em> For the sake of
 * good performance during dependency collection, implementations should provide a semantic
 * {@link Object#equals(Object) equals()} method.
 * 
 * @author Benjamin Bentmann
 * @see org.sonatype.aether.RepositorySystemSession#getDependencyTraverser()
 * @see org.sonatype.aether.RepositorySystem#collectDependencies(org.sonatype.aether.RepositorySystemSession,
 *      CollectRequest)
 */
public interface DependencyTraverser
{

    /**
     * Decides whether the dependencies of the specified dependency should be traversed.
     * 
     * @param dependency The dependency to check, must not be {@code null}.
     * @return {@code true} if the dependency graph builder should recurse into the specified dependency and process its
     *         dependencies, {@code false} otherwise.
     */
    boolean traverseDependency( Dependency dependency );

    /**
     * Derives a dependency traverser that will be used to decide whether the transitive dependencies of the dependency
     * given in the collection context shall be traversed. When calculating the child traverser, implementors are
     * strongly advised to simply return the current instance if nothing changed to help save memory.
     * 
     * @param context The dependency collection context, must not be {@code null}.
     * @return The dependency traverser for the target node, must not be {@code null}.
     */
    DependencyTraverser deriveChildTraverser( DependencyCollectionContext context );

}
