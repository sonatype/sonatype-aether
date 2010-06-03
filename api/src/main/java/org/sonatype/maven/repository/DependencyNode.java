package org.sonatype.maven.repository;

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
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * A node within a dependency graph.
 * 
 * @author Benjamin Bentmann
 */
public class DependencyNode
{

    private Dependency dependency;

    private String context = "";

    private List<Artifact> relocations = Collections.emptyList();

    private List<Artifact> aliases = Collections.emptyList();

    private Map<String, Object> properties = Collections.emptyMap();

    private DependencyNode parent;

    private List<DependencyNode> children = new ArrayList<DependencyNode>( 4 );

    private int depth;

    private Object conflictId;

    private String requestedVersion;

    private List<RemoteRepository> repositories = Collections.emptyList();

    /**
     * Creates a new root node with the specified dependency. Use {@link #addChild(Dependency)} to create inner nodes.
     * 
     * @param dependency The dependency associated with this node, may be {@code null}.
     */
    public DependencyNode( Dependency dependency )
    {
        this.dependency = dependency;
        this.parent = null;
        this.depth = 0;
    }

    private DependencyNode( Dependency dependency, DependencyNode parent )
    {
        this.dependency = dependency;
        this.parent = parent;
        this.depth = ( parent != null ) ? parent.getDepth() + 1 : 0;
    }

    /**
     * Gets the dependency associated with this node.
     * 
     * @return The dependency or {@code null} if none.
     */
    public Dependency getDependency()
    {
        return dependency;
    }

    /**
     * Gets the context in which this dependency node was created.
     * 
     * @return The context, never {@code null}.
     */
    public String getContext()
    {
        return context;
    }

    /**
     * Sets the context in which this dependency node was created.
     * 
     * @param context The context, may be {@code null}.
     * @return This dependency node for chaining, never {@code null}.
     */
    public DependencyNode setContext( String context )
    {
        this.context = ( context != null ) ? context : "";
        return this;
    }

    /**
     * Gets the sequence of relocations that was followed to resolve this dependency's artifact.
     * 
     * @return The sequence of relocations, never {@code null}.
     */
    public List<Artifact> getRelocations()
    {
        return relocations;
    }

    /**
     * Sets the sequence of relocations that was followed to resolve this dependency's artifact.
     * 
     * @param relocations The sequence of relocations, may be {@code null}.
     * @return This dependency node for chaining, never {@code null}.
     */
    public DependencyNode setRelocations( List<Artifact> relocations )
    {
        if ( relocations == null )
        {
            this.relocations = Collections.emptyList();
        }
        else
        {
            this.relocations = relocations;
        }
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
        return aliases;
    }

    /**
     * Sets the known aliases for this dependency's artifact.
     * 
     * @param aliases The known aliases, may be {@code null}.
     * @return This dependency node for chaining, never {@code null}.
     */
    public DependencyNode setAliases( List<Artifact> aliases )
    {
        if ( aliases == null )
        {
            this.aliases = Collections.emptyList();
        }
        else
        {
            this.aliases = aliases;
        }
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
        return properties;
    }

    /**
     * Sets the properties from the artifact's descriptor.
     * 
     * @param properties The artifact descriptor properties, may be {@code null}.
     * @return This dependency node for chaining, never {@code null}.
     */
    public DependencyNode setPropertes( Map<String, Object> properties )
    {
        if ( properties == null )
        {
            this.properties = Collections.emptyMap();
        }
        else
        {
            this.properties = properties;
        }
        return this;
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
     * @param dependency The dependency associated with the new child node, must not be {@code null}.
     * @return The new child node, never {@code null}.
     */
    public DependencyNode addChild( Dependency dependency )
    {
        DependencyNode child = new DependencyNode( dependency, this );
        children.add( child );
        return child;
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
     * Gets the conflict identifier for this node. Nodes having equal conflict identifiers are considered a conflict
     * group and are subject to conflict resolution.
     * 
     * @return The conflict identifier or {@code null} if none.
     */
    public Object getConflictId()
    {
        return conflictId;
    }

    /**
     * Sets the conflict identifier for this node.
     * 
     * @param conflictId The conflict identifier, may be {@code null}.
     * @return This dependency node for chaining, never {@code null}.
     */
    public DependencyNode setConflictId( Object conflictId )
    {
        this.conflictId = conflictId;
        return this;
    }

    /**
     * Gets the version or version range from which this dependency node was originally created.
     * 
     * @return The originally requested dependency version, never {@code null}.
     */
    public String getRequestedVersion()
    {
        return requestedVersion;
    }

    /**
     * Sets the version or version range from this dependency node was originally created.
     * 
     * @param requestedVersion The originally requested dependency version, may be {@code null}.
     * @return This dependency node for chaining, never {@code null}.
     */
    public DependencyNode setRequestedVersion( String requestedVersion )
    {
        this.requestedVersion = ( requestedVersion != null ) ? requestedVersion : "";
        return this;
    }

    /**
     * Gets the remote repositories from which this node's artifact shall be resolved.
     * 
     * @return The remote repositories to use for artifact resolution, never {@code null}.
     */
    public List<RemoteRepository> getRepositories()
    {
        return repositories;
    }

    /**
     * Sets the remote repositories from which this node's artifact shall be resolved.
     * 
     * @param repositories The remote repositories to use for artifact resolution, may be {@code null}.
     * @return This dependency node for chaining, never {@code null}.
     */
    public DependencyNode setRepositories( List<RemoteRepository> repositories )
    {
        if ( repositories == null )
        {
            this.repositories = Collections.emptyList();
        }
        else
        {
            this.repositories = repositories;
        }
        return this;
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
