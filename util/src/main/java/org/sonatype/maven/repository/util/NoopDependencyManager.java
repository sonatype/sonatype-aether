package org.sonatype.maven.repository.util;

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

import org.sonatype.maven.repository.Dependency;
import org.sonatype.maven.repository.DependencyManagement;
import org.sonatype.maven.repository.DependencyManager;
import org.sonatype.maven.repository.DependencyNode;

/**
 * @author Benjamin Bentmann
 */
public class NoopDependencyManager
    implements DependencyManager
{

    public static final DependencyManager INSTANCE = new NoopDependencyManager();

    public DependencyManager deriveChildManager( DependencyNode node, List<? extends Dependency> managedDependencies )
    {
        return this;
    }

    public DependencyManagement manageDependency( Dependency dependency )
    {
        return null;
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
        return true;
    }

    @Override
    public int hashCode()
    {
        return getClass().hashCode();
    }

}
