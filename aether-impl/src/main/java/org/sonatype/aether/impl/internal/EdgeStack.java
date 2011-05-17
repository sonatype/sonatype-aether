package org.sonatype.aether.impl.internal;

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
 *******************************************************************************/

import java.util.Arrays;

import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.graph.Dependency;

/**
 * @author Benjamin Bentmann
 * @see DefaultDependencyCollector
 */
class EdgeStack
{

    private GraphEdge[] edges = new GraphEdge[64];

    private int size;

    public GraphEdge top()
    {
        if ( size <= 0 )
        {
            throw new IllegalStateException( "stack empty" );
        }
        return edges[size - 1];
    }

    public void push( GraphEdge edge )
    {
        if ( size >= edges.length )
        {
            GraphEdge[] tmp = new GraphEdge[size + 64];
            System.arraycopy( edges, 0, tmp, 0, edges.length );
            edges = tmp;
        }
        edges[size++] = edge;
    }

    public void pop()
    {
        if ( --size < 0 )
        {
            throw new IllegalStateException( "stack empty" );
        }
    }

    public GraphEdge find( Artifact artifact )
    {
        for ( int i = size - 1; i >= 0; i-- )
        {
            GraphEdge edge = edges[i];

            Dependency dependency = edge.getDependency();
            if ( dependency == null )
            {
                break;
            }

            Artifact a = dependency.getArtifact();
            if ( !a.getArtifactId().equals( artifact.getArtifactId() ) )
            {
                continue;
            }
            if ( !a.getGroupId().equals( artifact.getGroupId() ) )
            {
                continue;
            }
            if ( !a.getBaseVersion().equals( artifact.getBaseVersion() ) )
            {
                continue;
            }
            if ( !a.getExtension().equals( artifact.getExtension() ) )
            {
                continue;
            }
            if ( !a.getClassifier().equals( artifact.getClassifier() ) )
            {
                continue;
            }

            return edge;
        }

        return null;
    }

    @Override
    public String toString()
    {
        return Arrays.toString( edges );
    }

}
