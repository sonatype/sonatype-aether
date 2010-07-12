package org.sonatype.maven.repository.util;

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
import java.util.LinkedHashSet;
import java.util.Set;

import org.sonatype.maven.repository.Artifact;
import org.sonatype.maven.repository.Dependency;
import org.sonatype.maven.repository.DependencySelector;
import org.sonatype.maven.repository.DependencyNode;
import org.sonatype.maven.repository.Exclusion;

/**
 * A dependency selector that applies exclusions based on artifact coordinates.
 * 
 * @author Benjamin Bentmann
 * @see Dependency#getExclusions()
 */
public class ExclusionDependencySelector
    implements DependencySelector
{

    private final Collection<Exclusion> exclusions;

    /**
     * Creates a new selector without any exclusions.
     */
    public ExclusionDependencySelector()
    {
        this( Collections.<Exclusion> emptySet() );
    }

    /**
     * Creates a new selector with the specified exclusions.
     * 
     * @param exclusions The exclusions, may be {@code null}.
     */
    public ExclusionDependencySelector( Set<Exclusion> exclusions )
    {
        if ( exclusions != null && !exclusions.isEmpty() )
        {
            this.exclusions = exclusions;
        }
        else
        {
            this.exclusions = Collections.emptySet();
        }
    }

    public boolean selectDependency( Dependency dependency )
    {
        Artifact artifact = dependency.getArtifact();
        for ( Exclusion exclusion : exclusions )
        {
            if ( matches( exclusion, artifact ) )
            {
                return false;
            }
        }
        return true;
    }

    private boolean matches( Exclusion exclusion, Artifact artifact )
    {
        if ( !matches( exclusion.getArtifactId(), artifact.getArtifactId() ) )
        {
            return false;
        }
        if ( !matches( exclusion.getGroupId(), artifact.getGroupId() ) )
        {
            return false;
        }
        if ( !matches( exclusion.getType(), artifact.getExtension() ) )
        {
            return false;
        }
        if ( !matches( exclusion.getClassifier(), artifact.getClassifier() ) )
        {
            return false;
        }
        return true;
    }

    private boolean matches( String pattern, String value )
    {
        return "*".equals( pattern ) || pattern.equals( value );
    }

    public DependencySelector deriveChildSelector( DependencyNode node )
    {
        Dependency dependency = node.getDependency();
        Collection<Exclusion> exclusions = ( dependency != null ) ? dependency.getExclusions() : null;
        if ( exclusions == null || exclusions.isEmpty() )
        {
            return this;
        }

        Set<Exclusion> merged = new LinkedHashSet<Exclusion>();
        merged.addAll( this.exclusions );
        merged.addAll( exclusions );

        return new ExclusionDependencySelector( merged );
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

        ExclusionDependencySelector that = (ExclusionDependencySelector) obj;
        return exclusions.equals( that.exclusions );
    }

    @Override
    public int hashCode()
    {
        int hash = getClass().hashCode();
        hash = hash * 31 + exclusions.hashCode();
        return hash;
    }

}
