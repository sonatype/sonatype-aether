package org.sonatype.maven.repository.util;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.sonatype.maven.repository.DependencyGraphTransformer;
import org.sonatype.maven.repository.DependencyNode;
import org.sonatype.maven.repository.RepositoryException;

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
