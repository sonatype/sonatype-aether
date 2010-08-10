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

import java.util.Collection;

import org.sonatype.aether.Artifact;
import org.sonatype.aether.Metadata;

/**
 * A metadata generator that participates in the installation/deployment of artifacts.
 * 
 * @author Benjamin Bentmann
 */
public interface MetadataGenerator
{

    /**
     * Prepares the generator to transform artifacts.
     * 
     * @param artifacts The artifacts to install/deploy, must not be {@code null}.
     * @return The metadata to process (e.g. merge with existing metadata) before artifact transformations, never
     *         {@code null}.
     */
    Collection<? extends Metadata> prepare( Collection<? extends Artifact> artifacts );

    /**
     * Enables the metadata generator to transform the specified artifact.
     * 
     * @param artifact The artifact to transform, must not be {@code null}.
     * @return The transformed artifact (or just the input artifact), never {@code null}.
     */
    Artifact transformArtifact( Artifact artifact );

    /**
     * Allows for metadata generation based on the transformed artifacts.
     * 
     * @param artifacts The (transformed) artifacts to install/deploy, must not be {@code null}.
     * @return The additional metadata to process after artifact transformations, never {@code null}.
     */
    Collection<? extends Metadata> finish( Collection<? extends Artifact> artifacts );

}
