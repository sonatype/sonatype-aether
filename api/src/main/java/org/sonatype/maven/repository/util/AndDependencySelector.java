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
import java.util.Set;

import org.sonatype.maven.repository.Dependency;
import org.sonatype.maven.repository.DependencySelector;
import org.sonatype.maven.repository.DependencyNode;

/**
 * A dependency selector that combines zero or more other selectors using a logical {@code AND}.
 * 
 * @author Benjamin Bentmann
 */
public class AndDependencySelector
    implements DependencySelector
{

    private final Collection<DependencySelector> selectors;

    /**
     * Creates a new selector from the specified selectors.
     * 
     * @param selectors The selectors to combine, may be {@code null}.
     */
    public AndDependencySelector( DependencySelector... selectors )
    {
        if ( selectors != null && selectors.length > 0 )
        {
            this.selectors = new LinkedHashSet<DependencySelector>();
            Collections.addAll( this.selectors, selectors );
        }
        else
        {
            this.selectors = Collections.emptySet();
        }
    }

    /**
     * Creates a new selector from the specified selectors.
     * 
     * @param selectors The selectors to combine, may be {@code null}.
     */
    public AndDependencySelector( Set<DependencySelector> selectors )
    {
        if ( selectors != null && !selectors.isEmpty() )
        {
            this.selectors = selectors;
        }
        else
        {
            this.selectors = Collections.emptySet();
        }
    }

    /**
     * Creates a new selector from the specified selectors.
     * 
     * @param selector1 The first selector to combine, may be {@code null}.
     * @param selector2 The first selector to combine, may be {@code null}.
     * @return The combined selector or {@code null} if both selectors were {@code null}.
     */
    public static DependencySelector newInstance( DependencySelector selector1, DependencySelector selector2 )
    {
        if ( selector1 == null )
        {
            return selector2;
        }
        else if ( selector2 == null )
        {
            return selector1;
        }
        return new AndDependencySelector( selector1, selector2 );
    }

    public boolean selectDependency( Dependency dependency )
    {
        for ( DependencySelector selector : selectors )
        {
            if ( !selector.selectDependency( dependency ) )
            {
                return false;
            }
        }
        return true;
    }

    public DependencySelector deriveChildSelector( DependencyNode node )
    {
        Set<DependencySelector> childSelectors = new LinkedHashSet<DependencySelector>();

        for ( DependencySelector selector : selectors )
        {
            childSelectors.add( selector.deriveChildSelector( node ) );
        }

        return !childSelectors.equals( selectors ) ? new AndDependencySelector( childSelectors ) : this;
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

        AndDependencySelector that = (AndDependencySelector) obj;
        return selectors.equals( that.selectors );
    }

    @Override
    public int hashCode()
    {
        int hash = getClass().hashCode();
        hash = hash * 31 + selectors.hashCode();
        return hash;
    }

}
