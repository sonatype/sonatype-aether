package org.sonatype.aether.util.graph;

/*******************************************************************************
 * Copyright (c) 2010 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

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
