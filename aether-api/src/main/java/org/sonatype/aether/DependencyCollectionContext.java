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
     * @return The dependency whose children are going to be processed, never {@code null}.
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
