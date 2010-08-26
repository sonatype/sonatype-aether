package org.sonatype.aether.test.util;

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

import java.io.IOException;
import java.util.List;

import org.junit.Test;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.test.util.DependencyGraphParser.TreeParseException;

/**
 * @author Benjamin Hanzelmann
 */
public class DependencyGraphParserTest
{
    @Test
    public void testOnlyRoot()
        throws IOException, TreeParseException
    {
        String def = "gid:aid:jar:1:scope";

        DependencyNode node = DependencyGraphParser.parse( def );

        assertNotNull( node );
        assertEquals( 0, node.getChildren().size() );

        Dependency dependency = node.getDependency();
        assertNotNull( dependency );
        assertEquals( "scope", dependency.getScope() );

        Artifact artifact = dependency.getArtifact();
        assertNotNull( artifact );

        assertEquals( "gid", artifact.getGroupId() );
        assertEquals( "aid", artifact.getArtifactId() );
        assertEquals( "jar", artifact.getExtension() );
        assertEquals( "1", artifact.getVersion() );
    }

    @Test
    public void testOptionalScope()
        throws IOException, TreeParseException
    {
        String def = "gid:aid:jar:1";

        DependencyNode node = DependencyGraphParser.parse( def );

        assertNotNull( node );
        assertEquals( 0, node.getChildren().size() );

        Dependency dependency = node.getDependency();
        assertNotNull( dependency );
        assertEquals( "", dependency.getScope() );

    }

    @Test
    public void testWithChildren() throws IOException, TreeParseException
    {
        String def = "gid1:aid1:ext1:ver1:scope1\n" + 
                     "+- gid2:aid2:ext2:ver2:scope2\n" +
                    "\\- gid3:aid3:ext3:ver3:scope3\n";

        DependencyNode node = DependencyGraphParser.parse( def );
        assertNotNull( node );

        int idx = 1;

        assertNodeProperties( node, idx++ );

        List<DependencyNode> children = node.getChildren();
        assertEquals( 2, children.size() );

        for ( DependencyNode child : children )
        {
            assertNodeProperties( child, idx++ );
        }
        
    }
    
    @Test
    public void testDeepChildren() throws TreeParseException
    {
        String def = "gid1:aid1:ext1:ver1\n" + 
                     "+- gid2:aid2:ext2:ver2:scope2\n" +
                     "|  \\- gid3:aid3:ext3:ver3\n" +
                     "\\- gid4:aid4:ext4:ver4:scope4";
        
        DependencyNode node = DependencyGraphParser.parse( def );
        assertNodeProperties( node, 1 );
        
        assertEquals( 2, node.getChildren().size() );
        assertNodeProperties( node.getChildren().get( 1 ), 4 );
        DependencyNode lvl1Node = node.getChildren().get( 0 );
        assertNodeProperties( lvl1Node, 2 );
        
        assertEquals( 1, lvl1Node.getChildren().size() );
        assertNodeProperties( lvl1Node.getChildren().get( 0 ), 3 );
    }

    private void assertNodeProperties( DependencyNode node, int idx )
    {
        Dependency dependency = node.getDependency();
        assertNotNull( dependency );
        if ( !"".equals( dependency.getScope() ) )
        {
            assertEquals( "scope" + idx, dependency.getScope() );
        }

        Artifact artifact = dependency.getArtifact();
        assertNotNull( artifact );

        assertEquals( "gid" + idx, artifact.getGroupId() );
        assertEquals( "aid" + idx, artifact.getArtifactId() );
        assertEquals( "ext" + idx, artifact.getExtension() );
        assertEquals( "ver" + idx, artifact.getVersion() );
    }
}
