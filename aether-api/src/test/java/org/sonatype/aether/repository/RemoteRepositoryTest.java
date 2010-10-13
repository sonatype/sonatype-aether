package org.sonatype.aether.repository;

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

import org.junit.Test;

/**
 * @author Benjamin Bentmann
 */
public class RemoteRepositoryTest
{

    @Test
    public void testGetProtocol()
    {
        RemoteRepository repo = new RemoteRepository( "id", "type", "" );
        assertEquals( "", repo.getProtocol() );

        repo = repo.setUrl( "http://localhost" );
        assertEquals( "http", repo.getProtocol() );

        repo = repo.setUrl( "HTTP://localhost" );
        assertEquals( "HTTP", repo.getProtocol() );

        repo = repo.setUrl( "dav+http://www.sonatype.org/" );
        assertEquals( "dav+http", repo.getProtocol() );

        repo = repo.setUrl( "dav:http://www.sonatype.org/" );
        assertEquals( "dav:http", repo.getProtocol() );

        repo = repo.setUrl( "file:/path" );
        assertEquals( "file", repo.getProtocol() );

        repo = repo.setUrl( "file:path" );
        assertEquals( "file", repo.getProtocol() );

        repo = repo.setUrl( "file:C:\\dir" );
        assertEquals( "file", repo.getProtocol() );

        repo = repo.setUrl( "file:C:/dir" );
        assertEquals( "file", repo.getProtocol() );
    }

    @Test
    public void testGetHost()
    {
        RemoteRepository repo = new RemoteRepository( "id", "type", "" );
        assertEquals( "", repo.getHost() );

        repo = repo.setUrl( "http://localhost" );
        assertEquals( "localhost", repo.getHost() );

        repo = repo.setUrl( "http://localhost/" );
        assertEquals( "localhost", repo.getHost() );

        repo = repo.setUrl( "http://localhost:1234/" );
        assertEquals( "localhost", repo.getHost() );

        repo = repo.setUrl( "http://127.0.0.1" );
        assertEquals( "127.0.0.1", repo.getHost() );

        repo = repo.setUrl( "http://127.0.0.1/" );
        assertEquals( "127.0.0.1", repo.getHost() );

        repo = repo.setUrl( "http://user@localhost/path" );
        assertEquals( "localhost", repo.getHost() );

        repo = repo.setUrl( "http://user:pass@localhost/path" );
        assertEquals( "localhost", repo.getHost() );

        repo = repo.setUrl( "http://user:pass@localhost:1234/path" );
        assertEquals( "localhost", repo.getHost() );
    }

}
