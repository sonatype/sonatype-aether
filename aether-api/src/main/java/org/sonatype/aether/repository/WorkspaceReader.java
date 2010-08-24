package org.sonatype.aether.repository;

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

/**
 * Manages a repository backed by the IDE workspace or a build session.
 * 
 * @author Benjamin Bentmann
 */
public interface WorkspaceReader
{

    /**
     * Gets a description of the workspace repository.
     * 
     * @return The repository description, never {@code null}.
     */
    WorkspaceRepository getRepository();

    /**
     * Locates the specified artifact.
     * 
     * @param artifact The artifact to locate, must not be {@code null}.
     * @return The path to the artifact or {@code null} if the artifact is not available.
     */
    File findArtifact( Artifact artifact );

    /**
     * Determines all available versions of the specified artifact.
     * 
     * @param artifact The artifact whose versions should be listed, must not be {@code null}.
     * @return The available versions of the artifact, must not be {@code null}.
     */
    List<String> findVersions( Artifact artifact );

}
