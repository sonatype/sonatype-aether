package org.sonatype.aether.util.artifact;

/*******************************************************************************
 * Copyright (c) 2010 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

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
