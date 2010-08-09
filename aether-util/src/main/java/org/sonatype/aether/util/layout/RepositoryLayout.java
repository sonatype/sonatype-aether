package org.sonatype.aether.util.layout;

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

import java.net.URI;

import org.sonatype.aether.Artifact;
import org.sonatype.aether.Metadata;

/**
 * The layout for a remote repository whose artifacts/metadata can be addressed via URIs.
 * 
 * @author Benjamin Bentmann
 */
public interface RepositoryLayout
{

    /**
     * Gets the URI to the location within a remote repository where the specified artifact would be stored. The URI is
     * relative to the root directory of the repository.
     * 
     * @param artifact The artifact to get the URI for, must not be {@code null}.
     * @return The relative URI to the artifact, never {@code null}.
     */
    URI getPath( Artifact artifact );

    /**
     * Gets the URI to the location within a remote repository where the specified metadata would be stored. The URI is
     * relative to the root directory of the repository.
     * 
     * @param metadata The metadata to get the URI for, must not be {@code null}.
     * @return The relative URI to the metadata, never {@code null}.
     */
    URI getPath( Metadata metadata );

}
