package org.sonatype.aether.spi.locator;

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
 * A stateless component of the repository system. The primary purpose of this interface is to provide a convenient
 * means to programmatically wire the several components of the repository system together when it is used outside of an
 * IoC container.
 * 
 * @author Benjamin Bentmann
 */
public interface Service
{

    /**
     * Provides the opportunity to initialize this service and to acquire other services for its operation from the
     * locator. A service must not save the reference to the provided service locator.
     * 
     * @param locator The service locator, must not be {@code null}.
     */
    void initService( ServiceLocator locator );

}
