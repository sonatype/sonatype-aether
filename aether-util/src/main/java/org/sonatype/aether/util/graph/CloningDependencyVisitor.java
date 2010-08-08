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

import java.util.LinkedList;

import org.sonatype.aether.DependencyNode;
import org.sonatype.aether.DependencyVisitor;

/**
 * A dependency visitor that constructs a clone of the visited dependency graph. If such a visitor is passed into a
 * {@link FilteringDependencyVisitor}, a sub tree can be created. This class creates shallow clones of the visited
 * dependency nodes but clients can create a subclass and override {@link #clone(DependencyNode, DependencyNode)} to
 * alter the clone process.
 * 
 * @author Benjamin Bentmann
 */
public class CloningDependencyVisitor
    implements DependencyVisitor
{

    private final LinkedList<DependencyNode> parents;

    private DependencyNode root;

    /**
     * Creates a new visitor that clones the visited nodes.
     */
    public CloningDependencyVisitor()
    {
        parents = new LinkedList<DependencyNode>();
    }

    /**
     * Gets the root node of the cloned dependency tree.
     * 
     * @return The root node of the cloned dependency tree or {@code null}.
     */
    public DependencyNode getRootNode()
    {
        return root;
    }

    /**
     * Creates a clone of the specified node for usage as a child of the given parent node.
     * 
     * @param node The node to clone, must not be {@code null}.
     * @param parent The parent for the cloned node, may be {@code null} if none.
     * @return The cloned node, never {@code null}.
     */
    protected DependencyNode clone( DependencyNode node, DependencyNode parent )
    {
        DefaultDependencyNode clone = new DefaultDependencyNode( node.getDependency(), parent );
        clone.setAliases( node.getAliases() );
        clone.setConflictId( node.getConflictId() );
        clone.setContext( node.getContext() );
        clone.setPremanagedScope( node.getPremanagedScope() );
        clone.setPremanagedVersion( node.getPremanagedVersion() );
        clone.setRelocations( node.getRelocations() );
        clone.setRepositories( node.getRepositories() );
        clone.setVersion( node.getVersion() );
        clone.setVersionConstraint( node.getVersionConstraint() );
        return clone;
    }

    public boolean visitEnter( DependencyNode node )
    {
        DependencyNode parent = parents.peek();

        DependencyNode clone = clone( node, parent );

        if ( parent == null )
        {
            root = clone;
        }
        else
        {
            parent.getChildren().add( clone );
        }

        parents.addFirst( clone );

        return true;
    }

    public boolean visitLeave( DependencyNode node )
    {
        parents.remove();

        return true;
    }

}
