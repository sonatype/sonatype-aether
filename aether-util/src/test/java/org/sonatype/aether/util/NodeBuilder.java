package org.sonatype.aether.util;

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
import org.sonatype.aether.DefaultArtifact;
import org.sonatype.aether.Dependency;
import org.sonatype.aether.DependencyNode;
import org.sonatype.aether.InvalidVersionSpecificationException;
import org.sonatype.aether.VersionScheme;
import org.sonatype.aether.util.graph.DefaultDependencyNode;
import org.sonatype.aether.util.version.GenericVersionScheme;

/**
 * @author Benjamin Bentmann
 */
public class NodeBuilder
{

    private String groupId = "test";

    private String artifactId = "";

    private String version = "0.1";

    private String range;

    private String ext = "jar";

    private String classifier = "";

    private String scope = "compile";

    private boolean optional = false;

    private String context;

    private List<Artifact> relocations = new ArrayList<Artifact>();

    private VersionScheme versionScheme = new GenericVersionScheme();

    public NodeBuilder artifactId( String artifactId )
    {
        this.artifactId = artifactId;
        return this;
    }

    public NodeBuilder groupId( String groupId )
    {
        this.groupId = groupId;
        return this;

    }

    public NodeBuilder ext( String ext )
    {
        this.ext = ext;
        return this;
    }

    public NodeBuilder version( String version )
    {
        this.version = version;
        this.range = null;
        return this;
    }

    public NodeBuilder range( String range )
    {
        this.range = range;
        return this;
    }

    public NodeBuilder scope( String scope )
    {
        this.scope = scope;
        return this;
    }

    public NodeBuilder context( String context )
    {
        this.context = context;
        return this;
    }

    public NodeBuilder reloc( String artifactId )
    {
        Artifact relocation = new DefaultArtifact( groupId, artifactId, classifier, ext, version );
        relocations.add( relocation );
        return this;
    }

    public NodeBuilder reloc( String groupId, String artifactId, String version )
    {
        Artifact relocation = new DefaultArtifact( groupId, artifactId, classifier, ext, version );
        relocations.add( relocation );
        return this;
    }

    public DependencyNode build()
    {
        Dependency dependency = null;
        DefaultDependencyNode node = new DefaultDependencyNode();
        if ( artifactId != null && artifactId.length() > 0 )
        {
            Artifact artifact = new DefaultArtifact( groupId, artifactId, classifier, ext, version );
            dependency = new Dependency( artifact, scope, optional );
            node.setDependency( dependency );
            try
            {
                node.setVersion( versionScheme.parseVersion( version ) );
                node.setVersionConstraint( versionScheme.parseVersionConstraint( range != null ? range : version ) );
            }
            catch ( InvalidVersionSpecificationException e )
            {
                throw new IllegalArgumentException( "bad version: " + e.getMessage(), e );
            }
        }
        node.setRequestContext( context );
        node.setRelocations( relocations );
        return node;
    }

}
