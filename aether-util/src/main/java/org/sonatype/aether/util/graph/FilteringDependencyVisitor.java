package org.sonatype.aether.util.graph;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import java.util.LinkedList;

import org.sonatype.aether.graph.DependencyFilter;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.graph.DependencyVisitor;

/**
 * A dependency visitor that delegates to another visitor if nodes match a filter. Note that in case of a mismatching
 * node, the children of that node are still visisted and presented to the filter.
 * 
 * @author Benjamin Bentmann
 */
public class FilteringDependencyVisitor
    implements DependencyVisitor
{

    private final DependencyFilter filter;

    private final DependencyVisitor visitor;

    private final LinkedList<Boolean> accepts;

    private final LinkedList<DependencyNode> parents;

    /**
     * Creates a new visitor that delegates traversal of nodes matching the given filter to the specified visitor.
     * 
     * @param visitor The visitor to delegate to, must not be {@code null}.
     * @param filter The filter to apply, may be {@code null} to not filter.
     */
    public FilteringDependencyVisitor( DependencyVisitor visitor, DependencyFilter filter )
    {
        if ( visitor == null )
        {
            throw new IllegalArgumentException( "dependency visitor not specified" );
        }
        this.visitor = visitor;
        this.filter = filter;
        this.accepts = new LinkedList<Boolean>();
        this.parents = new LinkedList<DependencyNode>();
    }

    /**
     * Gets the visitor to which this visitor delegates to.
     * 
     * @return The visitor being delegated to, never {@code null}.
     */
    public DependencyVisitor getVisitor()
    {
        return visitor;
    }

    /**
     * Gets the filter being applied before delegation.
     * 
     * @return The filter being applied or {@code null} if none.
     */
    public DependencyFilter getFilter()
    {
        return filter;
    }

    public boolean visitEnter( DependencyNode node )
    {
        boolean accept = filter == null || filter.accept( node, parents );

        accepts.addFirst( Boolean.valueOf( accept ) );

        parents.addFirst( node );

        if ( accept )
        {
            return visitor.visitEnter( node );
        }
        else
        {
            return true;
        }
    }

    public boolean visitLeave( DependencyNode node )
    {
        parents.removeFirst();

        Boolean accept = accepts.removeFirst();

        if ( accept.booleanValue() )
        {
            return visitor.visitLeave( node );
        }
        else
        {
            return true;
        }
    }

}
