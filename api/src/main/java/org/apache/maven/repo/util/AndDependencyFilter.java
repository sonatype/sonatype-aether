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

import org.apache.maven.repo.Dependency;
import org.apache.maven.repo.DependencyFilter;
import org.apache.maven.repo.DependencyNode;

/**
 * @author Benjamin Bentmann
 */
public class AndDependencyFilter
    implements DependencyFilter
{

    private final DependencyFilter[] filters;

    public AndDependencyFilter( DependencyFilter... filters )
    {
        this.filters = ( filters != null ) ? filters : new DependencyFilter[0];
    }

    public boolean accept( DependencyNode node, Dependency dependency )
    {
        for ( DependencyFilter filter : filters )
        {
            if ( !filter.accept( node, dependency ) )
            {
                return false;
            }
        }
        return true;
    }

    public DependencyFilter deriveChildFilter( DependencyNode childNode )
    {
        DependencyFilter[] childFilters = new DependencyFilter[filters.length];

        boolean changed = false;
        for ( int i = filters.length - 1; i >= 0; i-- )
        {
            childFilters[i] = filters[i].deriveChildFilter( childNode );
            if ( childFilters[i] != filters[i] )
            {
                changed = true;
            }
        }

        return changed ? new AndDependencyFilter( childFilters ) : this;
    }

}
