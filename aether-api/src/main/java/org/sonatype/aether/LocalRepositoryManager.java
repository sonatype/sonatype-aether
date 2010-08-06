package org.sonatype.aether;

import java.util.Collection;

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
     * returned location.
     * 
     * @param artifact The artifact for which to determine the path, must not be {@code null}.
     * @return The path, relative to the local repository's base directory.
     */
    String getPathForLocalArtifact( Artifact artifact );

    /**
     * Gets the relative path for an artifact cached from a remote repository. Note that the artifact need not actually
     * exist yet at the returned location.
     * 
     * @param artifact The artifact for which to determine the path, must not be {@code null}.
     * @param repository The source repository of the artifact, must not be {@code null}.
     * @param context The resolution context in which the artifact is being requested, may be {@code null}.
     * @return The path, relative to the local repository's base directory.
     */
    String getPathForRemoteArtifact( Artifact artifact, RemoteRepository repository, String context );

    /**
     * Gets the relative path for locally built metadata. Note that the metadata need not actually exist yet at the
     * returned location.
     * 
     * @param metadata The metadata for which to determine the path, must not be {@code null}.
     * @return The path, relative to the local repository's base directory.
     */
    String getPathForLocalMetadata( Metadata metadata );

    /**
     * Gets the relative path for metadata cached from a remote repository. Note that the metadata need not actually
     * exist yet at the returned location.
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
     * @param result The artifact request, must not be {@code null}.
     * @return The result of the request, never {@code null}.
     */
    LocalArtifactResult find( LocalArtifactRequest result );

    /**
     * Registers the specified locally built artifact with the local repository. Note that artifact registration is
     * merely concerned about updating the local repository's internal state, not about actually installing the artifact
     * or its accompanying metadata.
     * 
     * @param artifact The artifact to register, must not be {@code null}.
     */
    void addLocalArtifact( Artifact artifact );

    /**
     * Registers the specified remotely cached artifact with the local repository. Note that artifact registration is
     * merely concerned about updating the local repository's internal state, not about actually installing the artifact
     * or its accompanying metadata.
     * 
     * @param artifact The artifact to register, must not be {@code null}.
     * @param repository The remote repository from which the artifact was resolved, must not be {@code null}.
     * @param contexts The resolution contexts in which the artifact is available, may be {@code null}.
     */
    void addRemoteArtifact( Artifact artifact, RemoteRepository repository, Collection<String> contexts );

}
