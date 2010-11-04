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
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.sonatype.aether.artifact.Artifact;

/**
 * @author Benjamin Bentmann
 */
public class DefaultArtifactTest
{

    @Test
    public void testDefaultArtifactString()
    {
        Artifact a;

        a = new DefaultArtifact( "gid:aid:ver" );
        assertEquals( "gid", a.getGroupId() );
        assertEquals( "aid", a.getArtifactId() );
        assertEquals( "ver", a.getVersion() );
        assertEquals( "jar", a.getExtension() );
        assertEquals( "", a.getClassifier() );

        a = new DefaultArtifact( "gid:aid:ext:ver" );
        assertEquals( "gid", a.getGroupId() );
        assertEquals( "aid", a.getArtifactId() );
        assertEquals( "ver", a.getVersion() );
        assertEquals( "ext", a.getExtension() );
        assertEquals( "", a.getClassifier() );

        a = new DefaultArtifact( "gid:aid:ext:cls:ver" );
        assertEquals( "gid", a.getGroupId() );
        assertEquals( "aid", a.getArtifactId() );
        assertEquals( "ver", a.getVersion() );
        assertEquals( "ext", a.getExtension() );
        assertEquals( "cls", a.getClassifier() );

        a = new DefaultArtifact( "gid:aid::cls:ver" );
        assertEquals( "gid", a.getGroupId() );
        assertEquals( "aid", a.getArtifactId() );
        assertEquals( "ver", a.getVersion() );
        assertEquals( "jar", a.getExtension() );
        assertEquals( "cls", a.getClassifier() );

        a = new DefaultArtifact( new DefaultArtifact( "gid:aid:ext:cls:ver" ).toString() );
        assertEquals( "gid", a.getGroupId() );
        assertEquals( "aid", a.getArtifactId() );
        assertEquals( "ver", a.getVersion() );
        assertEquals( "ext", a.getExtension() );
        assertEquals( "cls", a.getClassifier() );
    }

    @Test( expected = IllegalArgumentException.class )
    public void testDefaultArtifactBadString()
    {
        new DefaultArtifact( "gid:aid" );
    }

    @Test
    public void testImmutability()
    {
        String coords = "gid:aid:ext:cls:ver";
        HashMap<String, String> map = new HashMap<String, String>();
        map.put( "someProperty", "someValue" );

        Artifact a = new DefaultArtifact( coords );
        assertNotSame( a, a.setFile( new File( "file" ) ) );
        assertNotSame( a, a.setVersion( "otherVersion" ));
        assertNotSame( a, a.setProperties( map ) );
    }

    @Test
    public void testArtifactType()
    {
        DefaultArtifactType type = new DefaultArtifactType( "typeId", "typeExt", "typeCls", "typeLang", true, true );

        Artifact a = new DefaultArtifact( "gid", "aid", null, null, null, null, type );
        assertEquals( "typeExt", a.getExtension() );
        assertEquals( "typeCls", a.getClassifier() );
        assertEquals( "typeLang", a.getProperties().get( ArtifactProperties.LANGUAGE ) );
        assertEquals( "typeId", a.getProperties().get( ArtifactProperties.TYPE ) );
        assertEquals( "true", a.getProperties().get( ArtifactProperties.INCLUDES_DEPENDENCIES ) );
        assertEquals( "true", a.getProperties().get( ArtifactProperties.CONSTITUTES_BUILD_PATH ) );
        
        a = new DefaultArtifact( "gid", "aid", "cls", "ext", "ver", null, type );
        assertEquals( "ext", a.getExtension() );
        assertEquals( "cls", a.getClassifier() );
        assertEquals( "typeLang", a.getProperties().get( ArtifactProperties.LANGUAGE ) );
        assertEquals( "typeId", a.getProperties().get( ArtifactProperties.TYPE ) );
        assertEquals( "true", a.getProperties().get( ArtifactProperties.INCLUDES_DEPENDENCIES ) );
        assertEquals( "true", a.getProperties().get( ArtifactProperties.CONSTITUTES_BUILD_PATH ) );

        Map<String, String> props = new HashMap<String, String>();
        props.put( "someNonStandardProperty", "someNonStandardProperty" );
        a = new DefaultArtifact( "gid", "aid", "cls", "ext", "ver", props, type );
        assertEquals( "ext", a.getExtension() );
        assertEquals( "cls", a.getClassifier() );
        assertEquals( "typeLang", a.getProperties().get( ArtifactProperties.LANGUAGE ) );
        assertEquals( "typeId", a.getProperties().get( ArtifactProperties.TYPE ) );
        assertEquals( "true", a.getProperties().get( ArtifactProperties.INCLUDES_DEPENDENCIES ) );
        assertEquals( "true", a.getProperties().get( ArtifactProperties.CONSTITUTES_BUILD_PATH ) );
        assertEquals( "someNonStandardProperty", a.getProperties().get( "someNonStandardProperty" ) );
        
        props = new HashMap<String, String>();
        props.put( "someNonStandardProperty", "someNonStandardProperty" );
        props.put( ArtifactProperties.CONSTITUTES_BUILD_PATH, "rubbish" );
        props.put( ArtifactProperties.INCLUDES_DEPENDENCIES, "rubbish" );
        a = new DefaultArtifact( "gid", "aid", "cls", "ext", "ver", props, type );
        assertEquals( "ext", a.getExtension() );
        assertEquals( "cls", a.getClassifier() );
        assertEquals( "typeLang", a.getProperties().get( ArtifactProperties.LANGUAGE ) );
        assertEquals( "typeId", a.getProperties().get( ArtifactProperties.TYPE ) );
        assertEquals( "rubbish", a.getProperties().get( ArtifactProperties.INCLUDES_DEPENDENCIES ) );
        assertEquals( "rubbish", a.getProperties().get( ArtifactProperties.CONSTITUTES_BUILD_PATH ) );
        assertEquals( "someNonStandardProperty", a.getProperties().get( "someNonStandardProperty" ) );
    }

}
