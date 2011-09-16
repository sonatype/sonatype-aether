package org.sonatype.aether.util.graph.traverser;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import org.sonatype.aether.collection.DependencyCollectionContext;
import org.sonatype.aether.collection.DependencyTraverser;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.util.artifact.ArtifactProperties;

/**
 * A dependency traverser that excludes the dependencies of fat artifacts from the traversal. Fat artifacts are
 * artifacts that have the property {@link org.sonatype.aether.util.artifact.ArtifactProperties#INCLUDES_DEPENDENCIES}
 * set to {@code true}.
 * 
 * @author Benjamin Bentmann
 * @see org.sonatype.aether.artifact.Artifact#getProperties()
 */
public class FatArtifactTraverser
    implements DependencyTraverser
{

    public boolean traverseDependency( Dependency dependency )
    {
        String prop = dependency.getArtifact().getProperty( ArtifactProperties.INCLUDES_DEPENDENCIES, "" );
        return !Boolean.parseBoolean( prop );
    }

    public DependencyTraverser deriveChildTraverser( DependencyCollectionContext context )
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
