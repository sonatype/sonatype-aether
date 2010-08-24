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

import java.util.IdentityHashMap;
import java.util.Map;

import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.graph.DependencyVisitor;

/**
 * A dependency visitor that delegates to another visitor if a node hasn't been visited before. In other words, this
 * visitor provides a tree-view of a dependency graph which generally can have multiple paths to the same node.
 * 
 * @author Benjamin Bentmann
 */
public class TreeDependencyVisitor
    implements DependencyVisitor
{

    private final Map<DependencyNode, Object> visitedNodes;

    private final DependencyVisitor visitor;

    private boolean visited;

    /**
     * Creates a new visitor that delegates to the specified visitor.
     * 
     * @param visitor The visitor to delegate to, must not be {@code null}.
     */
    public TreeDependencyVisitor( DependencyVisitor visitor )
    {
        if ( visitor == null )
        {
            throw new IllegalArgumentException( "no visitor delegate specified" );
        }
        visitedNodes = new IdentityHashMap<DependencyNode, Object>( 512 );
        this.visitor = visitor;
    }

    public boolean visitEnter( DependencyNode node )
    {
        visited = visitedNodes.put( node, Boolean.TRUE ) != null;

        if ( visited )
        {
            return false;
        }

        return visitor.visitEnter( node );
    }

    public boolean visitLeave( DependencyNode node )
    {
        if ( visited )
        {
            return true;
        }

        return visitor.visitLeave( node );
    }

}
