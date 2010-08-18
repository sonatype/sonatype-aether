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
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.sonatype.aether.Artifact;
import org.sonatype.aether.DependencyNode;
import org.sonatype.aether.RemoteRepository;

/**
 * @author Benjamin Bentmann
 */
class GraphNode
{

    private List<DependencyNode> outgoingEdges = new ArrayList<DependencyNode>( 0 );

    private Collection<Artifact> aliases = Collections.emptyList();

    private List<RemoteRepository> repositories = Collections.emptyList();

    public List<DependencyNode> getOutgoingEdges()
    {
        return outgoingEdges;
    }

    public List<RemoteRepository> getRepositories()
    {
        return repositories;
    }

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

    public Collection<Artifact> getAliases()
    {
        return aliases;
    }

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

    @Override
    public String toString()
    {
        return String.valueOf( getOutgoingEdges() );
    }

}
