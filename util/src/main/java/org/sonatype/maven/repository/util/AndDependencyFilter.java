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
import java.util.Collections;
import java.util.LinkedHashSet;

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

    private final Collection<DependencyFilter> filters = new LinkedHashSet<DependencyFilter>();

    /**
     * Creates a new filter from the specified filters.
     * 
     * @param filters The filters to combine, may be {@code null}.
     */
    public AndDependencyFilter( DependencyFilter... filters )
    {
        if ( filters != null )
        {
            Collections.addAll( this.filters, filters );
        }
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
            this.filters.addAll( filters );
        }
    }

    /**
     * Creates a new filter from the specified filters.
     * 
     * @param filter1 The first filter to combine, may be {@code null}.
     * @param filter2 The first filter to combine, may be {@code null}.
     * @return The combined filter or {@code null} if both filter were {@code null}.
     */
    public static DependencyFilter newInstance( DependencyFilter filter1, DependencyFilter filter2 )
    {
        if ( filter1 == null )
        {
            return filter2;
        }
        else if ( filter2 == null )
        {
            return filter1;
        }
        return new AndDependencyFilter( filter1, filter2 );
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

        AndDependencyFilter that = (AndDependencyFilter) obj;

        return this.filters.equals( that.filters );
    }

    @Override
    public int hashCode()
    {
        int hash = 17;
        hash = hash * 31 + filters.hashCode();
        return hash;
    }

}
