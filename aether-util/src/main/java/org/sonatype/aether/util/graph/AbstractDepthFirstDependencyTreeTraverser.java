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
 ******************************************************************************/

package org.sonatype.aether.util.graph;

import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.graph.DependencyVisitor;

import java.io.File;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Abstract base class for depth first dependency tree traversers. Subclasses of this visitor will visit each
 * node exactly once regardless how many paths within the dependency graph lead to the node such that the
 * resulting node sequence is free of duplicates.<br/>
 * Actual vertex ordering (preorder, inorder, postorder) needs to be defined by subclasses through appropriate
 * implementations for {@link #visitEnter(org.sonatype.aether.graph.DependencyNode)} and
 * {@link #visitLeave(org.sonatype.aether.graph.DependencyNode)}
 *
 * @author Ansgar Konermann
 * @author Benjamin Bentmann
 */
public abstract class AbstractDepthFirstDependencyTreeTraverser
    implements DependencyVisitor
{

    protected final Map<DependencyNode, Object> visitedNodes;

    protected final List<DependencyNode> nodes;

    public AbstractDepthFirstDependencyTreeTraverser()
    {
        nodes = new ArrayList<DependencyNode>( 128 );
        visitedNodes = new IdentityHashMap<DependencyNode, Object>( 512 );
    }

    /**
     * Gets the list of dependency nodes that was generated during the graph traversal.
     *
     * @return The list of dependency nodes in preorder, never {@code null}.
     */
    public List<DependencyNode> getNodes()
    {
        return nodes;
    }

    /**
     * Gets the dependencies seen during the graph traversal.
     *
     * @param includeUnresolved Whether unresolved dependencies shall be included in the result or not.
     * @return The list of dependencies in preorder, never {@code null}.
     */
    public List<Dependency> getDependencies( boolean includeUnresolved )
    {
        List<Dependency> dependencies = new ArrayList<Dependency>( getNodes().size() );

        for ( DependencyNode node : getNodes() )
        {
            Dependency dependency = node.getDependency();
            if ( dependency != null )
            {
                if ( includeUnresolved || dependency.getArtifact().getFile() != null )
                {
                    dependencies.add( dependency );
                }
            }
        }

        return dependencies;
    }

    /**
     * Gets the artifacts associated with the list of dependency nodes generated during the graph traversal.
     *
     * @param includeUnresolved Whether unresolved artifacts shall be included in the result or not.
     * @return The list of artifacts in preorder, never {@code null}.
     */
    public List<Artifact> getArtifacts( boolean includeUnresolved )
    {
        List<Artifact> artifacts = new ArrayList<Artifact>( getNodes().size() );

        for ( DependencyNode node : getNodes() )
        {
            if ( node.getDependency() != null )
            {
                Artifact artifact = node.getDependency().getArtifact();
                if ( includeUnresolved || artifact.getFile() != null )
                {
                    artifacts.add( artifact );
                }
            }
        }

        return artifacts;
    }

    /**
     * Gets the files of resolved artifacts seen during the graph traversal.
     *
     * @return The list of artifact files in preorder, never {@code null}.
     */
    public List<File> getFiles()
    {
        List<File> files = new ArrayList<File>( getNodes().size() );

        for ( DependencyNode node : getNodes() )
        {
            if ( node.getDependency() != null )
            {
                File file = node.getDependency().getArtifact().getFile();
                if ( file != null )
                {
                    files.add( file );
                }
            }
        }

        return files;
    }

    /**
     * Gets a class path by concatenating the artifact files of the visited dependency nodes. Nodes with unresolved
     * artifacts are automatically skipped.
     *
     * @return The class path, using the platform-specific path separator, never {@code null}.
     */
    public String getClassPath()
    {
        StringBuilder buffer = new StringBuilder( 1024 );

        for ( Iterator<DependencyNode> it = getNodes().iterator(); it.hasNext(); )
        {
            DependencyNode node = it.next();
            if ( node.getDependency() != null )
            {
                Artifact artifact = node.getDependency().getArtifact();
                if ( artifact.getFile() != null )
                {
                    buffer.append( artifact.getFile().getAbsolutePath() );
                    if ( it.hasNext() )
                    {
                        buffer.append( File.pathSeparatorChar );
                    }
                }
            }
        }

        return buffer.toString();
    }

    protected void setAlreadyVisited( DependencyNode node )
    {
        visitedNodes.put( node, Boolean.TRUE );
    }

    protected boolean isAlreadyVisited( DependencyNode node )
    {
        return visitedNodes.containsKey( node );
    }

    public abstract boolean visitEnter( DependencyNode node );

    public abstract boolean visitLeave( DependencyNode node );
}
