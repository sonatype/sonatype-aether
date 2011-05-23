package org.sonatype.aether.collection;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import java.util.List;

import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.graph.Dependency;

/**
 * A context used during dependency collection to update the dependency manager, selector and traverser.
 * 
 * @author Benjamin Bentmann
 * @see DependencyManager#deriveChildManager(DependencyCollectionContext)
 * @see DependencyTraverser#deriveChildTraverser(DependencyCollectionContext)
 * @see DependencySelector#deriveChildSelector(DependencyCollectionContext)
 */
public interface DependencyCollectionContext
{

    /**
     * Gets the repository system session during which the dependency collection happens.
     * 
     * @return The repository system session, never {@code null}.
     */
    RepositorySystemSession getSession();

    /**
     * Gets the dependency whose children are to be processed next during dependency collection.
     * 
     * @return The dependency whose children are going to be processed or {@code null} in case of the root node without
     *         dependency.
     */
    Dependency getDependency();

    /**
     * Gets the dependency management information that was contributed by the artifact descriptor of the current
     * dependency.
     * 
     * @return The dependency management information, never {@code null}.
     */
    List<Dependency> getManagedDependencies();

}
