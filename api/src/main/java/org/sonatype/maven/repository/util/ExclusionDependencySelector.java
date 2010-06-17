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
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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

    private final List<Exclusion> exclusions;

    /**
     * Creates a new selector without any exclusions.
     */
    public ExclusionDependencySelector()
    {
        this( Collections.<Exclusion> emptyList() );
    }

    /**
     * Creates a new selector with the specified exclusions.
     * 
     * @param exclusions The exclusions, may be {@code null}.
     */
    public ExclusionDependencySelector( List<Exclusion> exclusions )
    {
        this.exclusions = ( exclusions != null ) ? exclusions : Collections.<Exclusion> emptyList();
    }

    public boolean selectDependency( DependencyNode node, Dependency dependency )
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

    public DependencySelector deriveChildSelector( DependencyNode childNode )
    {
        Dependency dependency = childNode.getDependency();
        Collection<Exclusion> exclusions = ( dependency != null ) ? dependency.getExclusions() : null;
        if ( exclusions == null || exclusions.isEmpty() )
        {
            return this;
        }

        List<Exclusion> merged = new ArrayList<Exclusion>( this.exclusions.size() + exclusions.size() );
        merged.addAll( this.exclusions );
        merged.addAll( exclusions );

        return new ExclusionDependencySelector( merged );
    }

}
