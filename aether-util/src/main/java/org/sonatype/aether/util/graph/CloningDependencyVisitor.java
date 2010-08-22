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
import java.util.LinkedList;
import java.util.Map;

import org.sonatype.aether.DependencyNode;
import org.sonatype.aether.DependencyVisitor;

/**
 * A dependency visitor that constructs a clone of the visited dependency graph. If such a visitor is passed into a
 * {@link FilteringDependencyVisitor}, a sub graph can be created. This class creates shallow clones of the visited
 * dependency nodes but clients can create a subclass and override {@link #clone(DependencyNode)} to alter the clone
 * process.
 * 
 * @author Benjamin Bentmann
 */
public class CloningDependencyVisitor
    implements DependencyVisitor
{

    private final Map<DependencyNode, DependencyNode> clones;

    private final LinkedList<DependencyNode> parents;

    private DependencyNode root;

    /**
     * Creates a new visitor that clones the visited nodes.
     */
    public CloningDependencyVisitor()
    {
        parents = new LinkedList<DependencyNode>();
        clones = new IdentityHashMap<DependencyNode, DependencyNode>( 256 );
    }

    /**
     * Gets the root node of the cloned dependency graph.
     * 
     * @return The root node of the cloned dependency graph or {@code null}.
     */
    public DependencyNode getRootNode()
    {
        return root;
    }

    /**
     * Creates a clone of the specified node for usage as a child of the given parent node.
     * 
     * @param node The node to clone, must not be {@code null}.
     * @return The cloned node, never {@code null}.
     */
    protected DependencyNode clone( DependencyNode node )
    {
        DefaultDependencyNode clone = new DefaultDependencyNode( node );
        return clone;
    }

    public boolean visitEnter( DependencyNode node )
    {
        boolean recurse = true;

        DependencyNode clone = clones.get( node );
        if ( clone == null )
        {
            clone = clone( node );
            clones.put( node, clone );
        }
        else
        {
            recurse = false;
        }

        DependencyNode parent = parents.peek();

        if ( parent == null )
        {
            root = clone;
        }
        else
        {
            parent.getChildren().add( clone );
        }

        parents.addFirst( clone );

        return recurse;
    }

    public boolean visitLeave( DependencyNode node )
    {
        parents.remove();

        return true;
    }

}
