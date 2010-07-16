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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import org.sonatype.aether.Dependency;
import org.sonatype.aether.DependencyFilter;
import org.sonatype.aether.DependencyNode;

/**
 * A dependency filter based on dependency scopes. <em>Note:</em> This filter does not assume any relationships between
 * the scopes. In particular, the filter is not aware of scopes that logically include other scopes.
 * 
 * @author Benjamin Bentmann
 * @see Dependency#getScope()
 */
public class ScopeDependencyFilter
    implements DependencyFilter
{

    private final Collection<String> included = new HashSet<String>();

    private final Collection<String> excluded = new HashSet<String>();

    /**
     * Creates a new filter using the specified includes and excludes.
     * 
     * @param included The set of scopes to include, may be {@code null} or empty to include any scope.
     * @param excluded The set of scopes to exclude, may be {@code null} or empty to exclude no scope.
     */
    public ScopeDependencyFilter( Collection<String> included, Collection<String> excluded )
    {
        if ( included != null )
        {
            this.included.addAll( included );
        }
        if ( excluded != null )
        {
            this.excluded.addAll( excluded );
        }
    }

    /**
     * Creates a new filter using the specified excludes.
     * 
     * @param excluded The set of scopes to exclude, may be {@code null} or empty to exclude no scope.
     */
    public ScopeDependencyFilter( String... excluded )
    {
        if ( excluded != null )
        {
            this.excluded.addAll( Arrays.asList( excluded ) );
        }
    }

    public boolean accept( DependencyNode node )
    {
        Dependency dependency = node.getDependency();

        if ( dependency == null )
        {
            return true;
        }

        String scope = dependency.getScope();
        return ( included.isEmpty() || included.contains( scope ) )
            && ( excluded.isEmpty() || !excluded.contains( scope ) );
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

        ScopeDependencyFilter that = (ScopeDependencyFilter) obj;

        return this.included.equals( that.included ) && this.excluded.equals( that.excluded );
    }

    @Override
    public int hashCode()
    {
        int hash = 17;
        hash = hash * 31 + included.hashCode();
        hash = hash * 31 + excluded.hashCode();
        return hash;
    }

}
