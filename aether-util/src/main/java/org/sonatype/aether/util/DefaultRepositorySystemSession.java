package org.sonatype.aether.util;

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

import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;

import org.sonatype.aether.ArtifactTypeRegistry;
import org.sonatype.aether.AuthenticationSelector;
import org.sonatype.aether.DependencyGraphTransformer;
import org.sonatype.aether.DependencyManager;
import org.sonatype.aether.DependencySelector;
import org.sonatype.aether.DependencyTraverser;
import org.sonatype.aether.LocalRepository;
import org.sonatype.aether.LocalRepositoryManager;
import org.sonatype.aether.MirrorSelector;
import org.sonatype.aether.ProxySelector;
import org.sonatype.aether.RepositoryCache;
import org.sonatype.aether.RepositoryListener;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.SessionData;
import org.sonatype.aether.TransferListener;
import org.sonatype.aether.WorkspaceReader;

/**
 * A simple repository system session.
 * 
 * @author Benjamin Bentmann
 */
public class DefaultRepositorySystemSession
    implements RepositorySystemSession
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

    private RepositoryListener repositoryListener;

    private TransferListener transferListener;

    private Map<String, String> systemProperties = new HashMap<String, String>();

    private Map<String, String> userProperties = new HashMap<String, String>();

    private Map<String, Object> configProperties = new HashMap<String, Object>();

    private MirrorSelector mirrorSelector;

    private ProxySelector proxySelector;

    private AuthenticationSelector authenticationSelector;

    private ArtifactTypeRegistry artifactTypeRegistry;

    private DependencyTraverser dependencyTraverser;

    private DependencyManager dependencyManager;

    private DependencySelector dependencySelector;

    private DependencyGraphTransformer dependencyGraphTransformer;

    private SessionData data;

    private RepositoryCache cache;

    /**
     * Creates an uninitialized session.
     */
    public DefaultRepositorySystemSession()
    {
        // enables default constructor
    }

    /**
     * Creates a shallow copy of the specified session.
     * 
     * @param session The session to copy, must not be {@code null}.
     */
    public DefaultRepositorySystemSession( RepositorySystemSession session )
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
        setRepositoryListener( session.getRepositoryListener() );
        setTransferListener( session.getTransferListener() );
        setSystemProperties( session.getSystemProperties() );
        setUserProperties( session.getUserProperties() );
        setConfigProperties( session.getConfigProperties() );
        setMirrorSelector( session.getMirrorSelector() );
        setProxySelector( session.getProxySelector() );
        setAuthenticationSelector( session.getAuthenticationSelector() );
        setArtifactTypeRegistry( session.getArtifactTypeRegistry() );
        setDependencyTraverser( session.getDependencyTraverser() );
        setDependencyManager( session.getDependencyManager() );
        setDependencySelector( session.getDependencySelector() );
        setDependencyGraphTransformer( session.getDependencyGraphTransformer() );
        setData( session.getData() );
        setCache( session.getCache() );
    }

    public boolean isOffline()
    {
        return offline;
    }

    public DefaultRepositorySystemSession setOffline( boolean offline )
    {
        this.offline = offline;
        return this;
    }

    public boolean isTransferErrorCachingEnabled()
    {
        return transferErrorCachingEnabled;
    }

    public DefaultRepositorySystemSession setTransferErrorCachingEnabled( boolean transferErrorCachingEnabled )
    {
        this.transferErrorCachingEnabled = transferErrorCachingEnabled;
        return this;
    }

    public boolean isNotFoundCachingEnabled()
    {
        return notFoundCachingEnabled;
    }

    public DefaultRepositorySystemSession setNotFoundCachingEnabled( boolean notFoundCachingEnabled )
    {
        this.notFoundCachingEnabled = notFoundCachingEnabled;
        return this;
    }

    public boolean isIgnoreMissingArtifactDescriptor()
    {
        return ignoreMissingArtifactDescriptor;
    }

    public DefaultRepositorySystemSession setIgnoreMissingArtifactDescriptor( boolean ignoreMissingArtifactDescriptor )
    {
        this.ignoreMissingArtifactDescriptor = ignoreMissingArtifactDescriptor;
        return this;
    }

    public boolean isIgnoreInvalidArtifactDescriptor()
    {
        return ignoreInvalidArtifactDescriptor;
    }

    public DefaultRepositorySystemSession setIgnoreInvalidArtifactDescriptor( boolean ignoreInvalidArtifactDescriptor )
    {
        this.ignoreInvalidArtifactDescriptor = ignoreInvalidArtifactDescriptor;
        return this;
    }

    public String getChecksumPolicy()
    {
        return checksumPolicy;
    }

    public DefaultRepositorySystemSession setChecksumPolicy( String checksumPolicy )
    {
        this.checksumPolicy = checksumPolicy;
        return this;
    }

    public String getUpdatePolicy()
    {
        return updatePolicy;
    }

    public DefaultRepositorySystemSession setUpdatePolicy( String updatePolicy )
    {
        this.updatePolicy = updatePolicy;
        return this;
    }

    public LocalRepository getLocalRepository()
    {
        return getLocalRepositoryManager().getRepository();
    }

    public LocalRepositoryManager getLocalRepositoryManager()
    {
        return localRepositoryManager;
    }

    public DefaultRepositorySystemSession setLocalRepositoryManager( LocalRepositoryManager localRepositoryManager )
    {
        this.localRepositoryManager = localRepositoryManager;
        return this;
    }

    public WorkspaceReader getWorkspaceReader()
    {
        return workspaceReader;
    }

    public DefaultRepositorySystemSession setWorkspaceReader( WorkspaceReader workspaceReader )
    {
        this.workspaceReader = workspaceReader;
        return this;
    }

    public RepositoryListener getRepositoryListener()
    {
        return repositoryListener;
    }

    public DefaultRepositorySystemSession setRepositoryListener( RepositoryListener repositoryListener )
    {
        this.repositoryListener = repositoryListener;
        return this;
    }

    public TransferListener getTransferListener()
    {
        return transferListener;
    }

    public DefaultRepositorySystemSession setTransferListener( TransferListener transferListener )
    {
        this.transferListener = transferListener;
        return this;
    }

    private <T> Map<String, T> toSafeMap( Map<?, ?> table, Class<T> valueType )
    {
        Map<String, T> map;
        if ( table == null || table.isEmpty() )
        {
            map = new HashMap<String, T>();
        }
        else
        {
            map = new LinkedHashMap<String, T>();
            for ( Object key : table.keySet() )
            {
                if ( key instanceof String )
                {
                    Object value = table.get( key );
                    if ( valueType.isInstance( value ) )
                    {
                        map.put( key.toString(), valueType.cast( value ) );
                    }
                }
            }
        }
        return map;
    }

    public Map<String, String> getSystemProperties()
    {
        return systemProperties;
    }

    public DefaultRepositorySystemSession setSystemProperties( Map<String, String> systemProperties )
    {
        if ( systemProperties == null )
        {
            this.systemProperties = new HashMap<String, String>();
        }
        else
        {
            this.systemProperties = systemProperties;
        }
        return this;
    }

    public DefaultRepositorySystemSession setSystemProps( Hashtable<?, ?> systemProperties )
    {
        this.systemProperties = toSafeMap( systemProperties, String.class );
        return this;
    }

    public DefaultRepositorySystemSession setSystemProperty( String key, String value )
    {
        if ( value != null )
        {
            systemProperties.put( key, value );
        }
        else
        {
            systemProperties.remove( key );
        }
        return this;
    }

    public Map<String, String> getUserProperties()
    {
        return userProperties;
    }

    public DefaultRepositorySystemSession setUserProperties( Map<String, String> userProperties )
    {
        if ( userProperties == null )
        {
            this.userProperties = new HashMap<String, String>();
        }
        else
        {
            this.userProperties = userProperties;
        }
        return this;
    }

    public DefaultRepositorySystemSession setUserProps( Map<?, ?> userProperties )
    {
        this.userProperties = toSafeMap( userProperties, String.class );
        return this;
    }

    public DefaultRepositorySystemSession setUserProperty( String key, String value )
    {
        if ( value != null )
        {
            userProperties.put( key, value );
        }
        else
        {
            userProperties.remove( key );
        }
        return this;
    }

    public Map<String, Object> getConfigProperties()
    {
        return configProperties;
    }

    public DefaultRepositorySystemSession setConfigProperties( Map<String, Object> configProperties )
    {
        if ( configProperties == null )
        {
            this.configProperties = new HashMap<String, Object>();
        }
        else
        {
            this.configProperties = configProperties;
        }
        return this;
    }

    public DefaultRepositorySystemSession setConfigProps( Map<?, ?> configProperties )
    {
        this.configProperties = toSafeMap( configProperties, Object.class );
        return this;
    }

    public DefaultRepositorySystemSession setConfigProperty( String key, Object value )
    {
        if ( value != null )
        {
            configProperties.put( key, value );
        }
        else
        {
            configProperties.remove( key );
        }
        return this;
    }

    public MirrorSelector getMirrorSelector()
    {
        return mirrorSelector;
    }

    public DefaultRepositorySystemSession setMirrorSelector( MirrorSelector mirrorSelector )
    {
        this.mirrorSelector = mirrorSelector;
        return this;
    }

    public ProxySelector getProxySelector()
    {
        return proxySelector;
    }

    public DefaultRepositorySystemSession setProxySelector( ProxySelector proxySelector )
    {
        this.proxySelector = proxySelector;
        return this;
    }

    public AuthenticationSelector getAuthenticationSelector()
    {
        return authenticationSelector;
    }

    public DefaultRepositorySystemSession setAuthenticationSelector( AuthenticationSelector authenticationSelector )
    {
        this.authenticationSelector = authenticationSelector;
        return this;
    }

    public ArtifactTypeRegistry getArtifactTypeRegistry()
    {
        return artifactTypeRegistry;
    }

    public DefaultRepositorySystemSession setArtifactTypeRegistry( ArtifactTypeRegistry artifactTypeRegistry )
    {
        this.artifactTypeRegistry = artifactTypeRegistry;
        return this;
    }

    public DependencyTraverser getDependencyTraverser()
    {
        return dependencyTraverser;
    }

    public DefaultRepositorySystemSession setDependencyTraverser( DependencyTraverser dependencyTraverser )
    {
        this.dependencyTraverser = dependencyTraverser;
        return this;
    }

    public DependencyManager getDependencyManager()
    {
        return dependencyManager;
    }

    public DefaultRepositorySystemSession setDependencyManager( DependencyManager dependencyManager )
    {
        this.dependencyManager = dependencyManager;
        return this;
    }

    public DependencySelector getDependencySelector()
    {
        return dependencySelector;
    }

    public DefaultRepositorySystemSession setDependencySelector( DependencySelector dependencySelector )
    {
        this.dependencySelector = dependencySelector;
        return this;
    }

    public DependencyGraphTransformer getDependencyGraphTransformer()
    {
        return dependencyGraphTransformer;
    }

    public DefaultRepositorySystemSession setDependencyGraphTransformer( DependencyGraphTransformer dependencyGraphTransformer )
    {
        this.dependencyGraphTransformer = dependencyGraphTransformer;
        return this;
    }

    public RepositoryCache getCache()
    {
        return cache;
    }

    public DefaultRepositorySystemSession setData( SessionData data )
    {
        this.data = data;
        return this;
    }

    public SessionData getData()
    {
        return data;
    }

    public DefaultRepositorySystemSession setCache( RepositoryCache cache )
    {
        this.cache = cache;
        return this;
    }

}
