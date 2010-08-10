package org.sonatype.aether.impl;

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

import org.sonatype.aether.DeployRequest;
import org.sonatype.aether.InstallRequest;
import org.sonatype.aether.RepositorySystemSession;

/**
 * A factory to create metadata generators. Metadata generators can contribute additional metadata during the
 * installation/deployment of artifacts.
 * 
 * @author Benjamin Bentmann
 */
public interface MetadataGeneratorFactory
{

    /**
     * Creates a new metadata generator for the specified install request.
     * 
     * @param session The repository system session from which to configure the generator, must not be {@code null}.
     * @param request The install request the metadata generator is used for, must not be {@code null}.
     * @return The metadata generator for the request or {@code null} if none.
     */
    MetadataGenerator newInstance( RepositorySystemSession session, InstallRequest request );

    /**
     * Creates a new metadata generator for the specified deploy request.
     * 
     * @param session The repository system session from which to configure the generator, must not be {@code null}.
     * @param request The deploy request the metadata generator is used for, must not be {@code null}.
     * @return The metadata generator for the request or {@code null} if none.
     */
    MetadataGenerator newInstance( RepositorySystemSession session, DeployRequest request );

    /**
     * The priority of this factory. Factories with higher priority are invoked before those with lower priority.
     * 
     * @return The priority of this factory.
     */
    int getPriority();

}
