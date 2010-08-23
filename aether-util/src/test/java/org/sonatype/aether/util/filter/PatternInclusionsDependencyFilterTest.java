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

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.sonatype.aether.DependencyNode;
import org.sonatype.aether.VersionScheme;
import org.sonatype.aether.util.NodeBuilder;
import org.sonatype.aether.util.version.GenericVersionScheme;

public class PatternInclusionsDependencyFilterTest
    extends AbstractDependencyFilterTest
{

    @Test
    public void acceptTestCornerCases()
    {
        NodeBuilder builder = new NodeBuilder();
        builder.artifactId( "testArtifact" );
        List<DependencyNode> parents = new LinkedList<DependencyNode>();

        // Empty String, Empty List
        assertTrue( new PatternInclusionsDependencyFilter( "" ).accept( builder.build(), parents ) );
        assertFalse( new PatternInclusionsDependencyFilter( new LinkedList<String>() ).accept( builder.build(), parents ) );
        assertFalse( new PatternInclusionsDependencyFilter( (String[]) null ).accept( builder.build(), parents ) );
        assertFalse( new PatternInclusionsDependencyFilter( (VersionScheme) null, "[1,10]" ).accept( builder.build(),
                                                                                                     parents ) );
    }

    @Test
    public void acceptTestMatches()
    {
        NodeBuilder builder = new NodeBuilder();
        builder.groupId( "com.example.test" ).artifactId( "testArtifact" ).ext( "jar" ).version( "1.0.3" );
        List<DependencyNode> parents = new LinkedList<DependencyNode>();

        // full match
        assertTrue( new PatternInclusionsDependencyFilter( "com.example.test:testArtifact:jar:1.0.3" ).accept( builder.build(),
                                                                                                               parents ) );

        // single wildcard
        assertTrue( new PatternInclusionsDependencyFilter( "*:testArtifact:jar:1.0.3" ).accept( builder.build(),
                                                                                                parents ) );
        assertTrue( new PatternInclusionsDependencyFilter( "com.example.test:*:jar:1.0.3" ).accept( builder.build(),
                                                                                                    parents ) );
        assertTrue( new PatternInclusionsDependencyFilter( "com.example.test:testArtifact:*:1.0.3" ).accept( builder.build(),
                                                                                                             parents ) );
        assertTrue( new PatternInclusionsDependencyFilter( "com.example.test:testArtifact:*:1.0.3" ).accept( builder.build(),
                                                                                                             parents ) );

        // implicite wildcard
        assertTrue( new PatternInclusionsDependencyFilter( ":testArtifact:jar:1.0.3" ).accept( builder.build(), parents ) );
        assertTrue( new PatternInclusionsDependencyFilter( "com.example.test::jar:1.0.3" ).accept( builder.build(),
                                                                                                   parents ) );
        assertTrue( new PatternInclusionsDependencyFilter( "com.example.test:testArtifact::1.0.3" ).accept( builder.build(),
                                                                                                            parents ) );
        assertTrue( new PatternInclusionsDependencyFilter( "com.example.test:testArtifact:jar:" ).accept( builder.build(),
                                                                                                          parents ) );

        // multi wildcards
        assertTrue( new PatternInclusionsDependencyFilter( "*:*:jar:1.0.3" ).accept( builder.build(), parents ) );
        assertTrue( new PatternInclusionsDependencyFilter( "com.example.test:*:*:1.0.3" ).accept( builder.build(),
                                                                                                  parents ) );
        assertTrue( new PatternInclusionsDependencyFilter( "com.example.test:testArtifact:*:*" ).accept( builder.build(),
                                                                                                         parents ) );
        assertTrue( new PatternInclusionsDependencyFilter( "*:testArtifact:jar:*" ).accept( builder.build(), parents ) );
        assertTrue( new PatternInclusionsDependencyFilter( "*:*:jar:*" ).accept( builder.build(), parents ) );
        assertTrue( new PatternInclusionsDependencyFilter( ":*:jar:" ).accept( builder.build(), parents ) );

        // partial wildcards
        assertTrue( new PatternInclusionsDependencyFilter( "*.example.test:testArtifact:jar:1.0.3" ).accept( builder.build(),
                                                                                                             parents ) );
        assertTrue( new PatternInclusionsDependencyFilter( "com.example.test:testArtifact:*ar:1.0.*" ).accept( builder.build(),
                                                                                                               parents ) );
        assertTrue( new PatternInclusionsDependencyFilter( "com.example.test:testArtifact:jar:1.0.*" ).accept( builder.build(),
                                                                                                               parents ) );
        assertTrue( new PatternInclusionsDependencyFilter( "*.example.*:testArtifact:jar:1.0.3" ).accept( builder.build(),
                                                                                                          parents ) );

        // wildcard as empty string
        assertTrue( new PatternInclusionsDependencyFilter( "com.example.test*:testArtifact:jar:1.0.3" ).accept( builder.build(),
                                                                                                                parents ) );

        // TODO: Internal wildcards are not supported
        // assertTrue( new PatternInclusionsDependencyFilter( "com.example.test:test*fact:jar:1.0.3" ).accept(
        // builder.build() ) );
    }

    @Test
    public void acceptTestLessToken()
    {
        NodeBuilder builder = new NodeBuilder();
        builder.groupId( "com.example.test" ).artifactId( "testArtifact" ).ext( "jar" ).version( "1.0.3" );
        List<DependencyNode> parents = new LinkedList<DependencyNode>();

        assertTrue( new PatternInclusionsDependencyFilter( "com.example.test:testArtifact:jar" ).accept( builder.build(),
                                                                                                         parents ) );
        assertTrue( new PatternInclusionsDependencyFilter( "com.example.test:testArtifact" ).accept( builder.build(),
                                                                                                     parents ) );
        assertTrue( new PatternInclusionsDependencyFilter( "com.example.test" ).accept( builder.build(), parents ) );

        assertFalse( new PatternInclusionsDependencyFilter( "com.example.foo" ).accept( builder.build(), parents ) );
    }

    @Test
    public void acceptTestMissmatch()
    {
        NodeBuilder builder = new NodeBuilder();
        builder.groupId( "com.example.test" ).artifactId( "testArtifact" ).ext( "jar" ).version( "1.0.3" );
        List<DependencyNode> parents = new LinkedList<DependencyNode>();

        assertFalse( new PatternInclusionsDependencyFilter( "OTHER.GROUP.ID:testArtifact:jar:1.0.3" ).accept( builder.build(),
                                                                                                              parents ) );
        assertFalse( new PatternInclusionsDependencyFilter( "com.example.test:OTHER_ARTIFACT:jar:1.0.3" ).accept( builder.build(),
                                                                                                                  parents ) );
        assertFalse( new PatternInclusionsDependencyFilter( "com.example.test:OTHER_ARTIFACT:jar:1.0.3" ).accept( builder.build(),
                                                                                                                  parents ) );
        assertFalse( new PatternInclusionsDependencyFilter( "com.example.test:testArtifact:WAR:1.0.3" ).accept( builder.build(),
                                                                                                                parents ) );
        assertFalse( new PatternInclusionsDependencyFilter( "com.example.test:testArtifact:jar:SNAPSHOT" ).accept( builder.build(),
                                                                                                                   parents ) );

        assertFalse( new PatternInclusionsDependencyFilter( "*:*:war:*" ).accept( builder.build(), parents ) );
        assertFalse( new PatternInclusionsDependencyFilter( "OTHER.GROUP.ID" ).accept( builder.build(), parents ) );
    }

    @Test
    public void acceptTestMoreToken()
    {
        NodeBuilder builder = new NodeBuilder();
        builder.groupId( "com.example.test" ).artifactId( "testArtifact" ).ext( "jar" ).version( "1.0.3" );
        List<DependencyNode> parents = new LinkedList<DependencyNode>();

        assertFalse( new PatternInclusionsDependencyFilter( "com.example.test:testArtifact:jar:1.0.3:foo" ).accept( builder.build(),
                                                                                                                    parents ) );
    }

    @Test
    public void acceptTestRange()
    {
        NodeBuilder builder = new NodeBuilder();
        builder.groupId( "com.example.test" ).artifactId( "testArtifact" ).ext( "jar" ).version( "1.0.3" );
        List<DependencyNode> parents = new LinkedList<DependencyNode>();

        String prefix = "com.example.test:testArtifact:jar:";

        assertTrue( new PatternInclusionsDependencyFilter( new GenericVersionScheme(), prefix + "[1.0.3,1.0.4)" ).accept( builder.build(),
                                                                                                                          parents ) );
        assertTrue( new PatternInclusionsDependencyFilter( new GenericVersionScheme(), prefix + "[1.0.3,)" ).accept( builder.build(),
                                                                                                                     parents ) );
        assertTrue( new PatternInclusionsDependencyFilter( new GenericVersionScheme(), prefix + "[1.0.3,]" ).accept( builder.build(),
                                                                                                                     parents ) );
        assertTrue( new PatternInclusionsDependencyFilter( new GenericVersionScheme(), prefix + "(,1.0.3]" ).accept( builder.build(),
                                                                                                                     parents ) );
        assertTrue( new PatternInclusionsDependencyFilter( new GenericVersionScheme(), prefix + "[1.0,]" ).accept( builder.build(),
                                                                                                                   parents ) );
        assertTrue( new PatternInclusionsDependencyFilter( new GenericVersionScheme(), prefix + "[1,4]" ).accept( builder.build(),
                                                                                                                  parents ) );
        assertTrue( new PatternInclusionsDependencyFilter( new GenericVersionScheme(), prefix + "(1,4)" ).accept( builder.build(),
                                                                                                                  parents ) );

        assertTrue( new PatternInclusionsDependencyFilter( new GenericVersionScheme(), prefix + "(1.0.2,1.0.3]", prefix
            + "(1.1,)" ).accept( builder.build(), parents ) );

        assertFalse( new PatternInclusionsDependencyFilter( new GenericVersionScheme(), prefix + "(1.0.3,2.0]" ).accept( builder.build(),
                                                                                                                         parents ) );
        assertFalse( new PatternInclusionsDependencyFilter( new GenericVersionScheme(), prefix + "(1,1.0.2]" ).accept( builder.build(),
                                                                                                                       parents ) );

        assertFalse( new PatternInclusionsDependencyFilter( new GenericVersionScheme(), prefix + "(1.0.2,1.0.3)",
                                                            prefix + "(1.0.3,)" ).accept( builder.build(), parents ) );
    }

}
