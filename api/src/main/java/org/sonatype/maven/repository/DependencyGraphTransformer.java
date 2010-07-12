package org.sonatype.maven.repository;

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
 * Transforms a given dependency graph.
 * 
 * @author Benjamin Bentmann
 */
public interface DependencyGraphTransformer
{

    /**
     * Transforms the dependency graph denoted by the specified root node. The transformer may directly change the
     * provided input graph or create a new graph.
     * 
     * @param node The root node of the graph to transform, must not be {@code null}.
     * @return The result graph of the transformation, never {@code null}.
     * @throws RepositoryException If the transformation failed.
     */
    DependencyNode transformGraph( DependencyNode node )
        throws RepositoryException;

}
