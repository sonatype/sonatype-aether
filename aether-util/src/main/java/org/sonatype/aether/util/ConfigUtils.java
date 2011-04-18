package org.sonatype.aether.util;

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
     * @param key The property to read, must not be {@code null}.
     * @param defaultValue The default value to return in case the property isn't set, may be {@code null}.
     * @return The property value or {@code null} if none.
     */
    public static String get( Map<?, ?> properties, String key, String defaultValue )
    {
        Object value = properties.get( key );

        if ( value instanceof String )
        {
            return (String) value;
        }

        return defaultValue;
    }

    /**
     * Gets the specified configuration property.
     * 
     * @param session The repository system session from which to read the configuration property, must not be
     *            {@code null}.
     * @param key The property to read, must not be {@code null}.
     * @param defaultValue The default value to return in case the property isn't set, may be {@code null}.
     * @return The property value or {@code null} if none.
     */
    public static String get( RepositorySystemSession session, String key, String defaultValue )
    {
        return get( session.getConfigProperties(), key, defaultValue );
    }

    /**
     * Gets the specified configuration property.
     * 
     * @param properties The configuration properties to read, must not be {@code null}.
     * @param key The property to read, must not be {@code null}.
     * @param defaultValue The default value to return in case the property isn't set.
     * @return The property value.
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
     * @param session The repository system session from which to read the configuration property, must not be
     *            {@code null}.
     * @param key The property to read, must not be {@code null}.
     * @param defaultValue The default value to return in case the property isn't set.
     * @return The property value.
     */
    public static int get( RepositorySystemSession session, String key, int defaultValue )
    {
        return get( session.getConfigProperties(), key, defaultValue );
    }

    /**
     * Gets the specified configuration property.
     * 
     * @param properties The configuration properties to read, must not be {@code null}.
     * @param key The property to read, must not be {@code null}.
     * @param defaultValue The default value to return in case the property isn't set.
     * @return The property value.
     */
    public static boolean get( Map<?, ?> properties, String key, boolean defaultValue )
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

        return defaultValue;
    }

    /**
     * Gets the specified configuration property.
     * 
     * @param session The repository system session from which to read the configuration property, must not be
     *            {@code null}.
     * @param key The property to read, must not be {@code null}.
     * @param defaultValue The default value to return in case the property isn't set.
     * @return The property value.
     */
    public static boolean get( RepositorySystemSession session, String key, boolean defaultValue )
    {
        return get( session.getConfigProperties(), key, defaultValue );
    }

    /**
     * Gets the specified configuration property.
     * 
     * @param properties The configuration properties to read, must not be {@code null}.
     * @param key The property to read, must not be {@code null}.
     * @param defaultValue The default value to return in case the property isn't set, may be {@code null}.
     * @return The property value or {@code null} if none.
     */
    @SuppressWarnings( "unchecked" )
    public static <E> List<E> get( Map<?, ?> properties, String key, List<E> defaultValue )
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

        return defaultValue;
    }

    /**
     * Gets the specified configuration property.
     * 
     * @param session The repository system session from which to read the configuration property, must not be
     *            {@code null}.
     * @param key The property to read, must not be {@code null}.
     * @param defaultValue The default value to return in case the property isn't set, may be {@code null}.
     * @return The property value or {@code null} if none.
     */
    public static <E> List<E> get( RepositorySystemSession session, String key, List<E> defaultValue )
    {
        return get( session.getConfigProperties(), key, defaultValue );
    }

    /**
     * Gets the specified configuration property.
     * 
     * @param properties The configuration properties to read, must not be {@code null}.
     * @param key The property to read, must not be {@code null}.
     * @param defaultValue The default value to return in case the property isn't set, may be {@code null}.
     * @return The property value or {@code null} if none.
     */
    @SuppressWarnings( "unchecked" )
    public static <K, V> Map<K, V> get( Map<?, ?> properties, String key, Map<K, V> defaultValue )
    {
        Object value = properties.get( key );

        if ( value instanceof Map )
        {
            return (Map<K, V>) value;
        }

        return defaultValue;
    }

    /**
     * Gets the specified configuration property.
     * 
     * @param session The repository system session from which to read the configuration property, must not be
     *            {@code null}.
     * @param key The property to read, must not be {@code null}.
     * @param defaultValue The default value to return in case the property isn't set, may be {@code null}.
     * @return The property value or {@code null} if none.
     */
    public static <K, V> Map<K, V> get( RepositorySystemSession session, String key, Map<K, V> defaultValue )
    {
        return get( session.getConfigProperties(), key, defaultValue );
    }

}
