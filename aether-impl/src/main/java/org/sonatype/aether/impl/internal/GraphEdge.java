package org.sonatype.aether.impl.internal;

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
import java.util.Collections;
import java.util.List;

import org.sonatype.aether.Artifact;
import org.sonatype.aether.Dependency;
import org.sonatype.aether.DependencyNode;
import org.sonatype.aether.DependencyVisitor;
import org.sonatype.aether.RemoteRepository;
import org.sonatype.aether.Version;
import org.sonatype.aether.VersionConstraint;

/**
 * @author Benjamin Bentmann
 */
class GraphEdge
    implements DependencyNode
{

    private GraphNode source;

    private GraphNode target;

    private Dependency dependency;

    private String context;

    private String premanagedScope;

    private String premanagedVersion;

    private List<Artifact> relocations;

    private VersionConstraint versionConstraint;

    private Version version;

    public GraphEdge( GraphNode source, GraphNode target )
    {
        this.source = source;
        this.target = target;
    }

    public GraphNode getSource()
    {
        return source;
    }

    public GraphNode getTarget()
    {
        return target;
    }

    public List<DependencyNode> getChildren()
    {
        return getTarget().getOutgoingEdges();
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
        this.dependency = dependency.setArtifact( artifact );
    }

    public List<RemoteRepository> getRepositories()
    {
        return getTarget().getRepositories();
    }

    public void setScope( String scope )
    {
        this.dependency = dependency.setScope( scope );
    }

    public String getPremanagedScope()
    {
        return premanagedScope;
    }

    public void setPremanagedScope( String premanagedScope )
    {
        this.premanagedScope = premanagedScope;
    }

    public String getPremanagedVersion()
    {
        return premanagedVersion;
    }

    public void setPremanagedVersion( String premanagedVersion )
    {
        this.premanagedVersion = premanagedVersion;
    }

    public String getRequestContext()
    {
        return context;
    }

    public void setRequestContext( String context )
    {
        this.context = ( context != null ) ? context : "";
    }

    public List<Artifact> getRelocations()
    {
        return relocations;
    }

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
        return getTarget().getAliases();
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
