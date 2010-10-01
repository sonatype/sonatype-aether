package org.sonatype.aether.impl.internal;

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
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sonatype.aether.metadata.Metadata;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.MetadataRequest;
import org.sonatype.aether.resolution.MetadataResult;
import org.sonatype.aether.spi.connector.ArtifactDownload;
import org.sonatype.aether.spi.connector.MetadataDownload;
import org.sonatype.aether.spi.log.NullLogger;
import org.sonatype.aether.test.impl.TestRepositorySystemSession;
import org.sonatype.aether.test.util.TestFileUtils;
import org.sonatype.aether.test.util.impl.StubMetadata;
import org.sonatype.aether.transfer.MetadataNotFoundException;

/**
 * @author Benjamin Hanzelmann
 */
public class DefaultMetadataResolverTest
{

    private DefaultMetadataResolver resolver;

    private StubRemoteRepositoryManager manager;

    private RemoteRepository repository;

    private TestRepositorySystemSession session;

    private Metadata metadata;

    private RecordingRepositoryConnector connector;

    @Before
    public void setup()
        throws Exception
    {
        session = new TestRepositorySystemSession();
        manager = new StubRemoteRepositoryManager();
        resolver = new DefaultMetadataResolver( NullLogger.INSTANCE, new StaticUpdateCheckManager( true ), manager );
        repository = new RemoteRepository( "test-DMRT", "default", new File( "target/test-DMRT" ).toURI().toURL().toString() );
        metadata = new StubMetadata( "gid", "aid", "ver", "maven-metadata.xml", Metadata.Nature.RELEASE_OR_SNAPSHOT );
        connector = new RecordingRepositoryConnector();
        manager.setConnector( connector );
    }

    @After
    public void teardown()
    {
        TestFileUtils.deleteDir( new File( "target/test-DMRT" ) );
        TestFileUtils.deleteDir( new File( "target/test-local-repository" ) );
    }

    @Test
    public void testNoRepositoryFailing()
    {
        MetadataRequest request = new MetadataRequest( metadata, null, "" );
        List<MetadataResult> results = resolver.resolveMetadata( session, Arrays.asList( request ) );

        assertEquals( 1, results.size() );

        MetadataResult result = results.get( 0 );
        assertEquals( request, result.getRequest() );
        assertNotNull( "" + ( result.getMetadata() != null ? result.getMetadata().getFile() : result.getMetadata() ),
                       result.getException() );
        assertEquals( MetadataNotFoundException.class, result.getException().getClass() );

        assertNull( result.getMetadata() );
    }

    @Test
    public void testResolve()
        throws IOException
    {
        connector.setExpectGet( metadata );

        // prepare "download"
        File file = new File( "target/test-local-repository/gid/aid/ver/gid-aid-ver.pom" );
        TestFileUtils.write( file.getAbsolutePath(), file );

        MetadataRequest request = new MetadataRequest( metadata, repository, "" );
        List<MetadataResult> results = resolver.resolveMetadata( session, Arrays.asList( request ) );

        assertEquals( 1, results.size() );

        MetadataResult result = results.get( 0 );
        assertEquals( request, result.getRequest() );
        assertNull( result.getException() );
        assertNotNull( result.getMetadata() );
        assertNotNull( result.getMetadata().getFile() );

        assertEquals( file, result.getMetadata().getFile() );
        assertEquals( metadata, result.getMetadata().setFile( null ) );

        connector.assertSeenExpected();

    }

    @Test
    public void testRemoveMetadataIfMissing()
        throws IOException
    {
        connector = new RecordingRepositoryConnector()
        {

            @Override
            public void get( Collection<? extends ArtifactDownload> artifactDownloads,
                             Collection<? extends MetadataDownload> metadataDownloads )
            {
                super.get( artifactDownloads, metadataDownloads );
                for ( MetadataDownload d : metadataDownloads )
                {
                    d.setException( new MetadataNotFoundException( metadata, repository ) );
                }
            }

        };
        manager.setConnector( connector );

        File file = new File( "target/test-local-repository/gid/aid/ver/gid-aid-ver.pom" );
        TestFileUtils.write( file.getAbsolutePath(), file );
        metadata.setFile( file );

        MetadataRequest request = new MetadataRequest( metadata, repository, "" );
        request.setDeleteLocalCopyIfMissing( true );

        List<MetadataResult> results = resolver.resolveMetadata( session, Arrays.asList( request ) );
        assertEquals( 1, results.size() );
        MetadataResult result = results.get( 0 );

        assertNotNull( result.getException() );
        assertEquals( false, file.exists() );
    }
}
