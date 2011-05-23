package org.sonatype.aether.util.graph.transformer;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import org.sonatype.aether.RepositoryException;
import org.sonatype.aether.collection.DependencyGraphTransformationContext;
import org.sonatype.aether.collection.DependencyGraphTransformer;
import org.sonatype.aether.graph.DependencyNode;

/**
 * A dependency graph transformer that chains other transformers.
 * 
 * @author Benjamin Bentmann
 */
public class ChainedDependencyGraphTransformer
    implements DependencyGraphTransformer
{

    private final DependencyGraphTransformer[] transformers;

    /**
     * Creates a new transformer that chains the specified transformers.
     * 
     * @param transformers The transformers to chain, may be {@code null} or empty.
     */
    public ChainedDependencyGraphTransformer( DependencyGraphTransformer... transformers )
    {
        if ( transformers == null )
        {
            this.transformers = new DependencyGraphTransformer[0];
        }
        else
        {
            this.transformers = transformers;
        }
    }

    /**
     * Creates a new transformer that chains the specified transformers or simply returns one of them if the other one
     * is {@code null}.
     * 
     * @param transformer1 The first transformer of the chain, may be {@code null}.
     * @param transformer2 The second transformer of the chain, may be {@code null}.
     * @return The chained transformer or {@code null} if both input transformers are {@code null}.
     */
    public static DependencyGraphTransformer newInstance( DependencyGraphTransformer transformer1,
                                                          DependencyGraphTransformer transformer2 )
    {
        if ( transformer1 == null )
        {
            return transformer2;
        }
        else if ( transformer2 == null )
        {
            return transformer1;
        }
        return new ChainedDependencyGraphTransformer( transformer1, transformer2 );
    }

    public DependencyNode transformGraph( DependencyNode node, DependencyGraphTransformationContext context )
        throws RepositoryException
    {
        for ( DependencyGraphTransformer transformer : transformers )
        {
            node = transformer.transformGraph( node, context );
        }
        return node;
    }

}
