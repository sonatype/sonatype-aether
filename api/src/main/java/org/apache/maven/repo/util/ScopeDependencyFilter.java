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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import org.apache.maven.repo.Dependency;
import org.apache.maven.repo.DependencyFilter;
import org.apache.maven.repo.DependencyNode;

/**
 * A dependency filter based on dependency scopes.
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

    public boolean accept( DependencyNode node, Dependency dependency )
    {
        if ( node.getDependency() == null )
        {
            return true;
        }

        String scope = dependency.getScope();
        return ( included.isEmpty() || included.contains( scope ) )
            && ( excluded.isEmpty() || !excluded.contains( scope ) );
    }

    public DependencyFilter deriveChildFilter( DependencyNode childNode )
    {
        return this;
    }

}
