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

import org.sonatype.maven.repository.Dependency;
import org.sonatype.maven.repository.DependencySelector;
import org.sonatype.maven.repository.DependencyNode;

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
