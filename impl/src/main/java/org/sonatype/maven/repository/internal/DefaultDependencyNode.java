package org.sonatype.maven.repository.internal;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.sonatype.maven.repository.Artifact;
import org.sonatype.maven.repository.Dependency;
import org.sonatype.maven.repository.DependencyInfo;
import org.sonatype.maven.repository.DependencyNode;
import org.sonatype.maven.repository.DependencyVisitor;
import org.sonatype.maven.repository.RemoteRepository;
import org.sonatype.maven.repository.Version;
import org.sonatype.maven.repository.VersionConstraint;

/**
 * A node within a dependency graph.
 * 
 * @author Benjamin Bentmann
 */
public class DefaultDependencyNode
    implements DependencyNode
{

    private DependencyNode parent;

    private List<DependencyNode> children = new ArrayList<DependencyNode>( 0 );

    private int depth;

    private DependencyInfo info;

    /**
     * Creates a new root node with the specified dependency info. Use {@link #addChild} to create inner nodes.
     * 
     * @param info The dependency info associated with this node, may be {@code null}.
     */
    public DefaultDependencyNode( DependencyInfo info )
    {
        this.info = info;
        this.parent = null;
        this.depth = 0;
    }

    public DefaultDependencyNode( DependencyInfo info, DependencyNode parent )
    {
        this.info = info;
        this.parent = parent;
        this.depth = ( parent != null ) ? parent.getDepth() + 1 : 0;
    }

    /**
     * Gets the parent node of this node (if any).
     * 
     * @return The parent node or {@code null} if this node denotes the root of the dependency graph.
     */
    public DependencyNode getParent()
    {
        return parent;
    }

    /**
     * Gets the depth of this node within the dependency graph. The root node has depth zero.
     * 
     * @return The depth of this node within the dependency graph.
     */
    public int getDepth()
    {
        return depth;
    }

    /**
     * Gets the child nodes of this node.
     * 
     * @return The child nodes of this node, never {@code null}.
     */
    public List<DependencyNode> getChildren()
    {
        return children;
    }

    /**
     * Adds a new child node for the specified dependency to this node.
     * 
     * @param info The dependency information associated with the new child node, must not be {@code null}.
     * @return The new child node, never {@code null}.
     */
    public DefaultDependencyNode addChild( DependencyInfo info )
    {
        DefaultDependencyNode child = new DefaultDependencyNode( info, this );
        children.add( child );
        return child;
    }

    public void addChild( DependencyNode child )
    {
        children.add( child );
    }

    public DependencyInfo getInfo()
    {
        return info;
    }

    /**
     * Gets the dependency associated with this node. <em>Note:</em> For dependency graphs that have been constructed
     * without a root dependency, the root node will not have a dependency associated with it.
     * 
     * @return The dependency or {@code null} if none.
     */
    public Dependency getDependency()
    {
        return info.getDependency();
    }

    /**
     * Gets the context in which this dependency node was created.
     * 
     * @return The context, never {@code null}.
     */
    public String getContext()
    {
        return info.getContext();
    }

    /**
     * Sets the context in which this dependency node was created.
     * 
     * @param context The context, may be {@code null}.
     * @return This dependency node for chaining, never {@code null}.
     */
    public DefaultDependencyNode setContext( String context )
    {
        info.setContext( context );
        return this;
    }

    /**
     * Gets the sequence of relocations that was followed to resolve this dependency's artifact.
     * 
     * @return The sequence of relocations, never {@code null}.
     */
    public List<Artifact> getRelocations()
    {
        return info.getRelocations();
    }

    /**
     * Sets the sequence of relocations that was followed to resolve this dependency's artifact.
     * 
     * @param relocations The sequence of relocations, may be {@code null}.
     * @return This dependency node for chaining, never {@code null}.
     */
    public DefaultDependencyNode setRelocations( List<Artifact> relocations )
    {
        info.setRelocations( relocations );
        return this;
    }

    /**
     * Gets the known aliases for this dependency's artifact. An alias can be used to mark a patched rebuild of some
     * other artifact as such, thereby allowing conflict resolution to consider the patched and the orginal artifact as
     * a conflict.
     * 
     * @return The known aliases, never {@code null}.
     */
    public List<Artifact> getAliases()
    {
        return info.getAliases();
    }

    /**
     * Sets the known aliases for this dependency's artifact.
     * 
     * @param aliases The known aliases, may be {@code null}.
     * @return This dependency node for chaining, never {@code null}.
     */
    public DefaultDependencyNode setAliases( List<Artifact> aliases )
    {
        info.setAliases( aliases );
        return this;
    }

    /**
     * Gets the properties from the artifact's descriptor.
     * 
     * @return The artifact descriptor properties.
     * @see ArtifactDescriptorResult#getProperties()
     */
    public Map<String, Object> getProperties()
    {
        return info.getProperties();
    }

    /**
     * Sets the properties from the artifact's descriptor.
     * 
     * @param properties The artifact descriptor properties, may be {@code null}.
     * @return This dependency node for chaining, never {@code null}.
     */
    public DefaultDependencyNode setProperties( Map<String, Object> properties )
    {
        info.setProperties( properties );
        return this;
    }

    /**
     * Gets the conflict identifier for this node. Nodes having equal conflict identifiers are considered a conflict
     * group and are subject to conflict resolution.
     * 
     * @return The conflict identifier or {@code null} if none.
     */
    public Object getConflictId()
    {
        return info.getConflictId();
    }

    /**
     * Sets the conflict identifier for this node.
     * 
     * @param conflictId The conflict identifier, may be {@code null}.
     * @return This dependency node for chaining, never {@code null}.
     */
    public DefaultDependencyNode setConflictId( Object conflictId )
    {
        info.setConflictId( conflictId );
        return this;
    }

    /**
     * Gets the version constraint that was parsed from the dependency's version declaration.
     * 
     * @return The version constraint for this node or {@code null}.
     */
    public VersionConstraint getVersionConstraint()
    {
        return info.getVersionConstraint();
    }

    public DefaultDependencyNode setVersionConstraint( VersionConstraint versionConstraint )
    {
        info.setVersionConstraint( versionConstraint );
        return this;
    }

    public Version getVersion()
    {
        return info.getVersion();
    }

    public DefaultDependencyNode setVersion( Version version )
    {
        info.setVersion( version );
        return this;
    }

    public DefaultDependencyNode setScope( String scope )
    {
        info.setScope( scope );
        return this;
    }

    public DependencyNode setArtifact( Artifact artifact )
    {
        info.setArtifact( artifact );
        return this;
    }

    /**
     * Gets the version or version range for this dependency before dependency management was applied (if any).
     * 
     * @return The dependency version before dependency management or {@code null} if the version was not managed.
     */
    public String getPremanagedVersion()
    {
        return info.getPremanagedVersion();
    }

    /**
     * Sets the version or version range for this dependency before dependency management was applied (if any).
     * 
     * @param premanagedVersion The originally declared dependency version or {@code null} if the version was not
     *            managed.
     * @return This dependency node for chaining, never {@code null}.
     */
    public DefaultDependencyNode setPremanagedVersion( String premanagedVersion )
    {
        info.setPremanagedVersion( premanagedVersion );
        return this;
    }

    /**
     * Gets the scope for this dependency before dependency management was applied (if any).
     * 
     * @return The dependency scope before dependency management or {@code null} if the scope was not managed.
     */
    public String getPremanagedScope()
    {
        return info.getPremanagedScope();
    }

    /**
     * Sets the scope for this dependency before dependency management was applied (if any).
     * 
     * @param premanagedScope The originally declared dependency scope or {@code null} if the scope was not managed.
     * @return This dependency node for chaining, never {@code null}.
     */
    public DefaultDependencyNode setPremanagedScope( String premanagedScope )
    {
        info.setPremanagedScope( premanagedScope );
        return this;
    }

    /**
     * Gets the remote repositories from which this node's artifact shall be resolved.
     * 
     * @return The remote repositories to use for artifact resolution, never {@code null}.
     */
    public List<RemoteRepository> getRepositories()
    {
        return info.getRepositories();
    }

    /**
     * Sets the remote repositories from which this node's artifact shall be resolved.
     * 
     * @param repositories The remote repositories to use for artifact resolution, may be {@code null}.
     * @return This dependency node for chaining, never {@code null}.
     */
    public DefaultDependencyNode setRepositories( List<RemoteRepository> repositories )
    {
        info.setRepositories( repositories );
        return this;
    }

    /**
     * Traverses this node and potentially its children using the specified visitor.
     * 
     * @param visitor The visitor to call back, must not be {@code null}.
     * @return {@code true} to visit siblings nodes of this node as well, {@code false} to skip siblings.
     */
    public boolean accept( DependencyVisitor visitor )
    {
        if ( visitor.visitEnter( this ) )
        {
            for ( DependencyNode child : getChildren() )
            {
                if ( !child.accept( visitor ) )
                {
                    break;
                }
            }
        }

        return visitor.visitLeave( this );
    }

    @Override
    public String toString()
    {
        if ( getChildren().isEmpty() )
        {
            return String.valueOf( getDependency() );
        }
        else
        {
            return String.valueOf( getDependency() ) + " -> " + getChildren();
        }
    }

}
