package org.apache.maven.repository;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
     * @param node The node whose dependency should be checked or {@code null} in case of the root dependency.
     * @param dependency The dependency to check, must not be {@code null}.
     * @return {@code true} if the dependency tree builder should recurse into the specified dependency and process its
     *         dependencies, {@code false} otherwise.
     */
    boolean accept( DependencyNode node, Dependency dependency );

    /**
     * Derives a dependency traverser for the specified child node of the current node, i.e. the parent of the specified
     * node. This method is called by the dependency tree builder just before it processes the child node. Implementors
     * are expected to calculate a new dependency traverser for the child node.
     * 
     * @param childNode The child node to derive a traverser for, must not be {@code null}.
     * @return The dependency traverser for the child node, must not be {@code null}.
     */
    DependencyTraverser deriveChildTraverser( DependencyNode childNode );

}
