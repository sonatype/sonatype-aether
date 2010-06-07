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

    private final DependencySelector[] selectors;

    /**
     * Creates a new selector from the specified selectors.
     * 
     * @param selectors The selectors to combine, may be {@code null}.
     */
    public AndDependencySelector( DependencySelector... selectors )
    {
        this.selectors = ( selectors != null ) ? selectors : new DependencySelector[0];
    }

    /**
     * Creates a new selector from the specified selectors.
     * 
     * @param selectors The selectors to combine, may be {@code null}.
     */
    public AndDependencySelector( Collection<DependencySelector> selectors )
    {
        if ( selectors != null )
        {
            this.selectors = selectors.toArray( new DependencySelector[selectors.size()] );
        }
        else
        {
            this.selectors = new DependencySelector[0];
        }
    }

    public boolean selectDependency( DependencyNode node, Dependency dependency )
    {
        for ( DependencySelector selector : selectors )
        {
            if ( !selector.selectDependency( node, dependency ) )
            {
                return false;
            }
        }
        return true;
    }

    public DependencySelector deriveChildSelector( DependencyNode childNode )
    {
        DependencySelector[] childSelectors = new DependencySelector[selectors.length];

        boolean changed = false;
        for ( int i = selectors.length - 1; i >= 0; i-- )
        {
            childSelectors[i] = selectors[i].deriveChildSelector( childNode );
            if ( childSelectors[i] != selectors[i] )
            {
                changed = true;
            }
        }

        return changed ? new AndDependencySelector( childSelectors ) : this;
    }

}
