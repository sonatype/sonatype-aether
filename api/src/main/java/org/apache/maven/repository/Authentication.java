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
public class Authentication
{

    private String username;

    private String password;

    private String privateKeyFile;

    private String passphrase;

    public Authentication()
    {
        // enables default constructor
    }

    public Authentication( Authentication auth )
    {
        setUsername( auth.getUsername() );
        setPassword( auth.getPassword() );
        setPrivateKeyFile( auth.getPrivateKeyFile() );
        setPassphrase( auth.getPassphrase() );
    }

    public Authentication( String username, String password )
    {
        setUsername( username );
        setPassword( password );
    }

    public String getUsername()
    {
        return username;
    }

    public Authentication setUsername( String username )
    {
        this.username = username;

        return this;
    }

    public String getPassword()
    {
        return password;
    }

    public Authentication setPassword( String password )
    {
        this.password = password;

        return this;
    }

    public String getPrivateKeyFile()
    {
        return privateKeyFile;
    }

    public Authentication setPrivateKeyFile( String privateKeyFile )
    {
        this.privateKeyFile = privateKeyFile;

        return this;
    }

    public String getPassphrase()
    {
        return passphrase;
    }

    public Authentication setPassphrase( String passphrase )
    {
        this.passphrase = passphrase;

        return this;
    }

}
