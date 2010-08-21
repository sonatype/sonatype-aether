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

import java.util.Map;

/**
 * Defines settings and components that control the repository system.
 * 
 * @author Benjamin Bentmann
 */
public interface RepositorySystemSession
{

    /**
     * Gets the user agent that repository connectors should report to servers.
     * 
     * @return The user agent to use or {@code null}/empty if undefined.
     */
    String getUserAgent();

    /**
     * Gets the timeout (in milliseconds) to wait for a successful connection to a remote server.
     * 
     * @return The connection timeout in milliseconds, non-positive values indicate no timeout.
     */
    int getConnectTimeout();

    /**
     * Gets the timeout (in milliseconds) to wait for a response from a remote server.
     * 
     * @return The request timeout in milliseconds, non-positive values indicate no timeout.
     */
    int getRequestTimeout();

    /**
     * Indicates whether the repository system operates in offline mode and avoids/refuses any access to remote
     * repositories.
     * 
     * @return {@code true} if the repository system is in offline mode, {@code false} otherwise.
     */
    boolean isOffline();

    /**
     * Indicates whether transfer errors (e.g. unreachable host, bad authentication) from resolution attempts should be
     * cached in the local repository. If caching is enabled, resolution will not be reattempted until the update policy
     * for the affected resource has expired.
     * 
     * @return {@code true} if transfer errors are cached, {@code false} to always reattempt downloading.
     */
    boolean isTransferErrorCachingEnabled();

    /**
     * Indicates whether missing artifacts/metadata from resolution attempts should be cached in the local repository.
     * If caching is enabled, resolution will not be reattempted until the update policy for the affected resource has
     * expired.
     * 
     * @return {@code true} if missing resources are cached, {@code false} to always reattempt downloading.
     */
    boolean isNotFoundCachingEnabled();

    /**
     * Indicates whether missing artifact descriptors are silently ignored. If enabled and no artifact descriptor is
     * available, an empty stub descriptor is used instead.
     * 
     * @return {@code true} if missing artifact descriptors are ignored, {@code false} to fail the operation with an
     *         exception.
     */
    boolean isIgnoreMissingArtifactDescriptor();

    /**
     * Indicates whether invalid artifact descriptors are silently ignored. If enabled and an artifact descriptor is
     * invalid, an empty stub descriptor is used instead.
     * 
     * @return {@code true} if invalid artifact descriptors are ignored, {@code false} to fail the operation with an
     *         exception.
     */
    boolean isIgnoreInvalidArtifactDescriptor();

    /**
     * Gets the global checksum policy. If set, the global checksum policy overrides the checksum policies of the remote
     * repositories being used for resolution.
     * 
     * @return The global checksum policy or {@code null}/empty if not set and the per-repository policies apply.
     * @see RepositoryPolicy
     */
    String getChecksumPolicy();

    /**
     * Gets the global update policy. If set, the global update policy overrides the update policies of the remote
     * repositories being used for resolution.
     * 
     * @return The global update policy or {@code null}/empty if not set and the per-repository policies apply.
     * @see RepositoryPolicy
     */
    String getUpdatePolicy();

    /**
     * Gets the local repository used during this session. This is a convenience method for
     * {@link LocalRepositoryManager#getRepository()}.
     * 
     * @return The local repository being during this session, never {@code null}.
     */
    LocalRepository getLocalRepository();

    /**
     * Gets the local repository manager used during this session.
     * 
     * @return The local repository manager used during this session, never {@code null}.
     */
    LocalRepositoryManager getLocalRepositoryManager();

    /**
     * Gets the workspace reader used during this session. If set, the workspace reader will usually be consulted first
     * to resolve artifacts.
     * 
     * @return The workspace reader for this session or {@code null} if none.
     */
    WorkspaceReader getWorkspaceReader();

    /**
     * Gets the listener being notified of actions in the repository system.
     * 
     * @return The repository listener or {@code null} if none.
     */
    RepositoryListener getRepositoryListener();

    /**
     * Gets the listener being notified of uploads/downloads by the repository system.
     * 
     * @return The transfer listener or {@code null} if none.
     */
    TransferListener getTransferListener();

    /**
     * Gets the system properties to use, e.g. for processing of artifact descriptors. System properties are usually
     * collected from the runtime environment like {@link System#getProperties()} and environment variables.
     * 
     * @return The (read-only) system properties, never {@code null}.
     */
    Map<String, String> getSystemProperties();

    /**
     * Gets the user properties to use, e.g. for processing of artifact descriptors. User properties are similar to
     * system properties but are set on the discretion of the user and hence are considered of higher priority than
     * system properties.
     * 
     * @return The (read-only) user properties, never {@code null}.
     */
    Map<String, String> getUserProperties();

    /**
     * The configuration properties used to tweak internal aspects of the repository system (e.g. thread pooling,
     * connector-specific behavior, etc.)
     * 
     * @return The (read-only) configuration properties, never {@code null}.
     */
    Map<String, Object> getConfigProperties();

    /**
     * Gets the mirror selector to use for repositories discovered in artifact descriptors.
     * 
     * @return The mirror selector to use, never {@code null}.
     */
    MirrorSelector getMirrorSelector();

    /**
     * Gets the proxy selector to use for repositories discovered in artifact descriptors.
     * 
     * @return The proxy selector to use, never {@code null}.
     */
    ProxySelector getProxySelector();

    /**
     * Gets the authentication selector to use for repositories discovered in artifact descriptors.
     * 
     * @return The authentication selector to use, never {@code null}.
     */
    AuthenticationSelector getAuthenticationSelector();

    /**
     * Gets the registry of artifact types recognized by this session.
     * 
     * @return The artifact type registry, never {@code null}.
     */
    ArtifactTypeRegistry getArtifactTypeRegistry();

    /**
     * Gets the dependency traverser to use for building dependency graphs.
     * 
     * @return The dependency traverser to use for building dependency graphs, never {@code null}.
     */
    DependencyTraverser getDependencyTraverser();

    /**
     * Gets the dependency manager to use for building dependency graphs.
     * 
     * @return The dependency manager to use for building dependency graphs, never {@code null}.
     */
    DependencyManager getDependencyManager();

    /**
     * Gets the dependency selector to use for building dependency graphs.
     * 
     * @return The dependency selector to use for building dependency graphs, never {@code null}.
     */
    DependencySelector getDependencySelector();

    /**
     * Gets the dependency graph transformer to use for building dependency graphs.
     * 
     * @return The dependency graph transformer to use for building dependency graphs, never {@code null}.
     */
    DependencyGraphTransformer getDependencyGraphTransformer();

    /**
     * Gets the cache the repository system may use to save data for future reuse during the session.
     * 
     * @return The repository cache or {@code null} if none.
     */
    RepositoryCache getCache();

}
