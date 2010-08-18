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

/**
 * A collection of keys used by the dependency graph transformers when exchanging information via the graph
 * transformation context.
 * 
 * @author Benjamin Bentmann
 * @see org.sonatype.aether.DependencyGraphTransformationContext#get(Object)
 */
public final class TransformationContextKeys
{

    /**
     * The key in the graph transformation context where a {@code Map<DependencyNode, Object>} is stored which maps
     * dependency nodes to their conflict ids. All nodes that map to an equal conflict id belong to the same group of
     * conflicting dependencies. Note that the map keys use reference equality.
     */
    public static final Object CONFLICT_IDS = "conflictIds";

    private TransformationContextKeys()
    {
        // hide constructor
    }

}
