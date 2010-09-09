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

import org.sonatype.aether.RepositoryException;
import org.sonatype.aether.collection.DependencyGraphTransformationContext;
import org.sonatype.aether.collection.DependencyGraphTransformer;
import org.sonatype.aether.graph.DependencyNode;

/**
 * A dependency graph transformer that does nothing.
 * 
 * @author Benjamin Bentmann
 */
public class NoopDependencyGraphTransformer
    implements DependencyGraphTransformer
{

    public static final DependencyGraphTransformer INSTANCE = new NoopDependencyGraphTransformer();

    public DependencyNode transformGraph( DependencyNode node, DependencyGraphTransformationContext context )
        throws RepositoryException
    {
        return node;
    }

}
