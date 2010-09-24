package org.sonatype.aether.util.artifact;

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

import java.io.File;

import org.junit.Test;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.util.artifact.SubArtifact;

/**
 * @author Benjamin Bentmann
 */
public class SubArtifactTest
{

    private Artifact newMainArtifact( String coords )
    {
        return new DefaultArtifact( coords );
    }

    @Test
    public void testMainArtifactFileNotRetained()
    {
        Artifact a = newMainArtifact( "gid:aid:ver" ).setFile( new File( "" ) );
        assertNotNull( a.getFile() );
        a = new SubArtifact( a, "", "pom" );
        assertNull( a.getFile() );
    }

    @Test( expected = IllegalArgumentException.class )
    public void testMainArtifactMissing()
    {
        new SubArtifact( null, "", "pom" );
    }

    @Test
    public void testEmptyClassifier()
    {
        Artifact main = newMainArtifact( "gid:aid:ext:cls:ver" );
        Artifact sub = new SubArtifact( main, "", "pom" );
        assertEquals( "", sub.getClassifier() );
        sub = new SubArtifact( main, null, "pom" );
        assertEquals( "", sub.getClassifier() );
    }

    @Test
    public void testEmptyExtension()
    {
        Artifact main = newMainArtifact( "gid:aid:ext:cls:ver" );
        Artifact sub = new SubArtifact( main, "tests", "" );
        assertEquals( "", sub.getExtension() );
        sub = new SubArtifact( main, "tests", null );
        assertEquals( "", sub.getExtension() );
    }

    @Test
    public void testSameClassifier()
    {
        Artifact main = newMainArtifact( "gid:aid:ext:cls:ver" );
        Artifact sub = new SubArtifact( main, "*", "pom" );
        assertEquals( "cls", sub.getClassifier() );
    }

    @Test
    public void testSameExtension()
    {
        Artifact main = newMainArtifact( "gid:aid:ext:cls:ver" );
        Artifact sub = new SubArtifact( main, "tests", "*" );
        assertEquals( "ext", sub.getExtension() );
    }

    @Test
    public void testDerivedClassifier()
    {
        Artifact main = newMainArtifact( "gid:aid:ext:cls:ver" );
        Artifact sub = new SubArtifact( main, "*-tests", "pom" );
        assertEquals( "cls-tests", sub.getClassifier() );
        sub = new SubArtifact( main, "tests-*", "pom" );
        assertEquals( "tests-cls", sub.getClassifier() );

        main = newMainArtifact( "gid:aid:ext:ver" );
        sub = new SubArtifact( main, "*-tests", "pom" );
        assertEquals( "tests", sub.getClassifier() );
        sub = new SubArtifact( main, "tests-*", "pom" );
        assertEquals( "tests", sub.getClassifier() );
    }

    @Test
    public void testDerivedExtension()
    {
        Artifact main = newMainArtifact( "gid:aid:ext:cls:ver" );
        Artifact sub = new SubArtifact( main, "", "*.asc" );
        assertEquals( "ext.asc", sub.getExtension() );
        sub = new SubArtifact( main, "", "asc.*" );
        assertEquals( "asc.ext", sub.getExtension() );
    }

}
