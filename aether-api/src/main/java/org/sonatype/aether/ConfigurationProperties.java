package org.sonatype.aether;

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

import java.util.Map;

/**
 * The keys and defaults for common configuration properties.
 * 
 * @author Benjamin Bentmann
 * @see RepositorySystemSession#getConfigProperties()
 */
public final class ConfigurationProperties
{

    private static final String PREFIX_AETHER = "aether.";

    private static final String PREFIX_CONNECTOR = PREFIX_AETHER + "connector.";

    /**
     * A flag indicating whether interaction with the user is allowed.
     * 
     * @see #DEFAULT_INTERACTIVE
     */
    public static final String INTERACTIVE = PREFIX_AETHER + "interactive";

    /**
     * The default interactive mode if {@link #INTERACTIVE} isn't set.
     */
    public static final boolean DEFAULT_INTERACTIVE = false;

    /**
     * The user agent that repository connectors should reports to servers.
     * 
     * @see #DEFAULT_USER_AGENT
     */
    public static final String USER_AGENT = PREFIX_CONNECTOR + "userAgent";

    /**
     * The default user agent to use if {@link #USER_AGENT} isn't set.
     */
    public static final String DEFAULT_USER_AGENT = "Aether";

    /**
     * The timeout (in milliseconds) to wait for a successful connection to a remote server. Non-positive values
     * indicate no timeout.
     * 
     * @see #DEFAULT_CONNECT_TIMEOUT
     */
    public static final String CONNECT_TIMEOUT = PREFIX_CONNECTOR + "connectTimeout";

    /**
     * The default connect timeout to use if {@link #CONNECT_TIMEOUT} isn't set.
     */
    public static final int DEFAULT_CONNECT_TIMEOUT = 10 * 1000;

    /**
     * The timeout (in milliseconds) to wait for a response from a remote server. Non-positive values indicate no
     * timeout.
     * 
     * @see #DEFAULT_REQUEST_TIMEOUT
     */
    public static final String REQUEST_TIMEOUT = PREFIX_CONNECTOR + "requestTimeout";

    /**
     * The default request timeout to use if {@link #REQUEST_TIMEOUT} isn't set.
     */
    public static final int DEFAULT_REQUEST_TIMEOUT = 60 * 1000;

    private ConfigurationProperties()
    {
        // hide constructor
    }

    /**
     * Gets the specified configuration property.
     * 
     * @param properties The configuration properties to read, must not be {@code null}.
     * @param key The property to read, must not be {@code null}.
     * @param defaultValue The default value to return in case the property isn't set, may be {@code null}.
     * @return The property value or {@code null} if none.
     */
    public static String get( Map<?, ?> properties, String key, String defaultValue )
    {
        Object value = properties.get( key );

        if ( !( value instanceof String ) )
        {
            return defaultValue;
        }

        return (String) value;
    }

    /**
     * Gets the specified configuration property.
     * 
     * @param properties The configuration properties to read, must not be {@code null}.
     * @param key The property to read, must not be {@code null}.
     * @param defaultValue The default value to return in case the property isn't set, may be {@code null}.
     * @return The property value or {@code null} if none.
     */
    public static int get( Map<?, ?> properties, String key, int defaultValue )
    {
        Object value = properties.get( key );

        if ( value instanceof Number )
        {
            return ( (Number) value ).intValue();
        }

        try
        {
            return Integer.valueOf( (String) value );
        }
        catch ( Exception e )
        {
            return defaultValue;
        }
    }

    /**
     * Gets the specified configuration property.
     * 
     * @param properties The configuration properties to read, must not be {@code null}.
     * @param key The property to read, must not be {@code null}.
     * @param defaultValue The default value to return in case the property isn't set, may be {@code null}.
     * @return The property value or {@code null} if none.
     */
    public static boolean get( Map<?, ?> properties, String key, boolean defaultValue )
    {
        Object value = properties.get( key );

        if ( value instanceof Boolean )
        {
            return ( (Boolean) value ).booleanValue();
        }
        else if ( !( value instanceof String ) )
        {
            return defaultValue;
        }

        return Boolean.parseBoolean( (String) value );
    }

}
