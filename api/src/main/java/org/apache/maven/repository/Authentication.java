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
 * The authentication to use for accessing a protected resource.
 * 
 * @author Benjamin Bentmann
 */
public class Authentication
{

    private String username;

    private String password;

    private String privateKeyFile;

    private String passphrase;

    /**
     * Creates an uninitialized authentication.
     */
    public Authentication()
    {
        // enables default constructor
    }

    /**
     * Creates a copy of the specified authentication.
     * 
     * @param auth The authentication to copy, must not be {@code null}.
     */
    public Authentication( Authentication auth )
    {
        setUsername( auth.getUsername() );
        setPassword( auth.getPassword() );
        setPrivateKeyFile( auth.getPrivateKeyFile() );
        setPassphrase( auth.getPassphrase() );
    }

    /**
     * Creates a basic username+password authentication.
     * 
     * @param username The username, may be {@code null}.
     * @param password The password, may be {@code null}.
     */
    public Authentication( String username, String password )
    {
        setUsername( username );
        setPassword( password );
    }

    /**
     * Gets the username.
     * 
     * @return The username or {@code null} if none.
     */
    public String getUsername()
    {
        return username;
    }

    /**
     * Sets the username to use for authentication.
     * 
     * @param username The username, may be {@code null}.
     * @return This authentication for chaining, never {@code null}.
     */
    public Authentication setUsername( String username )
    {
        this.username = username;

        return this;
    }

    /**
     * Gets the password.
     * 
     * @return The password or {@code null} if none.
     */
    public String getPassword()
    {
        return password;
    }

    /**
     * Sets the password to use for authentication.
     * 
     * @param password The password, may be {@code null}.
     * @return This authentication for chaining, never {@code null}.
     */
    public Authentication setPassword( String password )
    {
        this.password = password;

        return this;
    }

    /**
     * Gets the path to the private key file to use for authentication.
     * 
     * @return The path to the private key file or {@code null} if none.
     */
    public String getPrivateKeyFile()
    {
        return privateKeyFile;
    }

    /**
     * Sets the path to the private key file to use for authentication.
     * 
     * @param privateKeyFile The path to the private key file, may be {@code null}.
     * @return This authentication for chaining, never {@code null}.
     */
    public Authentication setPrivateKeyFile( String privateKeyFile )
    {
        this.privateKeyFile = privateKeyFile;

        return this;
    }

    /**
     * Gets the passphrase for the private key.
     * 
     * @return The passphrase for the private key or {@code null} if none.
     */
    public String getPassphrase()
    {
        return passphrase;
    }

    /**
     * Sets the passphrase for the private key file.
     * 
     * @param passphrase The passphrase for the private key file, may be {@code null}.
     * @return This authentication for chaining, never {@code null}.
     */
    public Authentication setPassphrase( String passphrase )
    {
        this.passphrase = passphrase;

        return this;
    }

    @Override
    public String toString()
    {
        return getUsername();
    }

}
