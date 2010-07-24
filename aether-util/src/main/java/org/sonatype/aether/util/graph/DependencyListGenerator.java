package org.sonatype.aether.util.graph;

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

import java.util.ArrayList;
import java.util.List;

import org.sonatype.aether.Dependency;
import org.sonatype.aether.DependencyFilter;
import org.sonatype.aether.DependencyNode;
import org.sonatype.aether.DependencyVisitor;

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
        if ( node.getDependency() != null && ( filter == null || filter.accept( node ) ) )
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
