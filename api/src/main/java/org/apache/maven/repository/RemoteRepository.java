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

/**
 * @author Benjamin Bentmann
 */
public class RemoteRepository
    implements ArtifactRepository
{

    private String id;

    private String type;

    private String url;

    private RepositoryPolicy releasePolicy;

    private RepositoryPolicy snapshotPolicy;

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
        this.id = id;

        return this;
    }

    public String getType()
    {
        return type;
    }

    public RemoteRepository setType( String type )
    {
        this.type = type;

        return this;
    }

    public String getUrl()
    {
        return url;
    }

    public RemoteRepository setUrl( String url )
    {
        this.url = url;

        return this;
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

}
