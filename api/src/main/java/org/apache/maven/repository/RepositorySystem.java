package org.apache.maven.repository;

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

import java.util.Collection;
import java.util.List;

/**
 * @author Benjamin Bentmann
 */
public interface RepositorySystem
{

    /**
     * Expands a version range to a list of matching versions, in ascending order. For example, resolves "[3.8,4.0)" to
     * ["3.8", "3.8.1", "3.8.2"].
     */
    VersionRangeResult resolveVersionRange( RepositorySession session, VersionRangeRequest request )
        throws VersionRangeResolutionException;

    /**
     * Resolves a metaversion to a concrete version. For example, resolves "1.0-SNAPSHOT" to "1.0-20090208.132618-23" or
     * "RELEASE"/"LATEST" to "2.0".
     */
    VersionResult resolveVersion( RepositorySession session, VersionRequest request )
        throws VersionResolutionException;

    /**
     * Gets information about an artifact like its direct dependencies.
     */
    ArtifactDescriptorResult readArtifactDescriptor( RepositorySession session, ArtifactDescriptorRequest request )
        throws ArtifactDescriptorException;

    /**
     * Collects the transitive dependencies of an artifact.
     */
    CollectResult collectDependencies( RepositorySession session, CollectRequest request )
        throws DependencyCollectionException;

    /**
     * Resolves the paths for a collection of artifacts. Artifacts will be downloaded if necessary.
     */
    List<ArtifactResult> resolveArtifacts( RepositorySession session, Collection<? extends ArtifactRequest> requests )
        throws ArtifactResolutionException;

    /**
     * Resolves the paths for a collection of metadata. Metadata will be downloaded if necessary.
     */
    List<MetadataResult> resolveMetadata( RepositorySession session, Collection<? extends MetadataRequest> requests );

    /**
     * Installs a collection of artifacts and their accompanying metadata to the local repository.
     */
    void install( RepositorySession session, InstallRequest request )
        throws InstallationException;

    /**
     * Uploads a collection of artifacts and their accompanying metadata to a remote repository. This process
     * automatically includes the installation of the artifacts to the local repository.
     */
    void deploy( RepositorySession session, DeployRequest request );

}
