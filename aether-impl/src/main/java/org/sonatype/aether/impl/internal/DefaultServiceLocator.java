package org.sonatype.aether.impl.internal;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.impl.ArtifactResolver;
import org.sonatype.aether.impl.DependencyCollector;
import org.sonatype.aether.impl.Deployer;
import org.sonatype.aether.impl.Installer;
import org.sonatype.aether.impl.MetadataResolver;
import org.sonatype.aether.impl.RemoteRepositoryManager;
import org.sonatype.aether.impl.UpdateCheckManager;
import org.sonatype.aether.spi.locator.Service;
import org.sonatype.aether.spi.locator.ServiceLocator;

/**
 * A simple service locator that is already setup with all components from this library. To acquire a complete
 * repository system, clients need to add an artifact descriptor, a version resolver, a version range resolver and some
 * repository connectors.
 * 
 * @author Benjamin Bentmann
 */
public class DefaultServiceLocator
    implements ServiceLocator
{

    private Map<Class<?>, Collection<Class<?>>> classes;

    private Map<Class<?>, List<?>> instances;

    /**
     * Creates a new service locator that already knows about all service implementations included this library.
     */
    public DefaultServiceLocator()
    {
        classes = new HashMap<Class<?>, Collection<Class<?>>>();
        instances = new HashMap<Class<?>, List<?>>();

        addService( RepositorySystem.class, DefaultRepositorySystem.class );
        addService( ArtifactResolver.class, DefaultArtifactResolver.class );
        addService( DependencyCollector.class, DefaultDependencyCollector.class );
        addService( Deployer.class, DefaultDeployer.class );
        addService( Installer.class, DefaultInstaller.class );
        addService( MetadataResolver.class, DefaultMetadataResolver.class );
        addService( RemoteRepositoryManager.class, DefaultRemoteRepositoryManager.class );
        addService( UpdateCheckManager.class, DefaultUpdateCheckManager.class );
    }

    /**
     * Sets the implementation class for a service.
     * 
     * @param <T> The service type.
     * @param type The interface describing the service, must not be {@code null}.
     * @param impl The implementation class of the service, must not be {@code null}.
     * @return This locator for chaining, never {@code null}.
     */
    public <T> DefaultServiceLocator setService( Class<T> type, Class<? extends T> impl )
    {
        classes.remove( type );
        return addService( type, impl );
    }

    /**
     * Adds the implementation class for a service.
     * 
     * @param <T> The service type.
     * @param type The interface describing the service, must not be {@code null}.
     * @param impl The implementation class of the service, must not be {@code null}.
     * @return This locator for chaining, never {@code null}.
     */
    public <T> DefaultServiceLocator addService( Class<T> type, Class<? extends T> impl )
    {
        if ( impl == null )
        {
            throw new IllegalArgumentException( "implementation class must not be null" );
        }
        Collection<Class<?>> impls = classes.get( type );
        if ( impls == null )
        {
            impls = new LinkedHashSet<Class<?>>();
            classes.put( type, impls );
        }
        impls.add( impl );
        return this;
    }

    /**
     * Sets the instances for a service.
     * 
     * @param <T> The service type.
     * @param type The interface describing the service, must not be {@code null}.
     * @param services The instances of the service, must not be {@code null}.
     * @return This locator for chaining, never {@code null}.
     */
    public <T> DefaultServiceLocator setServices( Class<T> type, T... services )
    {
        synchronized ( instances )
        {
            instances.put( type, Arrays.asList( services ) );
        }
        return this;
    }

    public <T> T getService( Class<T> type )
    {
        List<T> objs = getServices( type );
        return objs.isEmpty() ? null : objs.get( 0 );
    }

    public <T> List<T> getServices( Class<T> type )
    {
        synchronized ( instances )
        {
            @SuppressWarnings( "unchecked" )
            List<T> objs = (List<T>) instances.get( type );

            if ( objs == null )
            {
                Iterator<T> it;
                Collection<Class<?>> impls = classes.get( type );
                if ( impls == null || impls.isEmpty() )
                {
                    objs = Collections.emptyList();
                    it = objs.iterator();
                }
                else
                {
                    objs = new ArrayList<T>( impls.size() );
                    for ( Class<?> impl : impls )
                    {
                        try
                        {
                            Object obj = impl.newInstance();
                            objs.add( type.cast( obj ) );
                        }
                        catch ( Exception e )
                        {
                            serviceCreationFailed( type, impl, e );
                        }
                    }
                    it = objs.iterator();
                    objs = Collections.unmodifiableList( objs );
                }

                instances.put( type, objs );

                while ( it.hasNext() )
                {
                    T obj = it.next();
                    if ( obj instanceof Service )
                    {
                        try
                        {
                            ( (Service) obj ).initService( this );
                        }
                        catch ( Exception e )
                        {
                            it.remove();
                            serviceCreationFailed( type, obj.getClass(), e );
                        }
                    }
                }
            }

            return objs;
        }
    }

    protected void serviceCreationFailed( Class<?> type, Class<?> impl, Throwable exception )
    {
        exception.printStackTrace();
    }

}
