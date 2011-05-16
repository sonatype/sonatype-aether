package org.sonatype.aether.util.graph;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 ******************************************************************************/

import org.sonatype.aether.graph.DependencyNode;

/**
 * Generates a sequence of dependency nodes from a dependeny graph by traversing the graph in postorder. This visitor
 * visits each node exactly once regardless how many paths within the dependency graph lead to the node such that the
 * resulting node sequence is free of duplicates.
 * 
 * @author Ansgar Konermann
 */
public class PostorderNodeListGenerator
    extends AbstractDepthFirstNodeListGenerator
{

    @Override
    public boolean visitEnter( DependencyNode node )
    {
        return true;
    }

    @Override
    public boolean visitLeave( DependencyNode node )
    {
        if ( ensureVisitedFlagIsSet( node ) == VisitStatus.WAS_VISITED_BEFORE )
        {
            return true;
        }

        if ( node.getDependency() != null )
        {
            nodes.add( node );
        }

        return true;
    }

}
