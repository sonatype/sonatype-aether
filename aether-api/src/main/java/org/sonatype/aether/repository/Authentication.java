package org.sonatype.aether.repository;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *******************************************************************************/

import java.util.Arrays;

/**
 * The authentication to use for accessing a protected resource. <em>Note:</em> Instances of this class are immutable
 * and the exposed mutators return new objects rather than changing the current instance.
 * 
 * @author Benjamin Bentmann
 */
public final class Authentication
{

    private final String username;

    private final char[] password;

    private final String privateKeyFile;

    private final char[] passphrase;

    private static char[] toChars( String str )
    {
        return ( str != null ) ? str.toCharArray() : null;
    }

    private static String toStr( char[] chars )
    {
        return ( chars != null ) ? new String( chars ) : null;
    }

    /**
     * Creates a new authentication with the specified properties
     * 
     * @param username The username, may be {@code null}.
     * @param password The password, may be {@code null}.
     * @param privateKeyFile The path to the private key file, may be {@code null}.
     * @param passphrase The passphrase for the private key file, may be {@code null}.
     */
    public Authentication( String username, char[] password, String privateKeyFile, char[] passphrase )
    {
        this.username = username;
        this.password = password;
        this.privateKeyFile = privateKeyFile;
        this.passphrase = passphrase;
    }

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
        this( username, toChars( password ), privateKeyFile, toChars( passphrase ) );
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
     * Creates a basic username+password authentication.
     * 
     * @param username The username, may be {@code null}.
     * @param password The password, may be {@code null}.
     */
    public Authentication( String username, char[] password )
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
        if ( eq( this.username, username ) )
        {
            return this;
        }
        return new Authentication( username, password, privateKeyFile, passphrase );
    }

    /**
     * Gets the password.
     * 
     * @return The password or {@code null} if none.
     */
    public String getPassword()
    {
        return toStr( password );
    }

    /**
     * Sets the password to use for authentication.
     * 
     * @param password The password, may be {@code null}.
     * @return The new authentication, never {@code null}.
     */
    public Authentication setPassword( String password )
    {
        return setPassword( toChars( password ) );
    }

    /**
     * Sets the password to use for authentication.
     * 
     * @param password The password, may be {@code null}.
     * @return The new authentication, never {@code null}.
     */
    public Authentication setPassword( char[] password )
    {
        if ( Arrays.equals( this.password, password ) )
        {
            return this;
        }
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
        if ( eq( this.privateKeyFile, privateKeyFile ) )
        {
            return this;
        }
        return new Authentication( username, password, privateKeyFile, passphrase );
    }

    /**
     * Gets the passphrase for the private key.
     * 
     * @return The passphrase for the private key or {@code null} if none.
     */
    public String getPassphrase()
    {
        return toStr( passphrase );
    }

    /**
     * Sets the passphrase for the private key file.
     * 
     * @param passphrase The passphrase for the private key file, may be {@code null}.
     * @return The new authentication, never {@code null}.
     */
    public Authentication setPassphrase( String passphrase )
    {
        return setPassphrase( toChars( passphrase ) );
    }

    /**
     * Sets the passphrase for the private key file.
     * 
     * @param passphrase The passphrase for the private key file, may be {@code null}.
     * @return The new authentication, never {@code null}.
     */
    public Authentication setPassphrase( char[] passphrase )
    {
        if ( Arrays.equals( this.passphrase, passphrase ) )
        {
            return this;
        }
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

        return eq( username, that.username ) && Arrays.equals( password, that.password )
            && eq( privateKeyFile, that.privateKeyFile ) && Arrays.equals( passphrase, passphrase );
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
        hash = hash * 31 + Arrays.hashCode( password );
        hash = hash * 31 + hash( privateKeyFile );
        return hash;
    }

    private static int hash( Object obj )
    {
        return obj != null ? obj.hashCode() : 0;
    }

}
