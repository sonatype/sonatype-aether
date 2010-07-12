package org.sonatype.maven.repository;

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
 * The authentication to use for accessing a protected resource.
 * 
 * @author Benjamin Bentmann
 */
public final class Authentication
{

    private final String username;

    private final String password;

    private final String privateKeyFile;

    private final String passphrase;

    /**
     * Creates a new authentication with the specified properties
     * 
     * @param username The username, may be {@code null}.
     * @param password The password, may be {@code null}.
     * @param privateKeyFile The path to the private key file, may be {@code null}.
     * @param passphrase The passphrase for the private key file, may be {@code null}.
     */
    public Authentication( String username, String password, String privateKeyFile, String passphrase )
    {
        this.username = username;
        this.password = password;
        this.privateKeyFile = privateKeyFile;
        this.passphrase = passphrase;
    }

    /**
     * Creates a basic username+password authentication.
     * 
     * @param username The username, may be {@code null}.
     * @param password The password, may be {@code null}.
     */
    public Authentication( String username, String password )
    {
        this( username, password, null, null );
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
     * @return The new authentication, never {@code null}.
     */
    public Authentication setUsername( String username )
    {
        return new Authentication( username, password, privateKeyFile, passphrase );
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
     * @return The new authentication, never {@code null}.
     */
    public Authentication setPassword( String password )
    {
        return new Authentication( username, password, privateKeyFile, passphrase );
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
     * @return The new authentication, never {@code null}.
     */
    public Authentication setPrivateKeyFile( String privateKeyFile )
    {
        return new Authentication( username, password, privateKeyFile, passphrase );
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
     * @return The new authentication, never {@code null}.
     */
    public Authentication setPassphrase( String passphrase )
    {
        return new Authentication( username, password, privateKeyFile, passphrase );
    }

    @Override
    public String toString()
    {
        return getUsername();
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

        Authentication that = (Authentication) obj;

        return eq( username, that.username ) && eq( password, that.password )
            && eq( privateKeyFile, that.privateKeyFile ) && eq( passphrase, passphrase );
    }

    private static <T> boolean eq( T s1, T s2 )
    {
        return s1 != null ? s1.equals( s2 ) : s2 == null;
    }

    @Override
    public int hashCode()
    {
        int hash = 17;
        hash = hash * 31 + hash( username );
        hash = hash * 31 + hash( password );
        hash = hash * 31 + hash( privateKeyFile );
        return hash;
    }

    private static int hash( Object obj )
    {
        return obj != null ? obj.hashCode() : 0;
    }

}
