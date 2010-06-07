package org.sonatype.maven.repository.util;

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

import org.sonatype.maven.repository.Artifact;
import org.sonatype.maven.repository.DependencyFilter;
import org.sonatype.maven.repository.DependencyNode;
import org.sonatype.maven.repository.DependencyVisitor;

/**
 * Generates a sequence of artifacts from a dependeny graph by traversing the graph in pre-order.
 * 
 * @author Benjamin Bentmann
 */
public class ArtifactListGenerator
    implements DependencyVisitor
{

    private DependencyFilter filter;

    private List<Artifact> artifacts;

    /**
     * Creates a new list generator.
     */
    public ArtifactListGenerator()
    {
        this( null );
    }

    /**
     * Generates a new list generator with the specified filter.
     * 
     * @param filter The dependency filter to use, may be {@code null} to include all artifacts.
     */
    public ArtifactListGenerator( DependencyFilter filter )
    {
        this.filter = filter;
        this.artifacts = new ArrayList<Artifact>();
    }

    /**
     * Gets the list of artifacts that was generated during the graph traversal.
     * 
     * @return The list of artifacts in preorder, never {@code null}.
     */
    public List<Artifact> getArtifacts()
    {
        return artifacts;
    }

    public boolean visitEnter( DependencyNode node )
    {
        if ( node.getDependency() != null && ( filter == null || filter.filterDependency( node ) ) )
        {
            artifacts.add( node.getDependency().getArtifact() );
        }

        return true;
    }

    public boolean visitLeave( DependencyNode node )
    {
        return true;
    }

}
