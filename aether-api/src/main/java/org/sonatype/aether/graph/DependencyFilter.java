package org.sonatype.aether.graph;

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
 * A filter to include/exclude dependency nodes during other operations.
 * 
 * @author Benjamin Bentmann
 */
public interface DependencyFilter
{

    /**
     * Indicates whether the specified dependency node shall be included or excluded.
     * 
     * @param node The dependency node to filter, must not be {@code null}.
     * @param parents The (read-only) chain of parent nodes that leads to the node to be filtered, must not be
     *            {@code null}. Iterating this (possibly empty) list walks up the dependency graph towards the root
     *            node, i.e. the immediate parent node (if any) is the first node in the list. The size of the list also
     *            denotes the zero-based depth of the filtered node.
     * @return {@code true} to include the dependency node, {@code false} to exclude it.
     */
    boolean accept( DependencyNode node, List<DependencyNode> parents );

}
