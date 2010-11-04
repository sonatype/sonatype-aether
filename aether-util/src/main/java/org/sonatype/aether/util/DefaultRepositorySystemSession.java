package org.sonatype.aether.util;

/*******************************************************************************
 * Copyright (c) 2010 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;

import org.sonatype.aether.RepositoryCache;
import org.sonatype.aether.RepositoryListener;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.SessionData;
import org.sonatype.aether.artifact.ArtifactType;
import org.sonatype.aether.artifact.ArtifactTypeRegistry;
import org.sonatype.aether.collection.DependencyGraphTransformer;
import org.sonatype.aether.collection.DependencyManager;
import org.sonatype.aether.collection.DependencySelector;
import org.sonatype.aether.collection.DependencyTraverser;
import org.sonatype.aether.repository.Authentication;
import org.sonatype.aether.repository.AuthenticationSelector;
import org.sonatype.aether.repository.LocalRepository;
import org.sonatype.aether.repository.LocalRepositoryManager;
import org.sonatype.aether.repository.MirrorSelector;
import org.sonatype.aether.repository.Proxy;
import org.sonatype.aether.repository.ProxySelector;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.repository.WorkspaceReader;
import org.sonatype.aether.transfer.TransferListener;
import org.sonatype.aether.util.graph.manager.NoopDependencyManager;
import org.sonatype.aether.util.graph.selector.StaticDependencySelector;
import org.sonatype.aether.util.graph.transformer.NoopDependencyGraphTransformer;
import org.sonatype.aether.util.graph.traverser.StaticDependencyTraverser;

/**
 * A simple repository system session.
 * 
 * @author Benjamin Bentmann
 */
public class DefaultRepositorySystemSession
    implements RepositorySystemSession
{

    private static final DependencyTraverser TRAVERSER = new StaticDependencyTraverser( true );

    private static final DependencyManager MANAGER = NoopDependencyManager.INSTANCE;

    private static final DependencySelector SELECTOR = new StaticDependencySelector( true );

    private static final DependencyGraphTransformer TRANSFORMER = NoopDependencyGraphTransformer.INSTANCE;

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

    private MirrorSelector mirrorSelector = NullMirrorSelector.INSTANCE;

    private ProxySelector proxySelector = NullProxySelector.INSTANCE;

    private AuthenticationSelector authenticationSelector = NullAuthenticationSelector.INSTANCE;

    private ArtifactTypeRegistry artifactTypeRegistry = NullArtifactTypeRegistry.INSTANCE;

    private DependencyTraverser dependencyTraverser = TRAVERSER;

    private DependencyManager dependencyManager = MANAGER;

    private DependencySelector dependencySelector = SELECTOR;

    private DependencyGraphTransformer dependencyGraphTransformer = TRANSFORMER;

    private SessionData data = new DefaultSessionData();

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
        LocalRepositoryManager lrm = getLocalRepositoryManager();
        return ( lrm != null ) ? lrm.getRepository() : null;
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
        if ( this.mirrorSelector == null )
        {
            this.mirrorSelector = NullMirrorSelector.INSTANCE;
        }
        return this;
    }

    public ProxySelector getProxySelector()
    {
        return proxySelector;
    }

    public DefaultRepositorySystemSession setProxySelector( ProxySelector proxySelector )
    {
        this.proxySelector = proxySelector;
        if ( this.proxySelector == null )
        {
            this.proxySelector = NullProxySelector.INSTANCE;
        }
        return this;
    }

    public AuthenticationSelector getAuthenticationSelector()
    {
        return authenticationSelector;
    }

    public DefaultRepositorySystemSession setAuthenticationSelector( AuthenticationSelector authenticationSelector )
    {
        this.authenticationSelector = authenticationSelector;
        if ( this.authenticationSelector == null )
        {
            this.authenticationSelector = NullAuthenticationSelector.INSTANCE;
        }
        return this;
    }

    public ArtifactTypeRegistry getArtifactTypeRegistry()
    {
        return artifactTypeRegistry;
    }

    public DefaultRepositorySystemSession setArtifactTypeRegistry( ArtifactTypeRegistry artifactTypeRegistry )
    {
        this.artifactTypeRegistry = artifactTypeRegistry;
        if ( this.artifactTypeRegistry == null )
        {
            this.artifactTypeRegistry = NullArtifactTypeRegistry.INSTANCE;
        }
        return this;
    }

    public DependencyTraverser getDependencyTraverser()
    {
        return dependencyTraverser;
    }

    public DefaultRepositorySystemSession setDependencyTraverser( DependencyTraverser dependencyTraverser )
    {
        this.dependencyTraverser = dependencyTraverser;
        if ( this.dependencyTraverser == null )
        {
            this.dependencyTraverser = TRAVERSER;
        }
        return this;
    }

    public DependencyManager getDependencyManager()
    {
        return dependencyManager;
    }

    public DefaultRepositorySystemSession setDependencyManager( DependencyManager dependencyManager )
    {
        this.dependencyManager = dependencyManager;
        if ( this.dependencyManager == null )
        {
            this.dependencyManager = MANAGER;
        }
        return this;
    }

    public DependencySelector getDependencySelector()
    {
        return dependencySelector;
    }

    public DefaultRepositorySystemSession setDependencySelector( DependencySelector dependencySelector )
    {
        this.dependencySelector = dependencySelector;
        if ( this.dependencySelector == null )
        {
            this.dependencySelector = SELECTOR;
        }
        return this;
    }

    public DependencyGraphTransformer getDependencyGraphTransformer()
    {
        return dependencyGraphTransformer;
    }

    public DefaultRepositorySystemSession setDependencyGraphTransformer( DependencyGraphTransformer dependencyGraphTransformer )
    {
        this.dependencyGraphTransformer = dependencyGraphTransformer;
        if ( this.dependencyGraphTransformer == null )
        {
            this.dependencyGraphTransformer = TRANSFORMER;
        }
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

    static class NullProxySelector
        implements ProxySelector
    {

        public static final ProxySelector INSTANCE = new NullProxySelector();

        public Proxy getProxy( RemoteRepository repository )
        {
            return null;
        }

    }

    static class NullMirrorSelector
        implements MirrorSelector
    {

        public static final MirrorSelector INSTANCE = new NullMirrorSelector();

        public RemoteRepository getMirror( RemoteRepository repository )
        {
            return null;
        }

    }

    static class NullAuthenticationSelector
        implements AuthenticationSelector
    {

        public static final AuthenticationSelector INSTANCE = new NullAuthenticationSelector();

        public Authentication getAuthentication( RemoteRepository repository )
        {
            return null;
        }

    }

    static class NullArtifactTypeRegistry
        implements ArtifactTypeRegistry
    {

        public static final ArtifactTypeRegistry INSTANCE = new NullArtifactTypeRegistry();

        public ArtifactType get( String typeId )
        {
            return null;
        }

    }

}
