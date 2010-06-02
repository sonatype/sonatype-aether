package org.apache.maven.repo.util;

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

import org.apache.maven.repo.Artifact;
import org.apache.maven.repo.Dependency;
import org.apache.maven.repo.DependencyFilter;
import org.apache.maven.repo.DependencyNode;
import org.apache.maven.repo.Exclusion;

/**
 * @author Benjamin Bentmann
 */
public class ExclusionDependencyFilter
    implements DependencyFilter
{

    private final List<Exclusion> exclusions;

    public ExclusionDependencyFilter()
    {
        this( Collections.<Exclusion> emptyList() );
    }

    public ExclusionDependencyFilter( List<Exclusion> exclusions )
    {
        this.exclusions = ( exclusions != null ) ? exclusions : Collections.<Exclusion> emptyList();
    }

    public boolean accept( DependencyNode node, Dependency dependency )
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
        if ( !matches( exclusion.getType(), artifact.getType() ) )
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

    public DependencyFilter deriveChildFilter( DependencyNode childNode )
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

        return new ExclusionDependencyFilter( merged );
    }

}
