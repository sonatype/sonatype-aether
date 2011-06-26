package org.sonatype.aether.impl;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import java.io.File;

import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.LocalRepository;

/**
 * An event describing an update to the local repository.
 * 
 * @author Benjamin Bentmann
 * @see LocalRepositoryMaintainer
 * @deprecated As of version 1.9, use a global {@link org.sonatype.aether.RepositoryListener} instead.
 */
@Deprecated
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
