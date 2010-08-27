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
package org.sonatype.aether.test.util;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.sonatype.aether.test.util.IniArtifactDataReader.ArtifactData;
import org.sonatype.aether.test.util.IniArtifactDataReader.ArtifactDescription;
import org.sonatype.aether.test.util.IniArtifactDataReader.Dependency;
import org.sonatype.aether.test.util.IniArtifactDataReader.DependencyData;

/**
 * @author Benjamin Hanzelmann
 */
public class IniArtifactDataReaderTest
{

    private IniArtifactDataReader parser;

    @Before
    public void setup()
        throws Exception
    {
        this.parser = new IniArtifactDataReader( "org/sonatype/aether/test/util/" );
    }

    @Test
    public void testSimple()
        throws IOException
    {
        String def = "[artifact]\n artifactId = aid\n groupId = gid\n version = ver\n";
        ArtifactData description = parser.parseLiteral( def ).getSelfData();

        assertEquals( "aid", description.getArtifactId() );
        assertEquals( "gid", description.getGroupId() );
        assertEquals( "ver", description.getVersion() );

    }

    @Test
    public void testResource()
        throws IOException
    {
        String resource = "ArtifactDataReaderTest.ini";
        ArtifactDescription description = parser.parse( resource );

        ArtifactData self = description.getSelfData();

        assertNotNull( self );
        assertEquals( "aid", self.getArtifactId() );
        assertEquals( "gid", self.getGroupId() );
        assertEquals( "ver", self.getVersion() );
        assertEquals( "ext", self.getPackaging() );

        ArtifactData parent = description.getParentData();
        assertNotNull( parent );
        assertEquals( "aid2", parent.getArtifactId() );
        assertEquals( "gid", parent.getGroupId() );
        assertEquals( "ver", parent.getVersion() );

        DependencyData dependencies = description.getDependencyData();
        assertNotNull( dependencies );

        assertNotNull( dependencies.getDependencies() );
        assertEquals( 2, dependencies.getDependencies().size() );
        
        Dependency dep = dependencies.getDependencies().get( 0 );
        assertEquals( "gid", dep.getGroupId() );
        assertEquals( "aid", dep.getArtifactId() );
        assertEquals( "ext", dep.getType() );
        assertEquals( "ver", dep.getVersion() );
        assertEquals( "scope", dep.getScope() );
        assertTrue( dep.isOptional() );

        dep = dependencies.getDependencies().get( 1 );
        assertEquals( "gid2", dep.getGroupId() );
        assertEquals( "aid2", dep.getArtifactId() );
        assertEquals( "ext", dep.getType() );
        assertEquals( "ver", dep.getVersion() );
        assertEquals( "scope", dep.getScope() );
        assertFalse( dep.isOptional() );

    }

}
