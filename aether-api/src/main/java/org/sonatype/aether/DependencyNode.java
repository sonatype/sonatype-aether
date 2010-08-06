package org.sonatype.aether;

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

import java.util.List;

/**
 * A node within a dependency graph.
 * 
 * @author Benjamin Bentmann
 */
public interface DependencyNode
{

    /**
     * Gets the parent node of this node (if any).
     * 
     * @return The parent node or {@code null} if this node denotes the root of the dependency graph.
     */
    DependencyNode getParent();

    /**
     * Gets the depth of this node within the dependency graph. The root node has depth zero.
     * 
     * @return The depth of this node within the dependency graph.
     */
    int getDepth();

    /**
     * Gets the child nodes of this node.
     * 
     * @return The child nodes of this node, never {@code null}.
     */
    List<DependencyNode> getChildren();

    /**
     * Gets the dependency associated with this node. <em>Note:</em> For dependency graphs that have been constructed
     * without a root dependency, the root node will not have a dependency associated with it.
     * 
     * @return The dependency or {@code null} if none.
     */
    Dependency getDependency();

    /**
     * Gets the context in which this dependency node was created.
     * 
     * @return The context, never {@code null}.
     */
    String getContext();

    /**
     * Sets the context in which this dependency node was created.
     * 
     * @param context The context, may be {@code null}.
     * @return This dependency node for chaining, never {@code null}.
     */
    DependencyNode setContext( String context );

    /**
     * Gets the sequence of relocations that was followed to resolve this dependency's artifact.
     * 
     * @return The sequence of relocations, never {@code null}.
     */
    List<Artifact> getRelocations();

    /**
     * Gets the known aliases for this dependency's artifact. An alias can be used to mark a patched rebuild of some
     * other artifact as such, thereby allowing conflict resolution to consider the patched and the original artifact as
     * a conflict.
     * 
     * @return The known aliases, never {@code null}.
     */
    List<Artifact> getAliases();

    /**
     * Gets the conflict identifier for this node. Nodes having equal conflict identifiers are considered a conflict
     * group and are subject to conflict resolution.
     * 
     * @return The conflict identifier or {@code null} if none.
     */
    Object getConflictId();

    /**
     * Sets the conflict identifier for this node.
     * 
     * @param conflictId The conflict identifier, may be {@code null}.
     * @return This dependency node for chaining, never {@code null}.
     */
    DependencyNode setConflictId( Object conflictId );

    /**
     * Gets the version constraint that was parsed from the dependency's version declaration.
     * 
     * @return The version constraint for this node or {@code null}.
     */
    VersionConstraint getVersionConstraint();

    DependencyNode setVersionConstraint( VersionConstraint versionConstraint );

    Version getVersion();

    DependencyNode setScope( String scope );

    DependencyNode setArtifact( Artifact artifact );

    /**
     * Gets the version or version range for this dependency before dependency management was applied (if any).
     * 
     * @return The dependency version before dependency management or {@code null} if the version was not managed.
     */
    String getPremanagedVersion();

    /**
     * Gets the scope for this dependency before dependency management was applied (if any).
     * 
     * @return The dependency scope before dependency management or {@code null} if the scope was not managed.
     */
    String getPremanagedScope();

    /**
     * Gets the remote repositories from which this node's artifact shall be resolved.
     * 
     * @return The remote repositories to use for artifact resolution, never {@code null}.
     */
    List<RemoteRepository> getRepositories();

    /**
     * Sets the remote repositories from which this node's artifact shall be resolved.
     * 
     * @param repositories The remote repositories to use for artifact resolution, may be {@code null}.
     * @return This dependency node for chaining, never {@code null}.
     */
    DependencyNode setRepositories( List<RemoteRepository> repositories );

    /**
     * Traverses this node and potentially its children using the specified visitor.
     * 
     * @param visitor The visitor to call back, must not be {@code null}.
     * @return {@code true} to visit siblings nodes of this node as well, {@code false} to skip siblings.
     */
    boolean accept( DependencyVisitor visitor );

}
