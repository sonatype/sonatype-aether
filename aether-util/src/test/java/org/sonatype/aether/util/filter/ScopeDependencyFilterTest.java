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

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.sonatype.aether.DependencyNode;
import org.sonatype.aether.util.NodeBuilder;

public class ScopeDependencyFilterTest
    extends AbstractDependencyFilterTest
{

    @Test
    public void acceptTest()
    {

        NodeBuilder builder = new NodeBuilder();
        builder.scope( "compile" ).artifactId( "test" );
        List<DependencyNode> parents = new LinkedList<DependencyNode>();

        // null or empty
        assertTrue( new ScopeDependencyFilter( null, null ).accept( builder.build(), parents ) );
        assertTrue( new ScopeDependencyFilter( new LinkedList<String>(), new LinkedList<String>() ).accept( builder.build(), parents ) );
        assertTrue( new ScopeDependencyFilter( (String[]) null ).accept( builder.build(), parents ) );
        
        //only excludes
        assertTrue( new ScopeDependencyFilter( "test" ).accept( builder.build(), parents ));
        assertFalse( new ScopeDependencyFilter( "compile" ).accept( builder.build(), parents ));
        assertFalse( new ScopeDependencyFilter( "compile", "test" ).accept( builder.build(), parents ));
        
        //Both
        String[] excludes1 = {"provided"};
        String[] includes1 = {"compile","test"}; 
        assertTrue( new ScopeDependencyFilter( Arrays.asList( includes1 ), Arrays.asList( excludes1 ) ).accept( builder.build() , parents ));
        assertTrue( new ScopeDependencyFilter( Arrays.asList( includes1 ), null ).accept( builder.build() , parents ));
        
        //exclude wins
        String[] excludes2 = {"compile"};
        String[] includes2 = {"compile"};
        assertFalse( new ScopeDependencyFilter( Arrays.asList( includes2 ), Arrays.asList( excludes2 ) ).accept( builder.build() , parents ));

    }

}
