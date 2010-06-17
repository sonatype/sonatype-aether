package org.sonatype.maven.repository;

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

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A repository on a remote server.
 * 
 * @author Benjamin Bentmann
 */
public class RemoteRepository
    implements ArtifactRepository
{

    private String id = "";

    private String type = "";

    private String url = "";

    private String protocol = "";

    private String host = "";

    private RepositoryPolicy releasePolicy;

    private RepositoryPolicy snapshotPolicy;

    private Proxy proxy;

    private Authentication authentication;

    private List<RemoteRepository> mirroredRepositories = Collections.emptyList();

    private boolean repositoryManager;

    /**
     * Creates a new repository using the default release/snapshot policies.
     */
    public RemoteRepository()
    {
        setPolicy( true, null );
        setPolicy( false, null );
    }

    /**
     * Creates a shallow copy of the specified repository.
     * 
     * @param repo The repository to copy, must not be {@code null}.
     */
    public RemoteRepository( RemoteRepository repo )
    {
        setId( repo.getId() );
        setContentType( repo.getContentType() );
        setUrl( repo.getUrl() );
        setPolicy( true, repo.getPolicy( true ) );
        setPolicy( false, repo.getPolicy( false ) );
        setAuthentication( repo.getAuthentication() );
        setProxy( repo.getProxy() );
        setMirroredRepositories( repo.getMirroredRepositories() );
        setRepositoryManager( repo.isRepositoryManager() );
    }

    /**
     * Creates a new repository with the specified properties and the default policies.
     * 
     * @param id The identifier of the repository, may be {@code null}.
     * @param type The type of the repository, may be {@code null}.
     * @param url The (base) URL of the repository, may be {@code null}.
     */
    public RemoteRepository( String id, String type, String url )
    {
        setId( id );
        setContentType( type );
        setUrl( url );
        setPolicy( true, null );
        setPolicy( false, null );
    }

    public String getId()
    {
        return id;
    }

    /**
     * Sets the identifier of this repository.
     * 
     * @param id The identifier of this repository, may be {@code null}.
     * @return This repository for chaining, never {@code null}.
     */
    public RemoteRepository setId( String id )
    {
        this.id = ( id != null ) ? id : "";

        return this;
    }

    public String getContentType()
    {
        return type;
    }

    /**
     * Sets the type of this repository, e.g. "default".
     * 
     * @param type The type of this repository, may be {@code null}.
     * @return This repository for chaining, never {@code null}.
     */
    public RemoteRepository setContentType( String type )
    {
        this.type = ( type != null ) ? type : "";

        return this;
    }

    /**
     * Gets the (base) URL of this repository.
     * 
     * @return The (base) URL of this repository, never {@code null}.
     */
    public String getUrl()
    {
        return url;
    }

    /**
     * Sets the (base) URL of this repository.
     * 
     * @param url The URL of this repository, may be {@code null}.
     * @return This repository for chaining, never {@code null}.
     */
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

    /**
     * Gets the protocol part from the repository's URL.
     * 
     * @return The protocol or an empty string if none, never {@code null}.
     */
    public String getProtocol()
    {
        return protocol;
    }

    /**
     * Gets the host part from the repository's URL.
     * 
     * @return The host or an empty string if none, never {@code null}.
     */
    public String getHost()
    {
        return host;
    }

    /**
     * Gets the policy to apply for snapshot/release artifacts.
     * 
     * @param snapshot {@code true} to retrieve the snapshot policy, {@code false} to retrieve the release policy.
     * @return The requested repository policy, never {@code null}.
     */
    public RepositoryPolicy getPolicy( boolean snapshot )
    {
        return snapshot ? snapshotPolicy : releasePolicy;
    }

    /**
     * Sets the policy to apply for snapshot/release artifacts.
     * 
     * @param snapshot {@code true} to set the snapshot policy, {@code false} to seg the release policy.
     * @param policy The repository policy to set, may be {@code null} to use a default policy.
     * @return This repository for chaining, never {@code null}.
     */
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

    /**
     * Gets the proxy that has been selected for this repository.
     * 
     * @return The selected proxy or {@code null} if none.
     */
    public Proxy getProxy()
    {
        return proxy;
    }

    /**
     * Sets the proxy to use in order to access this repository.
     * 
     * @param proxy The proxy to use, may be {@code null}.
     * @return This repository for chaining, never {@code null}.
     */
    public RemoteRepository setProxy( Proxy proxy )
    {
        this.proxy = proxy;

        return this;
    }

    /**
     * Gets the authentication that has been selected for this repository.
     * 
     * @return The selected authentication or {@code null} if none.
     */
    public Authentication getAuthentication()
    {
        return authentication;
    }

    /**
     * Sets the authentication to use in order to access this repository.
     * 
     * @param authentication The authentication to use, may be {@code null}.
     * @return This repository for chaining, never {@code null}.
     */
    public RemoteRepository setAuthentication( Authentication authentication )
    {
        this.authentication = authentication;

        return this;
    }

    /**
     * Gets the repositories that this repository serves as a mirror for.
     * 
     * @return The repositories being mirrored by this repository, never {@code null}.
     */
    public List<RemoteRepository> getMirroredRepositories()
    {
        return mirroredRepositories;
    }

    /**
     * Sets the repositories being mirrored by this repository.
     * 
     * @param mirroredRepositories The repositories being mirrored by this repository, may be {@code null}.
     * @return This repository for chaining, never {@code null}.
     */
    public RemoteRepository setMirroredRepositories( List<RemoteRepository> mirroredRepositories )
    {
        if ( mirroredRepositories == null )
        {
            this.mirroredRepositories = Collections.emptyList();
        }
        else
        {
            this.mirroredRepositories = mirroredRepositories;
        }
        return this;
    }

    /**
     * Indicates whether this repository refers to a repository manager or not.
     * 
     * @return {@code true} if this repository is a repository manager, {@code false} otherwise.
     */
    public boolean isRepositoryManager()
    {
        return repositoryManager;
    }

    /**
     * Marks this repository as a repository manager or not.
     * 
     * @param repositoryManager {@code true} if this repository points at a repository manager, {@code false} if the
     *            repository is just serving static contents.
     * @return This repository for chaining, never {@code null}.
     */
    public RemoteRepository setRepositoryManager( boolean repositoryManager )
    {
        this.repositoryManager = repositoryManager;
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
        buffer.append( ", managed=" ).append( isRepositoryManager() );
        buffer.append( ")" );
        return buffer.toString();
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj )
        {
            return true;
        }
        if ( obj == null || !getClass().equals( obj.getClass() ) )
        {
            return false;
        }

        RemoteRepository that = (RemoteRepository) obj;

        return eq( this.getUrl(), that.getUrl() ) && eq( this.getContentType(), that.getContentType() )
            && eq( this.getId(), that.getId() ) && eq( this.getPolicy( true ), that.getPolicy( true ) )
            && eq( this.getPolicy( false ), that.getPolicy( false ) ) && eq( this.getProxy(), that.getProxy() )
            && eq( this.getAuthentication(), that.getAuthentication() )
            && eq( this.getMirroredRepositories(), that.getMirroredRepositories() )
            && this.isRepositoryManager() == that.isRepositoryManager();
    }

    private static <T> boolean eq( T s1, T s2 )
    {
        return s1 != null ? s1.equals( s2 ) : s2 == null;
    }

    @Override
    public int hashCode()
    {
        int hash = 17;
        hash = hash * 31 + hash( getUrl() );
        hash = hash * 31 + hash( getContentType() );
        hash = hash * 31 + hash( getId() );
        hash = hash * 31 + hash( getPolicy( true ) );
        hash = hash * 31 + hash( getPolicy( false ) );
        hash = hash * 31 + hash( getProxy() );
        hash = hash * 31 + hash( getAuthentication() );
        hash = hash * 31 + hash( getMirroredRepositories() );
        hash = hash * 31 + ( isRepositoryManager() ? 1 : 0 );
        return hash;
    }

    private static int hash( Object obj )
    {
        return obj != null ? obj.hashCode() : 0;
    }

}
