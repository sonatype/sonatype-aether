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
 * A dependency selector with always includes or excludes dependencies.
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

    public boolean selectDependency( DependencyNode node, Dependency dependency )
    {
        return select;
    }

    public DependencySelector deriveChildSelector( DependencyNode childNode )
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
