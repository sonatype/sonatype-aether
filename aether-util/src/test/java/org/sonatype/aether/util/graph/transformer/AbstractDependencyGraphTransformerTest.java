package org.sonatype.aether.util.graph.transformer;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import java.util.LinkedList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.sonatype.aether.collection.DependencyGraphTransformationContext;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.test.util.NodeBuilder;

/**
 * @author Benjamin Bentmann
 */
public abstract class AbstractDependencyGraphTransformerTest
{

    protected NodeBuilder builder;

    protected DependencyGraphTransformationContext context;

    protected DependencyGraphTransformationContext newContext()
    {
        return new SimpleDependencyGraphTransformationContext();
    }

    protected List<DependencyNode> find( DependencyNode node, String id )
    {
        LinkedList<DependencyNode> trail = new LinkedList<DependencyNode>();
        find( trail, node, id );
        return trail;
    }

    private boolean find( LinkedList<DependencyNode> trail, DependencyNode node, String id )
    {
        trail.addFirst( node );

        if ( isMatch( node, id ) )
        {
            return true;
        }

        for ( DependencyNode child : node.getChildren() )
        {
            if ( find( trail, child, id ) )
            {
                return true;
            }
        }

        trail.removeFirst();

        return false;
    }

    private boolean isMatch( DependencyNode node, String id )
    {
        if ( node.getDependency() == null )
        {
            return false;
        }
        return id.equals( node.getDependency().getArtifact().getArtifactId() );
    }

    @Before
    public void setUp()
    {
        builder = new NodeBuilder();
        context = newContext();
    }

    @After
    public void tearDown()
    {
        builder = null;
        context = null;
    }

}
