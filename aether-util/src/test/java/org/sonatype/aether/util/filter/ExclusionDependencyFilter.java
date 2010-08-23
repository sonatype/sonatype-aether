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
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.sonatype.aether.DependencyNode;
import org.sonatype.aether.util.NodeBuilder;

public class ExclusionDependencyFilter
{

    @Test
    public void acceptTest()
    {

        NodeBuilder builder = new NodeBuilder();
        builder.groupId( "com.example.test" ).artifactId( "testArtifact" );
        List<DependencyNode> parents = new LinkedList<DependencyNode>();
        String[] excludes;

        excludes = new String[] { "com.example.test:testArtifact" };
        assertFalse( new ExclusionsDependencyFilter( Arrays.asList( excludes ) ).accept( builder.build(), parents ) );

        excludes = new String[] { "com.example.test:testArtifact", "com.foo:otherArtifact" };
        assertFalse( new ExclusionsDependencyFilter( Arrays.asList( excludes ) ).accept( builder.build(), parents ) );

        excludes = new String[] { "testArtifact" };
        assertFalse( new ExclusionsDependencyFilter( Arrays.asList( excludes ) ).accept( builder.build(), parents ) );

        excludes = new String[] { "otherArtifact" };
        assertTrue( new ExclusionsDependencyFilter( Arrays.asList( excludes ) ).accept( builder.build(), parents ) );

        assertTrue( new ExclusionsDependencyFilter( (Collection<String>) null ).accept( builder.build(), parents ) );
    }
}
