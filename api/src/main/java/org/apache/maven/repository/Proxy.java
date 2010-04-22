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
public class Proxy
{

    private String type;

    private String host;

    private int port;

    private Authentication auth;

    public Proxy()
    {
        // enables default constructor
    }

    public Proxy( String type, String host, int port, Authentication auth )
    {
        setType( type );
        setHost( host );
        setPort( port );
        setAuthentication( auth );
    }

    public String getType()
    {
        return type;
    }

    public Proxy setType( String type )
    {
        this.type = ( type != null ) ? type : "";

        return this;
    }

    public String getHost()
    {
        return host;
    }

    public Proxy setHost( String host )
    {
        this.host = ( host != null ) ? host : "";

        return this;
    }

    public int getPort()
    {
        return port;
    }

    public Proxy setPort( int port )
    {
        this.port = port;

        return this;
    }

    public Authentication getAuthentication()
    {
        return auth;
    }

    public Proxy setAuthentication( Authentication auth )
    {
        this.auth = auth;

        return this;
    }

}
