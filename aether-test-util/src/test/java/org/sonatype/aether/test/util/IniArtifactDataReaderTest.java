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
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.graph.Exclusion;

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
    public void testRelocations()
        throws IOException
    {
        String def = "[relocations]\ngid:aid:ver:ext\ngid2:aid2:ver2:ext2";

        ArtifactDescription description = parser.parseLiteral( def );

        List<Artifact> relocations = description.getRelocations();
        assertNotNull( relocations );
        assertEquals( 2, relocations.size() );

        Artifact artifact = relocations.get( 0 );
        assertEquals( "aid", artifact.getArtifactId() );
        assertEquals( "gid", artifact.getGroupId() );
        assertEquals( "ver", artifact.getVersion() );
        assertEquals( "ext", artifact.getExtension() );

        artifact = relocations.get( 1 );
        assertEquals( "aid2", artifact.getArtifactId() );
        assertEquals( "gid2", artifact.getGroupId() );
        assertEquals( "ver2", artifact.getVersion() );
        assertEquals( "ext2", artifact.getExtension() );
    }

    @Test
    public void testDependencies()
        throws IOException
    {
        String def = "[dependencies]\ngid:aid:ver:ext\n-exclusion:aid\ngid2:aid2:ver2:ext2";

        ArtifactDescription description = parser.parseLiteral( def );

        List<Dependency> dependencies = description.getDependencies();
        assertNotNull( dependencies );
        assertEquals( 2, dependencies.size() );

        Dependency dependency = dependencies.get( 0 );
        assertNotNull( dependency.getArtifact() );

        Artifact artifact = dependency.getArtifact();
        assertEquals( "aid", artifact.getArtifactId() );
        assertEquals( "gid", artifact.getGroupId() );
        assertEquals( "ver", artifact.getVersion() );
        assertEquals( "ext", artifact.getExtension() );

        Collection<Exclusion> exclusions = dependency.getExclusions();
        assertNotNull( exclusions );
        assertEquals( 1, exclusions.size() );
        Exclusion exclusion = exclusions.iterator().next();
        assertEquals( "exclusion", exclusion.getGroupId() );
        assertEquals( "aid", exclusion.getArtifactId() );

        dependency = dependencies.get( 1 );
        assertNotNull( dependency.getArtifact() );

        artifact = dependency.getArtifact();
        assertEquals( "aid2", artifact.getArtifactId() );
        assertEquals( "gid2", artifact.getGroupId() );
        assertEquals( "ver2", artifact.getVersion() );
        assertEquals( "ext2", artifact.getExtension() );
    }

}
