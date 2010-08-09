package org.sonatype.aether.util.graph.traverser;

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

import org.sonatype.aether.Dependency;
import org.sonatype.aether.DependencyNode;
import org.sonatype.aether.DependencyTraverser;
import org.sonatype.aether.util.ArtifactProperties;

/**
 * A dependency traverser that excludes fat artifacts from the traversal. Fat artifacts are artifacts that have the
 * property "includesDependencies" set to {@code true}.
 * 
 * @author Benjamin Bentmann
 * @see org.sonatype.aether.Artifact#getProperties()
 */
public class FatArtifactTraverser
    implements DependencyTraverser
{

    public boolean traverseDependency( Dependency dependency )
    {
        return !Boolean.valueOf( dependency.getArtifact().getProperty( ArtifactProperties.INCLUDES_DEPENDENCIES, "" ) );
    }

    public DependencyTraverser deriveChildTraverser( DependencyNode node )
    {
        return this;
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj )
        {
            return true;
        }
        else if ( null == obj || !getClass().equals( obj.getClass() ) )
        {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        return getClass().hashCode();
    }

}
