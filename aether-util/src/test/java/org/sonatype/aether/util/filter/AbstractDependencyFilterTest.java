package org.sonatype.aether.util.filter;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *******************************************************************************/

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
