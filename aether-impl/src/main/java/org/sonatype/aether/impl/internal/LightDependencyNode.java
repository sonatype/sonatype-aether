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

import java.util.ArrayList;
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
final class LightDependencyNode
    implements DependencyNode
{

    private DependencyNode parent;

    private List<DependencyNode> children = new ArrayList<DependencyNode>( 0 );

    private int depth;

    private Object conflictId;

    private DependencyNodeInfo info;

    private boolean exclusive;

    public LightDependencyNode( DependencyNodeInfo info, DependencyNode parent )
    {
        this.info = info;
        this.parent = parent;
        this.depth = ( parent != null ) ? parent.getDepth() + 1 : 0;
    }

    public DependencyNodeInfo getInfo()
    {
        return info;
    }

    public DependencyNode getParent()
    {
        return parent;
    }

    public int getDepth()
    {
        return depth;
    }

    public List<DependencyNode> getChildren()
    {
        return children;
    }

    public List<Artifact> getAliases()
    {
        return info.getAliases();
    }

    public Object getConflictId()
    {
        return conflictId;
    }

    public String getContext()
    {
        return info.getContext();
    }

    public Dependency getDependency()
    {
        return info.getDependency();
    }

    public String getPremanagedScope()
    {
        return info.getPremanagedScope();
    }

    public String getPremanagedVersion()
    {
        return info.getPremanagedVersion();
    }

    public List<Artifact> getRelocations()
    {
        return info.getRelocations();
    }

    public List<RemoteRepository> getRepositories()
    {
        return info.getRepositories();
    }

    public Version getVersion()
    {
        return info.getVersion();
    }

    public VersionConstraint getVersionConstraint()
    {
        return info.getVersionConstraint();
    }

    public DependencyNode setArtifact( Artifact artifact )
    {
        unshare();
        info.setArtifact( artifact );
        return this;
    }

    public DependencyNode setConflictId( Object conflictId )
    {
        this.conflictId = conflictId;
        return this;
    }

    public DependencyNode setContext( String context )
    {
        unshare();
        info.setContext( context );
        return this;
    }

    public DependencyNode setRepositories( List<RemoteRepository> repositories )
    {
        unshare();
        info.setRepositories( repositories );
        return this;
    }

    public DependencyNode setScope( String scope )
    {
        unshare();
        info.setScope( scope );
        return this;
    }

    public DependencyNode setVersionConstraint( VersionConstraint versionConstraint )
    {
        unshare();
        info.setVersionConstraint( versionConstraint );
        return this;
    }

    private void unshare()
    {
        if ( !exclusive )
        {
            info = new DependencyNodeInfo( info );
            exclusive = true;
        }
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
