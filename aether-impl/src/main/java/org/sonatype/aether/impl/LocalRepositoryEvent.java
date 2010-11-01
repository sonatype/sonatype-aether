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

import java.io.File;

import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.LocalRepository;

/**
 * An event describing an update to the local repository.
 * 
 * @author Benjamin Bentmann
 * @see LocalRepositoryMaintainer
 */
public interface LocalRepositoryEvent
{

    /**
     * The type of the event.
     */
    enum EventType
    {
        ARTIFACT_INSTALLED, ARTIFACT_DOWNLOADED
    }

    /**
     * Gets the type of the event.
     * 
     * @return The type of the event, never {@code null}.
     */
    EventType getType();

    /**
     * Gets the repository session from which this event originates.
     * 
     * @return The repository session, never {@code null}.
     */
    RepositorySystemSession getSession();

    /**
     * Gets the local repository which has been updated.
     * 
     * @return The local repository, never {@code null}.
     */
    LocalRepository getRepository();

    /**
     * Gets the artifact that was updated. Note that the file associated with this artifact need not point at the
     * artifact's location within the local repository, use {@link #getFile()} to query this path.
     * 
     * @return The artifact, never {@code null}.
     */
    Artifact getArtifact();

    /**
     * Gets the path to the artifact within the local repository.
     * 
     * @return The path to the artifact in the local repository, never {@code null}.
     */
    File getFile();

}
