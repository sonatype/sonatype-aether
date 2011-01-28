package org.sonatype.aether.collection;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *******************************************************************************/

import org.sonatype.aether.graph.Dependency;

/**
 * Applies dependency management to the dependencies of a dependency node.
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
     * Derives a dependency manager for the specified collection context. When calculating the child manager,
     * implementors are strongly advised to simply return the current instance if nothing changed to help save memory.
     * 
     * @param context The dependency collection context, must not be {@code null}.
     * @return The dependency manager for the dependencies of the target node, must not be {@code null}.
     */
    DependencyManager deriveChildManager( DependencyCollectionContext context );

}
