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

import org.junit.Test;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.spi.connector.ArtifactDownload;
import org.sonatype.aether.spi.connector.ArtifactUpload;
import org.sonatype.aether.spi.log.Logger;
import org.sonatype.aether.test.impl.TestFileProcessor;
import org.sonatype.aether.test.util.TestFileUtils;
import org.sonatype.aether.test.util.impl.StubArtifact;
import org.sonatype.aether.transfer.ArtifactNotFoundException;
import org.sonatype.aether.util.DefaultRepositorySystemSession;

import java.io.File;
import java.util.Arrays;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class AsyncHandlerExceptionTest
{

    @Test
    public void testIt()
        throws Exception
    {
        HttpServer server = new HttpServer();
        server.addResources( "/", "target/http" );
        server.start();

        try
        {
            RemoteRepository repo = new RemoteRepository( "id", "default", server.getHttpUrl() + "/repo" );
            RepositorySystemSession session = new DefaultRepositorySystemSession();

            AsyncRepositoryConnector connector =
                new AsyncRepositoryConnector( repo, session, new TestFileProcessor(), new StubLogger() );

            try
            {
                StubArtifact artifact = new StubArtifact( "gid:aid:1.0" );
                for ( int i = 0; i < 16; i++ )
                {
                    System.out.println( "RUN #" + i );
                    TestFileUtils.delete( new File( "target/http" ) );

                    ArtifactDownload download =
                        new ArtifactDownload( artifact, "project", new File( "target/a.jar" ), "ignore" );
                    System.out.println( "GET" );
                    connector.get( Arrays.asList( download ), null );
                    assertTrue( String.valueOf( download.getException() ),
                                download.getException() instanceof ArtifactNotFoundException );

                    ArtifactUpload upload = new ArtifactUpload( artifact, new File( "pom.xml" ) );
                    System.out.println( "PUT" );
                    connector.put( Arrays.asList( upload ), null );
                    if ( upload.getException() != null )
                    {
                        upload.getException().printStackTrace();
                    }
                    assertNull( String.valueOf( upload.getException() ), upload.getException() );
                }
            }
            finally
            {
                connector.close();
            }
        }
        finally
        {
            server.stop();
        }
    }

    private static class StubLogger
        implements Logger
    {

        public void debug( String msg )
        {
            debug( msg, null );
        }

        public void debug( String msg, Throwable t )
        {
            System.out.println( msg );
            if ( t != null )
            {
                t.printStackTrace( System.out );
            }
        }

        public boolean isDebugEnabled()
        {
            return true;
        }

    }
}
