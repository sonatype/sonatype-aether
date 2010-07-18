package org.sonatype.aether.util.graph.selector;

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

import org.sonatype.aether.Dependency;
import org.sonatype.aether.DependencyNode;
import org.sonatype.aether.DependencySelector;

/**
 * A dependency selector that excludes optional dependencies which occur beyond level one of the dependency graph.
 * 
 * @author Benjamin Bentmann
 * @see Dependency#isOptional()
 */
public class OptionalDependencySelector
    implements DependencySelector
{

    private final boolean transitive;

    /**
     * Creates a new selector to exclude optional transitive dependencies.
     */
    public OptionalDependencySelector()
    {
        transitive = false;
    }

    private OptionalDependencySelector( boolean transitive )
    {
        this.transitive = transitive;
    }

    public boolean selectDependency( Dependency dependency )
    {
        return !transitive || !dependency.isOptional();
    }

    public DependencySelector deriveChildSelector( DependencyNode node )
    {
        boolean transitive = node.getDepth() > 0;

        if ( transitive == this.transitive )
        {
            return this;
        }

        return new OptionalDependencySelector( transitive );
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

        OptionalDependencySelector that = (OptionalDependencySelector) obj;
        return transitive == that.transitive;
    }

    @Override
    public int hashCode()
    {
        int hash = getClass().hashCode();
        hash = hash * 31 + ( transitive ? 1 : 0 );
        return hash;
    }

}
