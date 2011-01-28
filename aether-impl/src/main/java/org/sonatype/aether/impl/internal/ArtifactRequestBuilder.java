package org.sonatype.aether.impl.internal;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *******************************************************************************/

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
