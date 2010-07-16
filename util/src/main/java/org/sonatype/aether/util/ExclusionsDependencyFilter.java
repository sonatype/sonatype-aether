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

import java.util.Collection;
import java.util.HashSet;

import org.sonatype.aether.Dependency;
import org.sonatype.aether.DependencyFilter;
import org.sonatype.aether.DependencyNode;

/**
 * A simple filter to exclude artifacts based either artifact id or group id and artifact id.
 * 
 * @author Benjamin Bentmann
 */
public class ExclusionsDependencyFilter
    implements DependencyFilter
{

    private final Collection<String> excludes = new HashSet<String>();

    /**
     * Creates a new filter using the specified exclude patterns. A pattern can either be of the form {@code
     * groupId:artifactId} (recommended) or just {@code artifactId}.
     * 
     * @param excludes The exclude patterns, may be {@code null} or empty to exclude no artifacts.
     */
    public ExclusionsDependencyFilter( Collection<String> excludes )
    {
        if ( excludes != null )
        {
            this.excludes.addAll( excludes );
        }
    }

    public boolean accept( DependencyNode node )
    {
        Dependency dependency = node.getDependency();

        if ( dependency == null )
        {
            return true;
        }

        String id = dependency.getArtifact().getArtifactId();

        if ( excludes.contains( id ) )
        {
            return false;
        }

        id = dependency.getArtifact().getGroupId() + ':' + id;

        if ( excludes.contains( id ) )
        {
            return false;
        }

        return true;
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj )
        {
            return true;
        }

        if ( obj == null || !getClass().equals( obj.getClass() ) )
        {
            return false;
        }

        ExclusionsDependencyFilter that = (ExclusionsDependencyFilter) obj;

        return this.excludes.equals( that.excludes );
    }

    @Override
    public int hashCode()
    {
        int hash = 17;
        hash = hash * 31 + excludes.hashCode();
        return hash;
    }

}
