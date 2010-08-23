package org.sonatype.aether.util.graph.transformer;

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
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.sonatype.aether.DependencyGraphTransformationContext;
import org.sonatype.aether.DependencyNode;
import org.sonatype.aether.util.NodeBuilder;

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
