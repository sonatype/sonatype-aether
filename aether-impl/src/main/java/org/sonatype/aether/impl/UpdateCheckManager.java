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

import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.metadata.Metadata;
import org.sonatype.aether.transfer.ArtifactTransferException;
import org.sonatype.aether.transfer.MetadataTransferException;

/**
 * Determines if updates of artifact and metadata from remote repositories are needed.
 * 
 * @author Benjamin Bentmann
 */
public interface UpdateCheckManager
{

    /**
     * Returns the policy with the shorter update interval.
     * 
     * @param session The repository system session during which the request is made, must not be {@code null}.
     * @param policy1 A policy to compare.
     * @param policy2 A policy to compare.
     * @return The policy with the shorter update interval.
     */
    String getEffectiveUpdatePolicy( RepositorySystemSession session, String policy1, String policy2 );

    /**
     * Checks whether an artifact has to be updated from a remote repository.
     * 
     * @param session The repository system session during which the request is made, must not be {@code null}.
     * @param check The update check request, must not be {@code null}.
     */
    void checkArtifact( RepositorySystemSession session, UpdateCheck<Artifact, ArtifactTransferException> check );

    /**
     * Updates the timestamp for the artifact contained in the update check.
     * 
     * @param session The repository system session during which the request is made, must not be {@code null}.
     * @param check The update check request, must not be {@code null}.
     */
    void touchArtifact( RepositorySystemSession session, UpdateCheck<Artifact, ArtifactTransferException> check );

    /**
     * Checks whether metadata has to be updated from a remote repository.
     * 
     * @param session The repository system session during which the request is made, must not be {@code null}.
     * @param check The update check request, must not be {@code null}.
     */
    void checkMetadata( RepositorySystemSession session, UpdateCheck<Metadata, MetadataTransferException> check );

    /**
     * Updates the timestamp for the metadata contained in the update check.
     * 
     * @param session The repository system session during which the request is made, must not be {@code null}.
     * @param check The update check request, must not be {@code null}.
     */
    void touchMetadata( RepositorySystemSession session, UpdateCheck<Metadata, MetadataTransferException> check );

}
