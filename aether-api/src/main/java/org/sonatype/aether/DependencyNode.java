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

import java.util.Collection;
import java.util.List;

/**
 * A node within a dependency graph. <em>Note:</em> When traversing a dirty graph, i.e. a graph which hasn't undergone
 * conflict resolution, there can be multiple path leading to the same node instance.
 * 
 * @author Benjamin Bentmann
 */
public interface DependencyNode
{

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
     * Sets the artifact of the dependency.
     * 
     * @param artifact The artifact satisfying the dependency, must not be {@code null}.
     */
    void setArtifact( Artifact artifact );

    /**
     * Gets the sequence of relocations that was followed to resolve the artifact referenced by the dependency.
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
    Collection<Artifact> getAliases();

    /**
     * Gets the version constraint that was parsed from the dependency's version declaration.
     * 
     * @return The version constraint for this node or {@code null}.
     */
    VersionConstraint getVersionConstraint();

    /**
     * Gets the version that was selected for the dependency's target artifact.
     * 
     * @return The parsed version or {@code null}.
     */
    Version getVersion();

    /**
     * Sets the scope of the dependency.
     * 
     * @param scope The scope, may be {@code null}.
     */
    void setScope( String scope );

    /**
     * Gets the version or version range for the dependency before dependency management was applied (if any).
     * 
     * @return The dependency version before dependency management or {@code null} if the version was not managed.
     */
    String getPremanagedVersion();

    /**
     * Gets the scope for the dependency before dependency management was applied (if any).
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
     * Gets the request context in which this dependency node was created.
     * 
     * @return The request context, never {@code null}.
     */
    String getRequestContext();

    /**
     * Sets the request context in which this dependency node was created.
     * 
     * @param context The context, may be {@code null}.
     */
    void setRequestContext( String context );

    /**
     * Traverses this node and potentially its children using the specified visitor.
     * 
     * @param visitor The visitor to call back, must not be {@code null}.
     * @return {@code true} to visit siblings nodes of this node as well, {@code false} to skip siblings.
     */
    boolean accept( DependencyVisitor visitor );

}
