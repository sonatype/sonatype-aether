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

package org.sonatype.aether.util.graph;

import org.sonatype.aether.graph.DependencyNode;

/**
 * Generates a sequence of dependency nodes from a dependeny graph by traversing the graph in preorder.
 *
 * @author Benjamin Bentmann
 */
public class PreorderNodeListGenerator
    extends AbstractDepthFirstDependencyTreeTraverser
{
    @Override
    public boolean visitEnter( DependencyNode node )
    {
        if ( isAlreadyVisited( node ) )
        {
            return false;
        }
        else
        {
            setAlreadyVisited( node );
        }

        if ( node.getDependency() != null )
        {
            nodes.add( node );
        }

        return true;
    }

    @Override
    public boolean visitLeave( DependencyNode node )
    {
        return true;
    }

}
