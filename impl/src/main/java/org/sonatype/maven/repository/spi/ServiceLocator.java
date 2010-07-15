package org.sonatype.maven.repository.spi;

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

import java.util.List;

/**
 * A simple infrastructure to programmatically wire the various components of the repository system together when it is
 * used outside of an IoC container. Once a concrete implementation of a service locator has been setup, clients could
 * use
 * 
 * <pre>
 * RepositorySystem repoSystem = serviceLocator.getService( RepositorySystem.class );
 * </pre>
 * 
 * to acquire the repository system. Components that implement {@link Service} will be given an opportunity to acquire
 * further components from the locator, thereby allowing to create the complete object graph of the repository system.
 * 
 * @author Benjamin Bentmann
 */
public interface ServiceLocator
{

    /**
     * Gets an instance of the specified service.
     * 
     * @param <T> The service type.
     * @param type The interface describing the service, must not be {@code null}.
     * @return The service instance or {@code null} if the service could not be located/initialized.
     */
    <T> T getService( Class<T> type );

    /**
     * Gets all available instances of the specified service.
     * 
     * @param <T> The service type.
     * @param type The interface describing the service, must not be {@code null}.
     * @return The available service instances, never {@code null}.
     */
    <T> List<T> getServices( Class<T> type );

}
