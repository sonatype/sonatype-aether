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
    VersionRangeResult resolveVersionRange( RepositorySystemSession session, VersionRangeRequest request )
        throws VersionRangeResolutionException;

    /**
     * Resolves an artifact's meta version (if any) to a concrete version. For example, resolves "1.0-SNAPSHOT" to
     * "1.0-20090208.132618-23" or "RELEASE"/"LATEST" to "2.0".
     * 
     * @param session The repository session, must not be {@code null}.
     * @param request The version request, must not be {@code null}
     * @return The version result, never {@code null}.
     * @throws VersionResolutionException If the metaversion could not be resolved.
     */
    VersionResult resolveVersion( RepositorySystemSession session, VersionRequest request )
        throws VersionResolutionException;

    /**
     * Gets information about an artifact like its direct dependencies.
     * 
     * @param session The repository session, must not be {@code null}.
     * @param request The descriptor request, must not be {@code null}
     * @return The descriptor result, never {@code null}.
     * @throws ArtifactDescriptorException If the artifact descritpor could not be read.
     * @see RepositorySystemSession#isIgnoreInvalidArtifactDescriptor()
     * @see RepositorySystemSession#isIgnoreMissingArtifactDescriptor()
     */
    ArtifactDescriptorResult readArtifactDescriptor( RepositorySystemSession session, ArtifactDescriptorRequest request )
        throws ArtifactDescriptorException;

    /**
     * Collects the transitive dependencies of an artifact and builds a dependency graph.
     * 
     * @param session The repository session, must not be {@code null}.
     * @param request The collection request, must not be {@code null}
     * @return The collection result, never {@code null}.
     * @throws DependencyCollectionException If the dependency tree could not be built.
     * @see RepositorySystemSession#getDependencyTraverser()
     * @see RepositorySystemSession#getDependencyManager()
     * @see RepositorySystemSession#getDependencySelector()
     * @see RepositorySystemSession#getDependencyGraphTransformer()
     */
    CollectResult collectDependencies( RepositorySystemSession session, CollectRequest request )
        throws DependencyCollectionException;

    /**
     * Resolves the paths for the artifacts referenced by the specified dependency graph. Artifacts will be downloaded
     * if necessary. Artifacts that are already resolved will be skipped and are not re-resolved.
     * 
     * @param session The repository session, must not be {@code null}.
     * @param node The root node of the dependency graph whose artifacts shall be resolved, must not be {@code null}
     * @param filter The node filter to use to select the actual nodes to resolve, may be {@code null} to resolve all
     *            nodes.
     * @return The resolution results, never {@code null}.
     * @throws ArtifactResolutionException If any artifact could not be resolved.
     * @see Artifact#getFile()
     */
    List<ArtifactResult> resolveDependencies( RepositorySystemSession session, DependencyNode node,
                                              DependencyFilter filter )
        throws ArtifactResolutionException;

    /**
     * Collects the transitive dependencies of an artifact and resolves the paths for the artifacts referenced by the
     * specified dependency graph. This is a convenience method that combines
     * {@link #collectDependencies(RepositorySystemSession, CollectRequest)} and
     * {@link #resolveDependencies(RepositorySystemSession, DependencyNode, DependencyFilter)}.
     * 
     * @param session The repository session, must not be {@code null}.
     * @param request The collection request, must not be {@code null}
     * @param filter The node filter to use to select the actual nodes to resolve, may be {@code null} to resolve all
     *            nodes.
     * @return The resolution results, never {@code null}.
     * @throws DependencyCollectionException If the dependency tree could not be built.
     * @throws ArtifactResolutionException If any artifact could not be resolved.
     */
    List<ArtifactResult> resolveDependencies( RepositorySystemSession session, CollectRequest request,
                                              DependencyFilter filter )
        throws DependencyCollectionException, ArtifactResolutionException;

    /**
     * Resolves the paths for an artifact. The Artifact will be downloaded if necessary. An artifacts that is already
     * resolved will be skipped and is not re-resolved.
     * 
     * @param session The repository session, must not be {@code null}.
     * @param request The resolution request, must not be {@code null}
     * @return The resolution result, never {@code null}.
     * @throws ArtifactResolutionException If any artifact could not be resolved.
     * @see Artifact#getFile()
     */
    ArtifactResult resolveArtifact( RepositorySystemSession session, ArtifactRequest request )
        throws ArtifactResolutionException;

    /**
     * Resolves the paths for a collection of artifacts. Artifacts will be downloaded if necessary. Artifacts that are
     * already resolved will be skipped and are not re-resolved.
     * 
     * @param session The repository session, must not be {@code null}.
     * @param requests The resolution requests, must not be {@code null}
     * @return The resolution results (in request order), never {@code null}.
     * @throws ArtifactResolutionException If any artifact could not be resolved.
     * @see Artifact#getFile()
     */
    List<ArtifactResult> resolveArtifacts( RepositorySystemSession session,
                                           Collection<? extends ArtifactRequest> requests )
        throws ArtifactResolutionException;

    /**
     * Resolves the paths for a collection of metadata. Metadata will be downloaded if necessary.
     * 
     * @param session The repository session, must not be {@code null}.
     * @param requests The resolution requests, must not be {@code null}
     * @return The resolution results (in request order), never {@code null}.
     * @see Metadata#getFile()
     */
    List<MetadataResult> resolveMetadata( RepositorySystemSession session,
                                          Collection<? extends MetadataRequest> requests );

    /**
     * Installs a collection of artifacts and their accompanying metadata to the local repository.
     * 
     * @param session The repository session, must not be {@code null}.
     * @param request The installation request, must not be {@code null}.
     * @return The installation result, never {@code null}.
     * @throws InstallationException If any artifact/metadata from the request could not be installed.
     */
    InstallResult install( RepositorySystemSession session, InstallRequest request )
        throws InstallationException;

    /**
     * Uploads a collection of artifacts and their accompanying metadata to a remote repository.
     * 
     * @param session The repository session, must not be {@code null}.
     * @param request The deployment request, must not be {@code null}.
     * @return The deployment result, never {@code null}.
     * @throws DeploymentException If any artifact/metadata from the request could not be deployed.
     */
    DeployResult deploy( RepositorySystemSession session, DeployRequest request )
        throws DeploymentException;

    /**
     * Creates a new manager for the specified local repository. If the specified local repository has no type, the
     * default repository type will be used.
     * 
     * @param localRepository The local repository to create a manager for, must not be {@code null}.
     * @return The local repository manager, never {@code null}.
     * @throws IllegalArgumentException If the specified repository type is not recognized or no base directory is
     *             given.
     */
    LocalRepositoryManager newLocalRepositoryManager( LocalRepository localRepository );

}
