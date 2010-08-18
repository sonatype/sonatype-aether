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
import org.sonatype.aether.DependencyCollectionContext;
import org.sonatype.aether.DependencySelector;

/**
 * A dependency selector that always includes or excludes dependencies.
 * 
 * @author Benjamin Bentmann
 */
public class StaticDependencySelector
    implements DependencySelector
{

    private final boolean select;

    /**
     * Creates a new selector with the specified selection behavior.
     * 
     * @param select {@code true} to select all dependencies, {@code false} to exclude all dependencies.
     */
    public StaticDependencySelector( boolean select )
    {
        this.select = select;
    }

    public boolean selectDependency( Dependency dependency )
    {
        return select;
    }

    public DependencySelector deriveChildSelector( DependencyCollectionContext context )
    {
        return this;
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

        StaticDependencySelector that = (StaticDependencySelector) obj;
        return select == that.select;
    }

    @Override
    public int hashCode()
    {
        int hash = getClass().hashCode();
        hash = hash * 31 + ( select ? 1 : 0 );
        return hash;
    }

}
