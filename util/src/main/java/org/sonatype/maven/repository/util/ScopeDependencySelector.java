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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import org.sonatype.maven.repository.Dependency;
import org.sonatype.maven.repository.DependencySelector;
import org.sonatype.maven.repository.DependencyNode;

/**
 * A dependency selector that filters transitive dependencies based on their scope. Direct dependencies are always
 * included regardless of their scope. <em>Note:</em> This filter does not assume any relationships between the scopes.
 * In particular, the filter is not aware of scopes that logically include other scopes.
 * 
 * @author Benjamin Bentmann
 * @see Dependency#getScope()
 */
public class ScopeDependencySelector
    implements DependencySelector
{

    private final boolean transitive;

    private final Collection<String> included;

    private final Collection<String> excluded;

    /**
     * Creates a new selector using the specified includes and excludes.
     * 
     * @param included The set of scopes to include, may be {@code null} or empty to include any scope.
     * @param excluded The set of scopes to exclude, may be {@code null} or empty to exclude no scope.
     */
    public ScopeDependencySelector( Collection<String> included, Collection<String> excluded )
    {
        transitive = false;
        if ( included != null )
        {
            this.included = new HashSet<String>();
            this.included.addAll( included );
        }
        else
        {
            this.included = Collections.emptySet();
        }
        if ( excluded != null )
        {
            this.excluded = new HashSet<String>();
            this.excluded.addAll( excluded );
        }
        else
        {
            this.excluded = Collections.emptySet();
        }
    }

    /**
     * Creates a new selector using the specified excludes.
     * 
     * @param excluded The set of scopes to exclude, may be {@code null} or empty to exclude no scope.
     */
    public ScopeDependencySelector( String... excluded )
    {
        this( null, Arrays.asList( excluded ) );
    }

    private ScopeDependencySelector( boolean transitive, Collection<String> included, Collection<String> excluded )
    {
        this.transitive = transitive;
        this.included = included;
        this.excluded = excluded;
    }

    public boolean selectDependency( Dependency dependency )
    {
        if ( !transitive )
        {
            return true;
        }

        String scope = dependency.getScope();
        return ( included.isEmpty() || included.contains( scope ) )
            && ( excluded.isEmpty() || !excluded.contains( scope ) );
    }

    public DependencySelector deriveChildSelector( DependencyNode node )
    {
        boolean transitive = node.getDependency() != null;

        if ( transitive == this.transitive )
        {
            return this;
        }

        return new ScopeDependencySelector( transitive, included, excluded );
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

        ScopeDependencySelector that = (ScopeDependencySelector) obj;
        return transitive == that.transitive && included.equals( that.included ) && excluded.equals( that.excluded );
    }

    @Override
    public int hashCode()
    {
        int hash = 17;
        hash = hash * 31 + ( transitive ? 1 : 0 );
        hash = hash * 31 + included.hashCode();
        hash = hash * 31 + excluded.hashCode();
        return hash;
    }

}
