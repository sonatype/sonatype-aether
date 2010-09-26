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

public abstract class AbstractDependencyFilterTest
{

    protected DependencyFilter getAcceptFilter()
    {
        return new DependencyFilter()
        {

            public boolean accept( DependencyNode node, List<DependencyNode> parents )
            {
                return true;
            }

        };
    }

    protected DependencyFilter getDenyFilter()
    {
        return new DependencyFilter()
        {

            public boolean accept( DependencyNode node, List<DependencyNode> parents )
            {
                return false;
            }
        };
    }


}
