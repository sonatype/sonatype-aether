package org.apache.maven.repository;

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

/**
 * @author Benjamin Bentmann
 */
public class DependencyNode
{

    private Dependency dependency;

    private List<Artifact> relocations = Collections.emptyList();

    private DependencyNode parent;

    private List<DependencyNode> children = new ArrayList<DependencyNode>( 4 );

    private int depth;

    private String requestedVersion;

    private List<RemoteRepository> repositories = Collections.emptyList();

    public DependencyNode( Dependency dependency, DependencyNode parent )
    {
        this.dependency = dependency;
        this.parent = parent;
        this.depth = ( parent != null ) ? parent.getDepth() + 1 : 0;
    }

    public Dependency getDependency()
    {
        return dependency;
    }

    public List<Artifact> getRelocations()
    {
        return relocations;
    }

    public DependencyNode setRelocations( List<Artifact> relocations )
    {
        this.relocations = relocations;
        return this;
    }

    public DependencyNode getParent()
    {
        return parent;
    }

    public List<DependencyNode> getChildren()
    {
        return children;
    }

    public DependencyNode addChild( Dependency dependency )
    {
        DependencyNode child = new DependencyNode( dependency, this );
        children.add( child );
        return child;
    }

    public int getDepth()
    {
        return depth;
    }

    public String getRequestedVersion()
    {
        return requestedVersion;
    }

    public DependencyNode setRequestedVersion( String requestedVersion )
    {
        this.requestedVersion = ( requestedVersion != null ) ? requestedVersion : "";
        return this;
    }

    public List<RemoteRepository> getRepositories()
    {
        return repositories;
    }

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
