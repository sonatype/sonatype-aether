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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.sonatype.aether.DependencyFilter;
import org.sonatype.aether.DependencyNode;
import org.sonatype.aether.util.NodeBuilder;

public class OrDependencyFilterTest
    extends AbstractDependencyFilterTest
{

    @Test
    public void acceptTest()
    {
        NodeBuilder builder = new NodeBuilder();
        List<DependencyNode> parents = new LinkedList<DependencyNode>();
        // Empty OR
        assertFalse( new OrDependencyFilter().accept( builder.build(), parents ) );

        // Basic Boolean Input
        assertTrue( new OrDependencyFilter( getAcceptFilter() ).accept( builder.build(), parents ) );
        assertFalse( new OrDependencyFilter( getDenyFilter() ).accept( builder.build(), parents ) );

        assertFalse( new OrDependencyFilter( getDenyFilter(), getDenyFilter() ).accept( builder.build(), parents ) );
        assertTrue( new OrDependencyFilter( getDenyFilter(), getAcceptFilter() ).accept( builder.build(), parents ) );
        assertTrue( new OrDependencyFilter( getAcceptFilter(), getDenyFilter() ).accept( builder.build(), parents ) );
        assertTrue( new OrDependencyFilter( getAcceptFilter(), getAcceptFilter() ).accept( builder.build(), parents ) );

        assertFalse( new OrDependencyFilter( getDenyFilter(), getDenyFilter(), getDenyFilter() ).accept( builder.build(),
                                                                                                         parents ) );
        assertTrue( new OrDependencyFilter( getAcceptFilter(), getDenyFilter(), getDenyFilter() ).accept( builder.build(),
                                                                                                          parents ) );
        assertTrue( new OrDependencyFilter( getAcceptFilter(), getAcceptFilter(), getDenyFilter() ).accept( builder.build(),
                                                                                                            parents ) );
        assertTrue( new OrDependencyFilter( getAcceptFilter(), getAcceptFilter(), getAcceptFilter() ).accept( builder.build(),
                                                                                                              parents ) );

        // User another constructor
        Collection<DependencyFilter> filters = new LinkedList<DependencyFilter>();
        filters.add( getDenyFilter() );
        filters.add( getAcceptFilter() );
        assertTrue( new OrDependencyFilter( filters ).accept( builder.build(), parents ) );

        filters = new LinkedList<DependencyFilter>();
        filters.add( getDenyFilter() );
        filters.add( getDenyFilter() );
        assertFalse( new OrDependencyFilter( filters ).accept( builder.build(), parents ) );

        // newInstance
        assertTrue( AndDependencyFilter.newInstance( getAcceptFilter(), getAcceptFilter() ).accept( builder.build(),
                                                                                                    parents ) );
        assertFalse( AndDependencyFilter.newInstance( getAcceptFilter(), getDenyFilter() ).accept( builder.build(),
                                                                                                   parents ) );
        assertTrue( AndDependencyFilter.newInstance( getAcceptFilter(), null ).accept( builder.build(), parents ) );
        assertFalse( AndDependencyFilter.newInstance( getDenyFilter(), null ).accept( builder.build(), parents ) );
        assertNull( AndDependencyFilter.newInstance( null, null ) );
    }

}
