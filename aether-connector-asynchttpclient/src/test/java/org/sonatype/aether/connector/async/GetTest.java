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

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.RepositoryPolicy;
import org.sonatype.aether.spi.connector.ArtifactDownload;
import org.sonatype.aether.spi.connector.RepositoryConnector;
import org.sonatype.aether.test.util.TestFileUtils;
import org.sonatype.aether.transfer.TransferEvent;
import org.sonatype.tests.http.runner.junit.ConfigurationRunner;

/**
 * @author Benjamin Hanzelmann
 */
@RunWith( ConfigurationRunner.class )
public class GetTest
    extends AsyncConnectorSuiteConfiguration
{

    @Test
    public void testDownloadArtifact()
        throws Exception
    {
        addDelivery( "gid/aid/version/aid-version-classifier.extension", "artifact" );
        addDelivery( "gid/aid/version/aid-version-classifier.extension.sha1", sha1( "artifact" ) );
        addDelivery( "gid/aid/version/aid-version-classifier.extension.md5", md5( "artifact" ) );

        File f = TestFileUtils.createTempFile( "" );
        Artifact a = artifact( "bla" );

        ArtifactDownload down = new ArtifactDownload( a, null, f, RepositoryPolicy.CHECKSUM_POLICY_FAIL );
        Collection<? extends ArtifactDownload> downs = Arrays.asList( down );
        RepositoryConnector c = connector();
        c.get( downs, null );

        assertNull( String.valueOf( down.getException() ), down.getException() );
        TestFileUtils.assertContent( "artifact", f );
    }

    @Test
    public void testDownloadArtifactChecksumFailure()
        throws Exception
    {
        addDelivery( "gid/aid/version/aid-version-classifier.extension", "artifact" );
        addDelivery( "gid/aid/version/aid-version-classifier.extension.sha1", "foo" );
        addDelivery( "gid/aid/version/aid-version-classifier.extension.md5", "bar" );

        File f = TestFileUtils.createTempFile( "" );
        Artifact a = artifact( "bla" );

        ArtifactDownload down = new ArtifactDownload( a, null, f, RepositoryPolicy.CHECKSUM_POLICY_FAIL );
        Collection<? extends ArtifactDownload> downs = Arrays.asList( down );
        connector().get( downs, null );

        assertNotNull( down.getException() );
    }

    @Test
    public void testDownloadArtifactNoChecksumAvailable()
        throws Exception
    {
        addDelivery( "gid/aid/version/aid-version-classifier.extension", "artifact" );

        File f = TestFileUtils.createTempFile( "" );
        Artifact a = artifact( "foo" );

        ArtifactDownload down = new ArtifactDownload( a, null, f, RepositoryPolicy.CHECKSUM_POLICY_FAIL );
        Collection<? extends ArtifactDownload> downs = Arrays.asList( down );
        connector().get( downs, null );

        TestFileUtils.assertContent( "", f );
        assertNotNull( down.getException() );
    }

    @Test
    public void testDownloadCorrupted()
        throws Exception
    {
        addDelivery( "gid/aid/version/aid-version-classifier.extension", "artifact" );
        addDelivery( "gid/aid/version/aid-version-classifier.extension.sha1", "foo" );
        addDelivery( "gid/aid/version/aid-version-classifier.extension.md5", "bar" );

        File f = TestFileUtils.createTempFile( "" );
        Artifact a = artifact( "bla" );

        ArtifactDownload down = new ArtifactDownload( a, null, f, RepositoryPolicy.CHECKSUM_POLICY_WARN );
        Collection<? extends ArtifactDownload> downs = Arrays.asList( down );
        connector().get( downs, null );

        TransferEvent corruptedEvent = null;
        for ( TransferEvent e : transferListener.getEvents() )
        {
            if ( TransferEvent.EventType.CORRUPTED.equals( e.getType() ) )
            {
                corruptedEvent = e;
                break;
            }
        }
        assertNotNull( corruptedEvent );
    }

    @Test
    public void testDownloadArtifactWithWait()
        throws Exception
    {
        addDelivery( "gid/aid/version/aid-version-classifier.extension", "artifact" );
        addDelivery( "gid/aid/version/aid-version-classifier.extension.sha1", sha1( "artifact" ) );
        addDelivery( "gid/aid/version/aid-version-classifier.extension.md5", md5( "artifact" ) );

        File f = TestFileUtils.createTempFile( "" );
        Artifact a = artifact( "foo" );

        ArtifactDownload down = new ArtifactDownload( a, null, f, RepositoryPolicy.CHECKSUM_POLICY_FAIL );
        Collection<? extends ArtifactDownload> downs = Arrays.asList( down );
        connector().get( downs, null );

        assertNull( String.valueOf( down.getException() ), down.getException() );
        TestFileUtils.assertContent( "foo", a.getFile() );
        TestFileUtils.assertContent( "artifact", f );
    }

    @Test( expected = IllegalStateException.class )
    public void testClosedGet()
        throws Exception
    {
        connector().close();

        File f = TestFileUtils.createTempFile( "" );
        Artifact a = artifact( "foo" );

        ArtifactDownload down = new ArtifactDownload( a, null, f, RepositoryPolicy.CHECKSUM_POLICY_FAIL );
        Collection<? extends ArtifactDownload> downs = Arrays.asList( down );
        connector().get( downs, null );
    }

    @Test
    public void testCloseAfterArtifactDownload()
        throws Exception
    {
        File f = TestFileUtils.createTempFile( "" );
        Artifact a = artifact( "foo" );

        ArtifactDownload down = new ArtifactDownload( a, null, f, RepositoryPolicy.CHECKSUM_POLICY_FAIL );
        Collection<? extends ArtifactDownload> downs = Arrays.asList( down );
        connector().get( downs, null );
        connector().close();
    }

}
