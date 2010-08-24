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

import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.metadata.Metadata;

/**
 * Manages access to the local repository.
 * 
 * @author Benjamin Bentmann
 */
public interface LocalRepositoryManager
{

    /**
     * Gets the description of the local repository being managed.
     * 
     * @return The description of the local repository, never {@code null}.
     */
    LocalRepository getRepository();

    /**
     * Gets the relative path for a locally built artifact. Note that the artifact need not actually exist yet at the
     * returned location. The path uses the forward slash as directory separator regardless of the underlying file
     * system.
     * 
     * @param artifact The artifact for which to determine the path, must not be {@code null}.
     * @return The path, relative to the local repository's base directory.
     */
    String getPathForLocalArtifact( Artifact artifact );

    /**
     * Gets the relative path for an artifact cached from a remote repository. Note that the artifact need not actually
     * exist yet at the returned location. The path uses the forward slash as directory separator regardless of the
     * underlying file system.
     * 
     * @param artifact The artifact for which to determine the path, must not be {@code null}.
     * @param repository The source repository of the artifact, must not be {@code null}.
     * @param context The resolution context in which the artifact is being requested, may be {@code null}.
     * @return The path, relative to the local repository's base directory.
     */
    String getPathForRemoteArtifact( Artifact artifact, RemoteRepository repository, String context );

    /**
     * Gets the relative path for locally built metadata. Note that the metadata need not actually exist yet at the
     * returned location. The path uses the forward slash as directory separator regardless of the underlying file
     * system.
     * 
     * @param metadata The metadata for which to determine the path, must not be {@code null}.
     * @return The path, relative to the local repository's base directory.
     */
    String getPathForLocalMetadata( Metadata metadata );

    /**
     * Gets the relative path for metadata cached from a remote repository. Note that the metadata need not actually
     * exist yet at the returned location. The path uses the forward slash as directory separator regardless of the
     * underlying file system.
     * 
     * @param metadata The metadata for which to determine the path, must not be {@code null}.
     * @param repository The source repository of the metadata, must not be {@code null}.
     * @param context The resolution context in which the metadata is being requested, may be {@code null}.
     * @return The path, relative to the local repository's base directory.
     */
    String getPathForRemoteMetadata( Metadata metadata, RemoteRepository repository, String context );

    /**
     * Queries for the existence of an artifact in the local repository. The request could be satisfied by a locally
     * built artifact or a previously downloaded artifact.
     * 
     * @param session The repository system session during which the request is made, must not be {@code null}.
     * @param request The artifact request, must not be {@code null}.
     * @return The result of the request, never {@code null}.
     */
    LocalArtifactResult find( RepositorySystemSession session, LocalArtifactRequest request );

    /**
     * Registers an installed or resolved artifact with the local repository. Note that artifact registration is merely
     * concerned about updating the local repository's internal state, not about actually installing the artifact or its
     * accompanying metadata.
     * 
     * @param session The repository system session during which the registration is made, must not be {@code null}.
     * @param request The registration request, must not be {@code null}.
     */
    void add( RepositorySystemSession session, LocalArtifactRegistration request );

}
