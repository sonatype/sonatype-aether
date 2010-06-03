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

/**
 * A proxy to use for connections to a repository.
 * 
 * @author Benjamin Bentmann
 */
public class Proxy
{

    private String type = "";

    private String host = "";

    private int port;

    private Authentication auth;

    /**
     * Creates a new uninitialized proxy.
     */
    public Proxy()
    {
        // enables default constructor
    }

    /**
     * Creates a new proxy with the specified properties.
     * 
     * @param type The type of the proxy, e.g. "http", may be {@code null}.
     * @param host The host of the proxy, may be {@code null}.
     * @param port The port of the proxy.
     * @param auth The authentication to use for the proxy connection, may be {@code null}.
     */
    public Proxy( String type, String host, int port, Authentication auth )
    {
        setType( type );
        setHost( host );
        setPort( port );
        setAuthentication( auth );
    }

    /**
     * Creates a deep copy of the specified proxy.
     * 
     * @param proxy The proxy to copy, must not be {@code null}.
     */
    public Proxy( Proxy proxy )
    {
        setType( proxy.getType() );
        setHost( proxy.getHost() );
        setPort( proxy.getPort() );
        if ( proxy.getAuthentication() != null )
        {
            setAuthentication( new Authentication( proxy.getAuthentication() ) );
        }
    }

    /**
     * Gets the type of this proxy.
     * 
     * @return The type of this proxy, never {@code null}.
     */
    public String getType()
    {
        return type;
    }

    /**
     * Sets the type of this proxy.
     * 
     * @param type The type of the proxy, e.g. "http", may be {@code null}.
     * @return This proxy for chaining, never {@code null}.
     */
    public Proxy setType( String type )
    {
        this.type = ( type != null ) ? type : "";

        return this;
    }

    /**
     * Gets the host for this proxy.
     * 
     * @return The host for this proxy, never {@code null}.
     */
    public String getHost()
    {
        return host;
    }

    /**
     * Sets the host of this proxy.
     * 
     * @param host The host of this proxy, may be {@code null}.
     * @return This proxy for chaining, never {@code null}.
     */
    public Proxy setHost( String host )
    {
        this.host = ( host != null ) ? host : "";

        return this;
    }

    /**
     * Gets the port number for this proxy.
     * 
     * @return The port number for this proxy.
     */
    public int getPort()
    {
        return port;
    }

    /**
     * Sets the port number for this proxy.
     * 
     * @param port The port number for this proxy.
     * @return This proxy for chaining, never {@code null}.
     */
    public Proxy setPort( int port )
    {
        this.port = port;

        return this;
    }

    /**
     * Gets the authentication to use for the proxy connection.
     * 
     * @return The authentication to use or {@code null} if none.
     */
    public Authentication getAuthentication()
    {
        return auth;
    }

    /**
     * Sets the authentication to use for the proxy connection.
     * 
     * @param auth The authentication to use, may be {@code null}.
     * @return This proxy for chaining, never {@code null}.
     */
    public Proxy setAuthentication( Authentication auth )
    {
        this.auth = auth;

        return this;
    }

    @Override
    public String toString()
    {
        return getHost() + ':' + getPort();
    }

}
