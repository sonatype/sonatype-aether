package org.sonatype.maven.repository;

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

import java.util.Properties;

/**
 * Defines settings and components that control the repository system.
 * 
 * @author Benjamin Bentmann
 */
public interface RepositorySystemSession
{

    String getId();

    boolean isOffline();

    boolean isTransferErrorCachingEnabled();

    boolean isNotFoundCachingEnabled();

    boolean isIgnoreMissingArtifactDescriptor();

    boolean isIgnoreInvalidArtifactDescriptor();

    String getChecksumPolicy();

    String getUpdatePolicy();

    LocalRepository getLocalRepository();

    LocalRepositoryManager getLocalRepositoryManager();

    WorkspaceReader getWorkspaceReader();

    RepositoryListener getRepositoryListener();

    TransferListener getTransferListener();

    Properties getSystemProperties();

    Properties getUserProperties();

    // timeouts, thread pool size, user-agent, etc.
    Properties getConfigProperties();

    MirrorSelector getMirrorSelector();

    ProxySelector getProxySelector();

    AuthenticationSelector getAuthenticationSelector();

    ArtifactTypeRegistry getArtifactTypeRegistry();

    DependencyTraverser getDependencyTraverser();

    DependencyManager getDependencyManager();

    DependencySelector getDependencySelector();

    DependencyGraphTransformer getDependencyGraphTransformer();

    RepositoryCache getCache();

}
