package org.apache.maven.repo;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
     * @param The source repository of the artifact, must not be {@code null}.
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
     * @param The source repository of the metadata, must not be {@code null}.
     * @return The path, relative to the local repository's base directory.
     */
    String getPathForRemoteMetadata( Metadata metadata, RemoteRepository repository, String context );

    /**
     * Queries for the existence of an artifact in the local repository. The query could be satisfied by a locally built
     * artifact or a previously downloaded artifact. The provided query object will be updated with its results.
     * 
     * @param query The artifact query, must not be {@code null}.
     */
    void find( LocalArtifactQuery query );

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
     * @param context The resolution context in which the artifact was resolved, may be {@code null}.
     */
    void addRemoteArtifact( Artifact artifact, RemoteRepository repository, String context );

}
