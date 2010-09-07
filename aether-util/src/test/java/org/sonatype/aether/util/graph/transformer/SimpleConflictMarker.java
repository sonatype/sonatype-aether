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

import java.util.IdentityHashMap;
import java.util.Map;

import org.sonatype.aether.RepositoryException;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.collection.DependencyGraphTransformationContext;
import org.sonatype.aether.collection.DependencyGraphTransformer;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.graph.DependencyNode;

/**
 * Set "groupId:artId:classifier:extension" as conflict marker for every node.
 * 
 * @author Benjamin Hanzelmann
 */
class SimpleConflictMarker
    implements DependencyGraphTransformer
{

    public DependencyNode transformGraph( DependencyNode node, DependencyGraphTransformationContext context )
        throws RepositoryException
    {
        Dependency dependency = node.getDependency();
        Artifact artifact = dependency.getArtifact();

        String key =
            String.format( "%s:%s:%s:%s", artifact.getGroupId(), artifact.getArtifactId(), artifact.getClassifier(),
                           artifact.getExtension() );

        @SuppressWarnings( "unchecked" )
        Map<DependencyNode, Object> nodes =
            (Map<DependencyNode, Object>) context.get( TransformationContextKeys.CONFLICT_IDS );
        if ( nodes == null )
        {
            nodes = new IdentityHashMap<DependencyNode, Object>();
            context.put( TransformationContextKeys.CONFLICT_IDS, nodes );
        }

        nodes.put( node, key );

        for ( DependencyNode child : node.getChildren() )
        {
            transformGraph( child, context );
        }

        return node;
    }

}
