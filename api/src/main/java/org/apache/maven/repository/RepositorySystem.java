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
 * The main entry point to the repository system.
 * 
 * @author Benjamin Bentmann
 */
public interface RepositorySystem
{

    /**
     * Expands a version range to a list of matching versions, in ascending order. For example, resolves "[3.8,4.0)" to
     * ["3.8", "3.8.1", "3.8.2"].
     * 
     * @param session The repository session, must not be {@code null}.
     * @param request The version range request, must not be {@code null}
     * @return The version range result, never {@code null}.
     * @throws VersionRangeResolutionException If the requested range could not be parsed. Note that an empty range does
     *             not raise an exception.
     */
    VersionRangeResult resolveVersionRange( RepositorySession session, VersionRangeRequest request )
        throws VersionRangeResolutionException;

    /**
     * Resolves a metaversion to a concrete version. For example, resolves "1.0-SNAPSHOT" to "1.0-20090208.132618-23" or
     * "RELEASE"/"LATEST" to "2.0".
     * 
     * @param session The repository session, must not be {@code null}.
     * @param request The version request, must not be {@code null}
     * @return The version result, never {@code null}.
     * @throws VersionResolutionException If the metaversion could not be resolved.
     */
    VersionResult resolveVersion( RepositorySession session, VersionRequest request )
        throws VersionResolutionException;

    /**
     * Gets information about an artifact like its direct dependencies.
     * 
     * @param session The repository session, must not be {@code null}.
     * @param request The descriptor request, must not be {@code null}
     * @return The descriptor result, never {@code null}.
     * @throws ArtifactDescriptorException If the artifact descritpor could not be read.
     * @see RepositorySession#isIgnoreInvalidArtifactDescriptor()
     * @see RepositorySession#isIgnoreMissingArtifactDescriptor()
     */
    ArtifactDescriptorResult readArtifactDescriptor( RepositorySession session, ArtifactDescriptorRequest request )
        throws ArtifactDescriptorException;

    /**
     * Collects the transitive dependencies of an artifact and builds a dependency graph.
     * 
     * @param session The repository session, must not be {@code null}.
     * @param request The collection request, must not be {@code null}
     * @return The collection result, never {@code null}.
     * @throws DependencyCollectionException If the dependency tree could not be built.
     * @see RepositorySession#getDependencyTraverser()
     * @see RepositorySession#getDependencyManager()
     * @see RepositorySession#getDependencyFilter()
     * @see RepositorySession#getDependencyGraphTransformer()
     */
    CollectResult collectDependencies( RepositorySession session, CollectRequest request )
        throws DependencyCollectionException;

    /**
     * Resolves the paths for any unresolved artifacts referenced by the specified dependency graph. Artifacts will be
     * downloaded if necessary.
     * 
     * @param session The repository session, must not be {@code null}.
     * @param node The root node of the dependency graph whose artifacts shall be resolved, must not be {@code null}
     * @throws ArtifactResolutionException If any artifact could not be resolved.
     * @see Artifact#getFile()
     */
    public void resolveDependencies( RepositorySession session, DependencyNode node )
        throws ArtifactResolutionException;

    /**
     * Resolves the paths for a collection of artifacts. Artifacts will be downloaded if necessary.
     * 
     * @param session The repository session, must not be {@code null}.
     * @param requests The resolution requests, must not be {@code null}
     * @throws ArtifactResolutionException If any artifact could not be resolved.
     * @see Artifact#getFile()
     */
    List<ArtifactResult> resolveArtifacts( RepositorySession session, Collection<? extends ArtifactRequest> requests )
        throws ArtifactResolutionException;

    /**
     * Resolves the paths for a collection of metadata. Metadata will be downloaded if necessary.
     * 
     * @param session The repository session, must not be {@code null}.
     * @param requests The resolution requests, must not be {@code null}
     * @see Metadata#getFile()
     */
    List<MetadataResult> resolveMetadata( RepositorySession session, Collection<? extends MetadataRequest> requests );

    /**
     * Installs a collection of artifacts and their accompanying metadata to the local repository.
     * 
     * @param session The repository session, must not be {@code null}.
     * @param request The installation request, must not be {@code null}.
     * @throws InstallationException If any artifact/metadata from the request could not be installed.
     */
    void install( RepositorySession session, InstallRequest request )
        throws InstallationException;

    /**
     * Uploads a collection of artifacts and their accompanying metadata to a remote repository.
     * 
     * @param session The repository session, must not be {@code null}.
     * @param request The deployment request, must not be {@code null}.
     * @throws DeploymentException If any artifact/metadata from the request could not be deployed.
     */
    void deploy( RepositorySession session, DeployRequest request )
        throws DeploymentException;

}
