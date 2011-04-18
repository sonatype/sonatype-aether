package org.sonatype.aether.util;

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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.sonatype.aether.RepositorySystemSession;

/**
 * A utility class to read configuration properties from a repository system session.
 * 
 * @author Benjamin Bentmann
 * @see RepositorySystemSession#getConfigProperties()
 */
public class ConfigUtils
{

    private ConfigUtils()
    {
        // hide constructor
    }

    /**
     * Gets the specified configuration property.
     * 
     * @param properties The configuration properties to read, must not be {@code null}.
     * @param defaultValue The default value to return in case the property isn't set, may be {@code null}.
     * @param keys The properties to read, must not be {@code null}. The specified keys are read one after one until a
     *            valid value is found.
     * @return The property value or {@code null} if none.
     */
    public static String get( Map<?, ?> properties, String defaultValue, String... keys )
    {
        for ( String key : keys )
        {
            Object value = properties.get( key );

            if ( value instanceof String )
            {
                return (String) value;
            }
        }

        return defaultValue;
    }

    /**
     * Gets the specified configuration property.
     * 
     * @param session The repository system session from which to read the configuration property, must not be
     *            {@code null}.
     * @param defaultValue The default value to return in case the property isn't set, may be {@code null}.
     * @param keys The properties to read, must not be {@code null}. The specified keys are read one after one until a
     *            valid value is found.
     * @return The property value or {@code null} if none.
     */
    public static String get( RepositorySystemSession session, String defaultValue, String... keys )
    {
        return get( session.getConfigProperties(), defaultValue, keys );
    }

    /**
     * Gets the specified configuration property.
     * 
     * @param properties The configuration properties to read, must not be {@code null}.
     * @param defaultValue The default value to return in case the property isn't set.
     * @param keys The properties to read, must not be {@code null}. The specified keys are read one after one until a
     *            valid value is found.
     * @return The property value.
     */
    public static int get( Map<?, ?> properties, int defaultValue, String... keys )
    {
        for ( String key : keys )
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
                // try next key
            }
        }

        return defaultValue;
    }

    /**
     * Gets the specified configuration property.
     * 
     * @param session The repository system session from which to read the configuration property, must not be
     *            {@code null}.
     * @param defaultValue The default value to return in case the property isn't set.
     * @param keys The properties to read, must not be {@code null}. The specified keys are read one after one until a
     *            valid value is found.
     * @return The property value.
     */
    public static int get( RepositorySystemSession session, int defaultValue, String... keys )
    {
        return get( session.getConfigProperties(), defaultValue, keys );
    }

    /**
     * Gets the specified configuration property.
     * 
     * @param properties The configuration properties to read, must not be {@code null}.
     * @param defaultValue The default value to return in case the property isn't set.
     * @param keys The properties to read, must not be {@code null}. The specified keys are read one after one until a
     *            valid value is found.
     * @return The property value.
     */
    public static boolean get( Map<?, ?> properties, boolean defaultValue, String... keys )
    {
        for ( String key : keys )
        {
            Object value = properties.get( key );

            if ( value instanceof Boolean )
            {
                return ( (Boolean) value ).booleanValue();
            }
            else if ( value instanceof String )
            {
                return Boolean.parseBoolean( (String) value );
            }
        }

        return defaultValue;
    }

    /**
     * Gets the specified configuration property.
     * 
     * @param session The repository system session from which to read the configuration property, must not be
     *            {@code null}.
     * @param defaultValue The default value to return in case the property isn't set.
     * @param keys The properties to read, must not be {@code null}. The specified keys are read one after one until a
     *            valid value is found.
     * @return The property value.
     */
    public static boolean get( RepositorySystemSession session, boolean defaultValue, String... keys )
    {
        return get( session.getConfigProperties(), defaultValue, keys );
    }

    /**
     * Gets the specified configuration property.
     * 
     * @param properties The configuration properties to read, must not be {@code null}.
     * @param defaultValue The default value to return in case the property isn't set, may be {@code null}.
     * @param keys The properties to read, must not be {@code null}. The specified keys are read one after one until a
     *            valid value is found.
     * @return The property value or {@code null} if none.
     */
    @SuppressWarnings( "unchecked" )
    public static <E> List<E> get( Map<?, ?> properties, List<E> defaultValue, String... keys )
    {
        for ( String key : keys )
        {
            Object value = properties.get( key );

            if ( value instanceof List )
            {
                return (List<E>) value;
            }
            else if ( value instanceof Collection )
            {
                return Collections.unmodifiableList( new ArrayList<E>( (Collection<E>) value ) );
            }
        }

        return defaultValue;
    }

    /**
     * Gets the specified configuration property.
     * 
     * @param session The repository system session from which to read the configuration property, must not be
     *            {@code null}.
     * @param defaultValue The default value to return in case the property isn't set, may be {@code null}.
     * @param keys The properties to read, must not be {@code null}. The specified keys are read one after one until a
     *            valid value is found.
     * @return The property value or {@code null} if none.
     */
    public static <E> List<E> get( RepositorySystemSession session, List<E> defaultValue, String... keys )
    {
        return get( session.getConfigProperties(), defaultValue, keys );
    }

    /**
     * Gets the specified configuration property.
     * 
     * @param properties The configuration properties to read, must not be {@code null}.
     * @param defaultValue The default value to return in case the property isn't set, may be {@code null}.
     * @param keys The properties to read, must not be {@code null}. The specified keys are read one after one until a
     *            valid value is found.
     * @return The property value or {@code null} if none.
     */
    @SuppressWarnings( "unchecked" )
    public static <K, V> Map<K, V> get( Map<?, ?> properties, Map<K, V> defaultValue, String... keys )
    {
        for ( String key : keys )
        {
            Object value = properties.get( key );

            if ( value instanceof Map )
            {
                return (Map<K, V>) value;
            }
        }

        return defaultValue;
    }

    /**
     * Gets the specified configuration property.
     * 
     * @param session The repository system session from which to read the configuration property, must not be
     *            {@code null}.
     * @param defaultValue The default value to return in case the property isn't set, may be {@code null}.
     * @param keys The properties to read, must not be {@code null}. The specified keys are read one after one until a
     *            valid value is found.
     * @return The property value or {@code null} if none.
     */
    public static <K, V> Map<K, V> get( RepositorySystemSession session, Map<K, V> defaultValue, String... keys )
    {
        return get( session.getConfigProperties(), defaultValue, keys );
    }

}
