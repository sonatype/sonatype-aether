package org.sonatype.aether.util.filter;

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

import java.util.List;

import org.sonatype.aether.graph.DependencyFilter;
import org.sonatype.aether.graph.DependencyNode;

/**
 * A dependency filter that negates another filters.
 * 
 * @author Benjamin Bentmann
 */
public class NotDependencyFilter
    implements DependencyFilter
{

    private final DependencyFilter filter;

    /**
     * Creates a new filter negatint the specified filter.
     * 
     * @param filter The filter to negate, must not be {@code null}.
     */
    public NotDependencyFilter( DependencyFilter filter )
    {
        if ( filter == null )
        {
            throw new IllegalArgumentException( "no filter specified" );
        }
        this.filter = filter;
    }

    public boolean accept( DependencyNode node, List<DependencyNode> parents )
    {
        return !filter.accept( node, parents );
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

        NotDependencyFilter that = (NotDependencyFilter) obj;

        return this.filter.equals( that.filter );
    }

    @Override
    public int hashCode()
    {
        int hash = getClass().hashCode();
        hash = hash * 31 + filter.hashCode();
        return hash;
    }

}
