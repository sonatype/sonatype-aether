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

import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.graph.DependencyVisitor;
import org.sonatype.aether.resolution.ArtifactRequest;

/**
 * @author Benjamin Bentmann
 */
class ArtifactRequestBuilder
    implements DependencyVisitor
{

    private List<ArtifactRequest> requests;

    public ArtifactRequestBuilder()
    {
        this.requests = new ArrayList<ArtifactRequest>();
    }

    public List<ArtifactRequest> getRequests()
    {
        return requests;
    }

    public boolean visitEnter( DependencyNode node )
    {
        if ( node.getDependency() != null )
        {
            ArtifactRequest request = new ArtifactRequest( node );
            requests.add( request );
        }

        return true;
    }

    public boolean visitLeave( DependencyNode node )
    {
        return true;
    }

}
