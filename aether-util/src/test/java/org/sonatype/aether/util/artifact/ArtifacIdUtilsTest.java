package org.sonatype.aether.util.artifact;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import static org.junit.Assert.*;

import org.junit.Test;
import org.sonatype.aether.artifact.Artifact;

/**
 * @author Benjamin Bentmann
 */
public class ArtifacIdUtilsTest
{

    @Test
    public void testToIdArtifact()
    {
        Artifact artifact = null;
        assertSame( null, ArtifacIdUtils.toId( artifact ) );

        artifact = new DefaultArtifact( "gid", "aid", "ext", "1.0-20110205.132618-23" );
        assertEquals( "gid:aid:ext:1.0-20110205.132618-23", ArtifacIdUtils.toId( artifact ) );

        artifact = new DefaultArtifact( "gid", "aid", "cls", "ext", "1.0-20110205.132618-23" );
        assertEquals( "gid:aid:ext:cls:1.0-20110205.132618-23", ArtifacIdUtils.toId( artifact ) );
    }

    @Test
    public void testToIdStrings()
    {
        assertEquals( ":::", ArtifacIdUtils.toId( null, null, null, null, null ) );

        assertEquals( "gid:aid:ext:1", ArtifacIdUtils.toId( "gid", "aid", "ext", "", "1" ) );

        assertEquals( "gid:aid:ext:cls:1", ArtifacIdUtils.toId( "gid", "aid", "ext", "cls", "1" ) );
    }

    @Test
    public void testToBaseIdArtifact()
    {
        Artifact artifact = null;
        assertSame( null, ArtifacIdUtils.toBaseId( artifact ) );

        artifact = new DefaultArtifact( "gid", "aid", "ext", "1.0-20110205.132618-23" );
        assertEquals( "gid:aid:ext:1.0-SNAPSHOT", ArtifacIdUtils.toBaseId( artifact ) );

        artifact = new DefaultArtifact( "gid", "aid", "cls", "ext", "1.0-20110205.132618-23" );
        assertEquals( "gid:aid:ext:cls:1.0-SNAPSHOT", ArtifacIdUtils.toBaseId( artifact ) );
    }

    @Test
    public void testToVersionlessIdArtifact()
    {
        Artifact artifact = null;
        assertSame( null, ArtifacIdUtils.toId( artifact ) );

        artifact = new DefaultArtifact( "gid", "aid", "ext", "1" );
        assertEquals( "gid:aid:ext", ArtifacIdUtils.toVersionlessId( artifact ) );

        artifact = new DefaultArtifact( "gid", "aid", "cls", "ext", "1" );
        assertEquals( "gid:aid:ext:cls", ArtifacIdUtils.toVersionlessId( artifact ) );
    }

    @Test
    public void testToVersionlessIdStrings()
    {
        assertEquals( "::", ArtifacIdUtils.toVersionlessId( null, null, null, null ) );

        assertEquals( "gid:aid:ext", ArtifacIdUtils.toVersionlessId( "gid", "aid", "ext", "" ) );

        assertEquals( "gid:aid:ext:cls", ArtifacIdUtils.toVersionlessId( "gid", "aid", "ext", "cls" ) );
    }

}
