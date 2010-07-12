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
 * A visitor for nodes of the dependency graph.
 * 
 * @author Benjamin Bentmann
 * @see DependencyNode#accept(DependencyVisitor)
 */
public interface DependencyVisitor
{

    /**
     * Notifies the visitor of a node visit before its children have been processed.
     * 
     * @param node The dependency node being visited, must not be {@code null}.
     * @return {@code true} to visit child nodes of the specified node as well, {@code false} to skip children.
     */
    boolean visitEnter( DependencyNode node );

    /**
     * Notifies the visitor of a node visit after its children have been processed.
     * 
     * @param node The dependency node being visited, must not be {@code null}.
     * @return {@code true} to visit siblings nodes of the specified node as well, {@code false} to skip siblings.
     */
    boolean visitLeave( DependencyNode node );

}
