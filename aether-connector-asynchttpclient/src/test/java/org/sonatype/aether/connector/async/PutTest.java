package org.sonatype.aether.connector.async;

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

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.metadata.Metadata;
import org.sonatype.aether.spi.connector.ArtifactUpload;
import org.sonatype.aether.spi.connector.MetadataUpload;
import org.sonatype.tests.jetty.runner.ConfigurationRunner;

/**
 * @author Benjamin Hanzelmann
 */
@RunWith( ConfigurationRunner.class )
public class PutTest
    extends AsyncConnectorSuiteConfiguration
{

    @Test
    public void testArtifactUpload()
        throws Exception
    {
        addExpectation( "gid/aid/version/aid-version-classifier.extension", "artifact" );
        addExpectation( "gid/aid/version/aid-version-classifier.extension.sha1", sha1( "artifact" ) );
        addExpectation( "gid/aid/version/aid-version-classifier.extension.md5", md5( "artifact" ) );

        Artifact artifact = artifact( "artifact" );
        ArtifactUpload up = new ArtifactUpload( artifact, artifact.getFile() );
        List<ArtifactUpload> uploads = Arrays.asList( up );
        connector().put( uploads, null );

        assertNull( up.getException() );
        assertExpectations();
    }

    @Test
    public void testMetadataUpload()
        throws Exception
    {
        String content = "metadata";
        addExpectation( "gid/aid/version/maven-metadata.xml", content );
        addExpectation( "gid/aid/version/maven-metadata.xml.sha1", sha1( content ) );
        addExpectation( "gid/aid/version/maven-metadata.xml.md5", md5( content ) );

        Metadata metadata = metadata( content );

        List<MetadataUpload> uploads = Arrays.asList( new MetadataUpload( metadata, metadata.getFile() ) );
        connector().put( null, uploads );

        assertExpectations();
    }

    @Test( expected = Exception.class )
    public void testClosedPut()
        throws Exception
    {
        connector().close();
        Metadata metadata = metadata( "metadata" );

        List<MetadataUpload> uploads = Arrays.asList( new MetadataUpload( metadata, metadata.getFile() ) );
        connector().put( null, uploads );
    }

    @Test
    public void testCloseAfterArtifactUpload()
        throws Exception
    {
        Artifact artifact = artifact( "artifact" );
        List<ArtifactUpload> uploads = Arrays.asList( new ArtifactUpload( artifact, artifact.getFile() ) );
        connector().put( uploads, null );
        connector().close();
    }

    @Test
    public void testCloseAfterMetadataUpload()
        throws Exception
    {
        Metadata metadata = metadata( "metadata" );

        List<MetadataUpload> uploads = Arrays.asList( new MetadataUpload( metadata, metadata.getFile() ) );
        connector().put( null, uploads );
        connector().close();
    }

}
