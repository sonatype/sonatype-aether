package org.sonatype.maven.repository;

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
     * Derives a dependency selector for the specified node. Implementors are expected to calculate a new dependency
     * selector for the dependencies of the node.
     * 
     * @param node The node to derive a filter for, must not be {@code null}.
     * @return The dependency filter for the node, must not be {@code null}.
     */
    DependencySelector deriveChildSelector( DependencyNode node );

}
