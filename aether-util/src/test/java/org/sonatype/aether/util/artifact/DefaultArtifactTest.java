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
import java.util.HashMap;

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

}
