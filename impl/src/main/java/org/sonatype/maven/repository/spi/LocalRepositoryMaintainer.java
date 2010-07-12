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

/**
 * Performs housekeeping tasks in response to updates to the local repository. This provides an extension point to
 * integrators to performs things like updating indexes.
 * 
 * @author Benjamin Bentmann
 */
public interface LocalRepositoryMaintainer
{

    /**
     * Notifies the maintainer of the addition of an artifact to the local repository by a local build.
     * 
     * @param event The event that holds details about the artifact, must not be {@code null}.
     */
    void artifactInstalled( LocalRepositoryEvent event );

    /**
     * Notifies the maintainer of the addition of an artifact to the local repository by download from a remote
     * repository.
     * 
     * @param event The event that holds details about the artifact, must not be {@code null}.
     */
    void artifactDownloaded( LocalRepositoryEvent event );

}
