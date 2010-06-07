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

import java.util.Collection;

import org.sonatype.maven.repository.DependencyFilter;
import org.sonatype.maven.repository.DependencyNode;

/**
 * A dependency filter that combines zero or more other filters using a logical {@code AND}.
 * 
 * @author Benjamin Bentmann
 */
public class AndDependencyFilter
    implements DependencyFilter
{

    private final DependencyFilter[] filters;

    /**
     * Creates a new filter from the specified filters.
     * 
     * @param filters The filters to combine, may be {@code null}.
     */
    public AndDependencyFilter( DependencyFilter... filters )
    {
        this.filters = ( filters != null ) ? filters : new DependencyFilter[0];
    }

    /**
     * Creates a new filter from the specified filters.
     * 
     * @param filters The filters to combine, may be {@code null}.
     */
    public AndDependencyFilter( Collection<DependencyFilter> filters )
    {
        if ( filters != null )
        {
            this.filters = filters.toArray( new DependencyFilter[filters.size()] );
        }
        else
        {
            this.filters = new DependencyFilter[0];
        }
    }

    public boolean filterDependency( DependencyNode node )
    {
        for ( DependencyFilter filter : filters )
        {
            if ( !filter.filterDependency( node ) )
            {
                return false;
            }
        }
        return true;
    }

}
