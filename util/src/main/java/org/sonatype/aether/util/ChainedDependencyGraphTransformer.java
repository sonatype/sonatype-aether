package org.sonatype.aether.util;

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

import org.sonatype.aether.DependencyGraphTransformer;
import org.sonatype.aether.DependencyNode;
import org.sonatype.aether.RepositoryException;

/**
 * @author Benjamin Bentmann
 */
public class ChainedDependencyGraphTransformer
    implements DependencyGraphTransformer
{

    private final DependencyGraphTransformer[] transformers;

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

    public DependencyNode transformGraph( DependencyNode node )
        throws RepositoryException
    {
        for ( DependencyGraphTransformer transformer : transformers )
        {
            node = transformer.transformGraph( node );
        }
        return node;
    }

}
