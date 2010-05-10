package org.apache.maven.repository.util;

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

import org.apache.maven.repository.Dependency;
import org.apache.maven.repository.DependencyFilter;
import org.apache.maven.repository.DependencyNode;

/**
 * @author Benjamin Bentmann
 */
public class ScopeDependencyFilter
    implements DependencyFilter
{

    private final Collection<String> included = new HashSet<String>();

    private final Collection<String> excluded = new HashSet<String>();

    public ScopeDependencyFilter( Collection<String> included, Collection<String> excluded )
    {
        this.included.addAll( included );
        this.excluded.addAll( excluded );
    }

    public ScopeDependencyFilter( String... excluded )
    {
        this.excluded.addAll( Arrays.asList( excluded ) );
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
