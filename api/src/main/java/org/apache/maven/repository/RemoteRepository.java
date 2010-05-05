package org.apache.maven.repository;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Benjamin Bentmann
 */
public class RemoteRepository
    implements ArtifactRepository
{

    // TODO: allow tracking of remote repositories mirrored by this one

    private String id;

    private String type;

    private String url;

    private String protocol;

    private String host;

    private RepositoryPolicy releasePolicy;

    private RepositoryPolicy snapshotPolicy;

    private Proxy proxy;

    private Authentication authentication;

    public RemoteRepository()
    {
        setPolicy( true, null );
        setPolicy( false, null );
    }

    public RemoteRepository( RemoteRepository repo )
    {
        setId( repo.getId() );
        setType( repo.getType() );
        setUrl( repo.getUrl() );
        setPolicy( true, repo.getPolicy( true ) );
        setPolicy( false, repo.getPolicy( false ) );
    }

    public RemoteRepository( String id, String type, String url )
    {
        setId( id );
        setType( type );
        setUrl( url );
        setPolicy( true, null );
        setPolicy( false, null );
    }

    public String getId()
    {
        return id;
    }

    public RemoteRepository setId( String id )
    {
        this.id = ( id != null ) ? id : "";

        return this;
    }

    public String getType()
    {
        return type;
    }

    public RemoteRepository setType( String type )
    {
        this.type = ( type != null ) ? type : "";

        return this;
    }

    public String getUrl()
    {
        return url;
    }

    public RemoteRepository setUrl( String url )
    {
        this.url = ( url != null ) ? url : "";

        Matcher m = Pattern.compile( "([^:/]+(:[^:/]+)*):(//([^@/]*@)?([^/:]+))?.*" ).matcher( this.url );

        if ( m.matches() )
        {
            this.protocol = m.group( 1 );
            this.host = ( m.group( 5 ) != null ) ? m.group( 5 ) : "";
        }
        else
        {
            this.host = this.protocol = "";
        }

        return this;
    }

    public String getProtocol()
    {
        return protocol;
    }

    public String getHost()
    {
        return host;
    }

    public RepositoryPolicy getPolicy( boolean snapshot )
    {
        return snapshot ? snapshotPolicy : releasePolicy;
    }

    public RemoteRepository setPolicy( boolean snapshot, RepositoryPolicy policy )
    {
        if ( policy == null )
        {
            policy = new RepositoryPolicy();
        }

        if ( snapshot )
        {
            snapshotPolicy = policy;
        }
        else
        {
            releasePolicy = policy;
        }

        return this;
    }

    public Proxy getProxy()
    {
        return proxy;
    }

    public RemoteRepository setProxy( Proxy proxy )
    {
        this.proxy = proxy;

        return this;
    }

    public Authentication getAuthentication()
    {
        return authentication;
    }

    public RemoteRepository setAuthentication( Authentication authentication )
    {
        this.authentication = authentication;

        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder buffer = new StringBuilder( 256 );
        buffer.append( getId() );
        buffer.append( " (" ).append( getUrl() );
        buffer.append( ", releases=" ).append( getPolicy( false ).isEnabled() );
        buffer.append( ", snapshots=" ).append( getPolicy( true ).isEnabled() );
        buffer.append( ")" );
        return buffer.toString();
    }

}
