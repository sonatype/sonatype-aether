package org.sonatype.aether;

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
import java.util.List;

import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.metadata.Metadata;
import org.sonatype.aether.repository.ArtifactRepository;

/**
 * An event describing an action performed by the repository system.
 * 
 * @author Benjamin Bentmann
 * @see RepositoryListener
 */
public interface RepositoryEvent
{

    /**
     * The type of the event.
     */
    enum EventType
    {
        ARTIFACT_DESCRIPTOR_INVALID,
        ARTIFACT_DESCRIPTOR_MISSING,
        METADATA_INVALID,
        ARTIFACT_RESOLVING,
        ARTIFACT_RESOLVED,
        METADATA_RESOLVING,
        METADATA_RESOLVED,
        ARTIFACT_INSTALLING,
        ARTIFACT_INSTALLED,
        METADATA_INSTALLING,
        METADATA_INSTALLED,
        ARTIFACT_DEPLOYING,
        ARTIFACT_DEPLOYED,
        METADATA_DEPLOYING,
        METADATA_DEPLOYED
    }

    /**
     * Gets the type of the event.
     * 
     * @return The type of the event, never {@code null}.
     */
    EventType getType();

    /**
     * Gets the repository system session during which the event occurred.
     * 
     * @return The repository system session during which the event occurred, never {@code null}.
     */
    RepositorySystemSession getSession();

    /**
     * Gets the artifact involved in the event (if any).
     * 
     * @return The involved artifact or {@code null} if none.
     */
    Artifact getArtifact();

    /**
     * Gets the metadata involved in the event (if any).
     * 
     * @return The involved metadata or {@code null} if none.
     */
    Metadata getMetadata();

    /**
     * Gets the file involved in the event (if any).
     * 
     * @return The involved file or {@code null} if none.
     */
    File getFile();

    /**
     * Gets the repository involved in the event (if any).
     * 
     * @return The involved repository or {@code null} if none.
     */
    ArtifactRepository getRepository();

    /**
     * Gets the exception that caused the event (if any).
     * 
     * @return The exception or {@code null} if none.
     */
    Exception getException();

    /**
     * Gets the exceptions that caused the event (if any).
     * 
     * @return The exceptions, never {@code null}.
     */
    List<Exception> getExceptions();

}
