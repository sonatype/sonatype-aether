package org.sonatype.aether.util.graph.transformer;

/*******************************************************************************
 * Copyright (c) 2010 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

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
        if ( dependency != null )
        {
            Artifact artifact = dependency.getArtifact();

            String key =
                String.format( "%s:%s:%s:%s", artifact.getGroupId(), artifact.getArtifactId(),
                               artifact.getClassifier(), artifact.getExtension() );

            @SuppressWarnings( "unchecked" )
            Map<DependencyNode, Object> nodes =
                (Map<DependencyNode, Object>) context.get( TransformationContextKeys.CONFLICT_IDS );
            if ( nodes == null )
            {
                nodes = new IdentityHashMap<DependencyNode, Object>();
                context.put( TransformationContextKeys.CONFLICT_IDS, nodes );
            }

            nodes.put( node, key );
        }

        for ( DependencyNode child : node.getChildren() )
        {
            transformGraph( child, context );
        }

        return node;
    }

}
