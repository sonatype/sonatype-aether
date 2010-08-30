package org.sonatype.aether.test.util.impl;

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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.graph.DependencyVisitor;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.version.Version;
import org.sonatype.aether.version.VersionConstraint;

/**
 * A node within a dependency graph.
 * 
 * @author Benjamin Bentmann
 */
public class TestDependencyNode
    implements DependencyNode
{

    private List<DependencyNode> children = new ArrayList<DependencyNode>( 0 );

    private Dependency dependency;

    private List<Artifact> relocations = Collections.emptyList();

    private Collection<Artifact> aliases = Collections.emptyList();

    private VersionConstraint versionConstraint;

    private Version version;

    private String premanagedVersion;

    private String premanagedScope;

    private List<RemoteRepository> repositories = Collections.emptyList();

    private String context = "";

    /**
     * Creates an empty dependency node.
     */
    public TestDependencyNode()
    {
        // enables no-arg constructor
    }

    /**
     * Creates a new root node with the specified dependency.
     * 
     * @param dependency The dependency associated with this node, may be {@code null}.
     */
    public TestDependencyNode( Dependency dependency )
    {
        this.dependency = dependency;
    }

    /**
     * Creates a shallow clone of the specified node.
     * 
     * @param node The node to copy, must not be {@code null}.
     */
    public TestDependencyNode( DependencyNode node )
    {
        setDependency( node.getDependency() );
        setAliases( node.getAliases() );
        setRequestContext( node.getRequestContext() );
        setPremanagedScope( node.getPremanagedScope() );
        setPremanagedVersion( node.getPremanagedVersion() );
        setRelocations( node.getRelocations() );
        setRepositories( node.getRepositories() );
        setVersion( node.getVersion() );
        setVersionConstraint( node.getVersionConstraint() );
    }

    public List<DependencyNode> getChildren()
    {
        return children;
    }

    public Dependency getDependency()
    {
        return dependency;
    }

    public void setDependency( Dependency dependency )
    {
        this.dependency = dependency;
    }

    public void setArtifact( Artifact artifact )
    {
        dependency = dependency.setArtifact( artifact );
    }

    public List<Artifact> getRelocations()
    {
        return relocations;
    }

    /**
     * Sets the sequence of relocations that was followed to resolve this dependency's artifact.
     * 
     * @param relocations The sequence of relocations, may be {@code null}.
     */
    public void setRelocations( List<Artifact> relocations )
    {
        if ( relocations == null || relocations.isEmpty() )
        {
            this.relocations = Collections.emptyList();
        }
        else
        {
            this.relocations = relocations;
        }
    }

    public Collection<Artifact> getAliases()
    {
        return aliases;
    }

    /**
     * Sets the known aliases for this dependency's artifact.
     * 
     * @param aliases The known aliases, may be {@code null}.
     */
    public void setAliases( Collection<Artifact> aliases )
    {
        if ( aliases == null || aliases.isEmpty() )
        {
            this.aliases = Collections.emptyList();
        }
        else
        {
            this.aliases = aliases;
        }
    }

    public VersionConstraint getVersionConstraint()
    {
        return versionConstraint;
    }

    public void setVersionConstraint( VersionConstraint versionConstraint )
    {
        this.versionConstraint = versionConstraint;
    }

    public Version getVersion()
    {
        return version;
    }

    public void setVersion( Version version )
    {
        this.version = version;
    }

    public void setScope( String scope )
    {
        dependency = dependency.setScope( scope );
    }

    public String getPremanagedVersion()
    {
        return premanagedVersion;
    }

    /**
     * Sets the version or version range for this dependency before dependency management was applied (if any).
     * 
     * @param premanagedVersion The originally declared dependency version or {@code null} if the version was not
     *            managed.
     */
    public void setPremanagedVersion( String premanagedVersion )
    {
        this.premanagedVersion = premanagedVersion;
    }

    public String getPremanagedScope()
    {
        return premanagedScope;
    }

    /**
     * Sets the scope for this dependency before dependency management was applied (if any).
     * 
     * @param premanagedScope The originally declared dependency scope or {@code null} if the scope was not managed.
     */
    public void setPremanagedScope( String premanagedScope )
    {
        this.premanagedScope = premanagedScope;
    }

    public List<RemoteRepository> getRepositories()
    {
        return repositories;
    }

    /**
     * Sets the remote repositories from which this node's artifact shall be resolved.
     * 
     * @param repositories The remote repositories to use for artifact resolution, may be {@code null}.
     */
    public void setRepositories( List<RemoteRepository> repositories )
    {
        if ( repositories == null || repositories.isEmpty() )
        {
            this.repositories = Collections.emptyList();
        }
        else
        {
            this.repositories = repositories;
        }
    }

    public String getRequestContext()
    {
        return context;
    }

    public void setRequestContext( String context )
    {
        this.context = ( context != null ) ? context : "";
    }

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
        return String.valueOf( getDependency() );
    }

}
