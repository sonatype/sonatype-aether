package org.sonatype.maven.repository.util;

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

import java.util.ArrayList;
import java.util.List;

import org.sonatype.maven.repository.Dependency;
import org.sonatype.maven.repository.DependencyFilter;
import org.sonatype.maven.repository.DependencyNode;
import org.sonatype.maven.repository.DependencyVisitor;

/**
 * Generates a sequence of dependencies from a dependeny graph by traversing the graph in pre-order.
 * 
 * @author Benjamin Bentmann
 */
public class DependencyListGenerator
    implements DependencyVisitor
{

    private DependencyFilter filter;

    private List<Dependency> dependencies;

    /**
     * Creates a new list generator.
     */
    public DependencyListGenerator()
    {
        this( null );
    }

    /**
     * Generates a new list generator with the specified filter.
     * 
     * @param filter The dependency filter to use, may be {@code null} to include all artifacts.
     */
    public DependencyListGenerator( DependencyFilter filter )
    {
        this.filter = filter;
        this.dependencies = new ArrayList<Dependency>();
    }

    /**
     * Gets the list of dependencies that was generated during the graph traversal.
     * 
     * @return The list of dependencies in preorder, never {@code null}.
     */
    public List<Dependency> getDependencies()
    {
        return dependencies;
    }

    /**
     * Gets the list of dependencies that was generated during the traversal of the specified dependency graph.
     * 
     * @param node The root node of the dependency graph to traverse, must not be {@code null}.
     * @return The list of dependencies in preorder, never {@code null}.
     */
    public static List<Dependency> getDependencies( DependencyNode node )
    {
        DependencyListGenerator alg = new DependencyListGenerator();
        node.accept( alg );
        return alg.getDependencies();
    }

    public boolean visitEnter( DependencyNode node )
    {
        if ( node.getDependency() != null && ( filter == null || filter.filterDependency( node ) ) )
        {
            dependencies.add( node.getDependency() );
        }

        return true;
    }

    public boolean visitLeave( DependencyNode node )
    {
        return true;
    }

}
