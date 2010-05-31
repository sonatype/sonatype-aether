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

import java.util.Properties;

/**
 * @author Benjamin Bentmann
 */
public class DefaultRepositorySession
    implements RepositorySession
{

    private boolean offline;

    private boolean transferErrorCachingEnabled;

    private boolean notFoundCachingEnabled;

    private boolean ignoreMissingArtifactDescriptor;

    private boolean ignoreInvalidArtifactDescriptor;

    private String checksumPolicy;

    private String updatePolicy;

    private LocalRepositoryManager localRepositoryManager;

    private WorkspaceReader workspaceReader;

    private TransferListener transferListener;

    private Properties systemProperties = new Properties();

    private Properties userProperties = new Properties();

    private Properties configProperties = new Properties();

    private MirrorSelector mirrorSelector;

    private ProxySelector proxySelector;

    private AuthenticationSelector authenticationSelector;

    private ArtifactStereotypeManager artifactStereotypeManager;

    private DependencyTraverser dependencyTraverser;

    private DependencyManager dependencyManager;

    private DependencyFilter dependencyFilter;

    private DependencyGraphTransformer dependencyGraphTransformer;

    private RepositoryCache cache;

    public DefaultRepositorySession()
    {
        // enables default constructor
    }

    public DefaultRepositorySession( RepositorySession session )
    {
        setOffline( session.isOffline() );
        setTransferErrorCachingEnabled( session.isTransferErrorCachingEnabled() );
        setNotFoundCachingEnabled( session.isNotFoundCachingEnabled() );
        setIgnoreInvalidArtifactDescriptor( session.isIgnoreInvalidArtifactDescriptor() );
        setIgnoreMissingArtifactDescriptor( session.isIgnoreMissingArtifactDescriptor() );
        setChecksumPolicy( session.getChecksumPolicy() );
        setUpdatePolicy( session.getUpdatePolicy() );
        setLocalRepositoryManager( session.getLocalRepositoryManager() );
        setWorkspaceReader( session.getWorkspaceReader() );
        setTransferListener( session.getTransferListener() );
        setSystemProperties( session.getSystemProperties() );
        setUserProperties( session.getUserProperties() );
        setConfigProperties( session.getConfigProperties() );
        setMirrorSelector( session.getMirrorSelector() );
        setProxySelector( session.getProxySelector() );
        setAuthenticationSelector( session.getAuthenticationSelector() );
        setArtifactStereotypeManager( session.getArtifactStereotypeManager() );
        setDependencyTraverser( session.getDependencyTraverser() );
        setDependencyManager( session.getDependencyManager() );
        setDependencyFilter( session.getDependencyFilter() );
        setDependencyGraphTransformer( session.getDependencyGraphTransformer() );
        setCache( session.getCache() );
    }

    public boolean isOffline()
    {
        return offline;
    }

    public DefaultRepositorySession setOffline( boolean offline )
    {
        this.offline = offline;
        return this;
    }

    public boolean isTransferErrorCachingEnabled()
    {
        return transferErrorCachingEnabled;
    }

    public DefaultRepositorySession setTransferErrorCachingEnabled( boolean transferErrorCachingEnabled )
    {
        this.transferErrorCachingEnabled = transferErrorCachingEnabled;
        return this;
    }

    public boolean isNotFoundCachingEnabled()
    {
        return notFoundCachingEnabled;
    }

    public DefaultRepositorySession setNotFoundCachingEnabled( boolean notFoundCachingEnabled )
    {
        this.notFoundCachingEnabled = notFoundCachingEnabled;
        return this;
    }

    public boolean isIgnoreMissingArtifactDescriptor()
    {
        return ignoreMissingArtifactDescriptor;
    }

    public DefaultRepositorySession setIgnoreMissingArtifactDescriptor( boolean ignoreMissingArtifactDescriptor )
    {
        this.ignoreMissingArtifactDescriptor = ignoreMissingArtifactDescriptor;
        return this;
    }

    public boolean isIgnoreInvalidArtifactDescriptor()
    {
        return ignoreInvalidArtifactDescriptor;
    }

    public DefaultRepositorySession setIgnoreInvalidArtifactDescriptor( boolean ignoreInvalidArtifactDescriptor )
    {
        this.ignoreInvalidArtifactDescriptor = ignoreInvalidArtifactDescriptor;
        return this;
    }

    public String getChecksumPolicy()
    {
        return checksumPolicy;
    }

    public DefaultRepositorySession setChecksumPolicy( String checksumPolicy )
    {
        this.checksumPolicy = checksumPolicy;
        return this;
    }

    public String getUpdatePolicy()
    {
        return updatePolicy;
    }

    public DefaultRepositorySession setUpdatePolicy( String updatePolicy )
    {
        this.updatePolicy = updatePolicy;
        return this;
    }

    public LocalRepositoryManager getLocalRepositoryManager()
    {
        return localRepositoryManager;
    }

    public DefaultRepositorySession setLocalRepositoryManager( LocalRepositoryManager localRepositoryManager )
    {
        this.localRepositoryManager = localRepositoryManager;
        return this;
    }

    public WorkspaceReader getWorkspaceReader()
    {
        return workspaceReader;
    }

    public DefaultRepositorySession setWorkspaceReader( WorkspaceReader workspaceReader )
    {
        this.workspaceReader = workspaceReader;
        return this;
    }

    public TransferListener getTransferListener()
    {
        return transferListener;
    }

    public DefaultRepositorySession setTransferListener( TransferListener transferListener )
    {
        this.transferListener = transferListener;
        return this;
    }

    public Properties getSystemProperties()
    {
        return systemProperties;
    }

    public DefaultRepositorySession setSystemProperties( Properties systemProperties )
    {
        this.systemProperties = systemProperties;
        return this;
    }

    public Properties getUserProperties()
    {
        return userProperties;
    }

    public DefaultRepositorySession setUserProperties( Properties userProperties )
    {
        this.userProperties = userProperties;
        return this;
    }

    public Properties getConfigProperties()
    {
        return configProperties;
    }

    public DefaultRepositorySession setConfigProperties( Properties configProperties )
    {
        this.configProperties = configProperties;
        return this;
    }

    public MirrorSelector getMirrorSelector()
    {
        return mirrorSelector;
    }

    public DefaultRepositorySession setMirrorSelector( MirrorSelector mirrorSelector )
    {
        this.mirrorSelector = mirrorSelector;
        return this;
    }

    public ProxySelector getProxySelector()
    {
        return proxySelector;
    }

    public DefaultRepositorySession setProxySelector( ProxySelector proxySelector )
    {
        this.proxySelector = proxySelector;
        return this;
    }

    public AuthenticationSelector getAuthenticationSelector()
    {
        return authenticationSelector;
    }

    public DefaultRepositorySession setAuthenticationSelector( AuthenticationSelector authenticationSelector )
    {
        this.authenticationSelector = authenticationSelector;
        return this;
    }

    public ArtifactStereotypeManager getArtifactStereotypeManager()
    {
        return artifactStereotypeManager;
    }

    public DefaultRepositorySession setArtifactStereotypeManager( ArtifactStereotypeManager artifactStereotypeManager )
    {
        this.artifactStereotypeManager = artifactStereotypeManager;
        return this;
    }

    public DependencyTraverser getDependencyTraverser()
    {
        return dependencyTraverser;
    }

    public DefaultRepositorySession setDependencyTraverser( DependencyTraverser dependencyTraverser )
    {
        this.dependencyTraverser = dependencyTraverser;
        return this;
    }

    public DependencyManager getDependencyManager()
    {
        return dependencyManager;
    }

    public DefaultRepositorySession setDependencyManager( DependencyManager dependencyManager )
    {
        this.dependencyManager = dependencyManager;
        return this;
    }

    public DependencyFilter getDependencyFilter()
    {
        return dependencyFilter;
    }

    public DefaultRepositorySession setDependencyFilter( DependencyFilter dependencyFilter )
    {
        this.dependencyFilter = dependencyFilter;
        return this;
    }

    public DependencyGraphTransformer getDependencyGraphTransformer()
    {
        return dependencyGraphTransformer;
    }

    public DefaultRepositorySession setDependencyGraphTransformer( DependencyGraphTransformer dependencyGraphTransformer )
    {
        this.dependencyGraphTransformer = dependencyGraphTransformer;
        return this;
    }

    public RepositoryCache getCache()
    {
        return cache;
    }

    public DefaultRepositorySession setCache( RepositoryCache cache )
    {
        this.cache = cache;
        return this;
    }

}
