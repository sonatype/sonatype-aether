package org.sonatype.aether.impl.internal;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sonatype.aether.metadata.Metadata;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.MetadataRequest;
import org.sonatype.aether.resolution.MetadataResult;
import org.sonatype.aether.spi.log.NullLogger;
import org.sonatype.aether.test.impl.TestRepositorySystemSession;
import org.sonatype.aether.test.util.FileUtil;
import org.sonatype.aether.test.util.impl.StubMetadata;
import org.sonatype.aether.transfer.MetadataNotFoundException;

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

    @Before
    public void setup()
        throws MalformedURLException
    {
        session = new TestRepositorySystemSession();
        manager = new StubRemoteRepositoryManager();
        resolver = new DefaultMetadataResolver( NullLogger.INSTANCE, new DoNothingUpdateCheckManager(), manager );
        repository = new RemoteRepository( "test-DMRT", "default", new File( "target/test-DMRT" ).toURL().toString() );
        metadata = new StubMetadata( "gid", "aid", "ver", "maven-metadata.xml", Metadata.Nature.RELEASE_OR_SNAPSHOT );
    }

    @After
    public void teardown()
    {
        FileUtil.deleteDir( new File( "target/test-DMRT" ) );
    }

    @Test
    public void testNoRepositoryFailing()
    {
        MetadataRequest request = new MetadataRequest( metadata, null, "" );
        List<MetadataResult> results = resolver.resolveMetadata( session, Arrays.asList( request ) );

        assertEquals( 1, results.size() );

        MetadataResult result = results.get( 0 );
        assertEquals( request, result.getRequest() );
        assertNotNull( result.getException() );
        assertEquals( MetadataNotFoundException.class, result.getException().getClass() );

        assertNull( result.getMetadata() );
    }

    @Test
    public void testResolve()
        throws IOException
    {
        RecordingRepositoryConnector connector = new RecordingRepositoryConnector();
        connector.setExpectGet( metadata );
        manager.setConnector( connector );

        // prepare "download"
        File file = new File( "target/test-local-repository/gid/aid/ver/gid-aid-ver.pom" );
        file.getParentFile().mkdirs();
        file.createNewFile();

        MetadataRequest request = new MetadataRequest( metadata, repository, "" );
        List<MetadataResult> results = resolver.resolveMetadata( session, Arrays.asList( request ) );

        assertEquals( 1, results.size() );

        MetadataResult result = results.get( 0 );
        assertEquals( request, result.getRequest() );
        assertNull( result.getException() );
        assertNotNull( result.getMetadata() );
        assertNotNull( result.getMetadata().getFile() );

        assertEquals( metadata, result.getMetadata().setFile( null ) );

        connector.assertSeenExpected();

    }
}
