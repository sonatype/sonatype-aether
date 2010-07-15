package org.sonatype.maven.repository.util;

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

import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.sonatype.maven.repository.ArtifactTypeRegistry;
import org.sonatype.maven.repository.AuthenticationSelector;
import org.sonatype.maven.repository.DependencySelector;
import org.sonatype.maven.repository.DependencyGraphTransformer;
import org.sonatype.maven.repository.DependencyManager;
import org.sonatype.maven.repository.DependencyTraverser;
import org.sonatype.maven.repository.LocalRepository;
import org.sonatype.maven.repository.LocalRepositoryManager;
import org.sonatype.maven.repository.MirrorSelector;
import org.sonatype.maven.repository.ProxySelector;
import org.sonatype.maven.repository.RepositoryCache;
import org.sonatype.maven.repository.RepositoryListener;
import org.sonatype.maven.repository.RepositorySystemSession;
import org.sonatype.maven.repository.TransferListener;
import org.sonatype.maven.repository.WorkspaceReader;

/**
 * @author Benjamin Bentmann
 */
public class DefaultRepositorySystemSession
    implements RepositorySystemSession
{

    private String id;

    private String userAgent;

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

    private Map<String, String> systemProperties = Collections.emptyMap();

    private Map<String, String> userProperties = Collections.emptyMap();

    private Map<String, String> configProperties = Collections.emptyMap();

    private MirrorSelector mirrorSelector;

    private ProxySelector proxySelector;

    private AuthenticationSelector authenticationSelector;

    private ArtifactTypeRegistry artifactTypeRegistry;

    private DependencyTraverser dependencyTraverser;

    private DependencyManager dependencyManager;

    private DependencySelector dependencySelector;

    private DependencyGraphTransformer dependencyGraphTransformer;

    private RepositoryCache cache;

    public static DefaultRepositorySystemSession newMavenRepositorySystemSession()
    {
        DefaultRepositorySystemSession session = new DefaultRepositorySystemSession();

        session.setMirrorSelector( new DefaultMirrorSelector() );
        session.setAuthenticationSelector( new DefaultAuthenticationSelector() );
        session.setProxySelector( new DefaultProxySelector() );

        DependencyTraverser depTraverser = new FatArtifactTraverser();
        session.setDependencyTraverser( depTraverser );

        DependencyManager depManager = new ClassicDependencyManager();
        session.setDependencyManager( depManager );

        DependencySelector depFilter =
            new AndDependencySelector( new ScopeDependencySelector( "test", "provided" ),
                                       new OptionalDependencySelector(), new ExclusionDependencySelector() );
        session.setDependencySelector( depFilter );

        DependencyGraphTransformer transformer =
            new ChainedDependencyGraphTransformer( new ConflictMarker(), new JavaEffectiveScopeCalculator(),
                                                   new ClassicVersionConflictResolver(),
                                                   new JavaDependencyContextRefiner() );
        session.setDependencyGraphTransformer( transformer );

        DefaultArtifactTypeRegistry stereotypes = new DefaultArtifactTypeRegistry();
        stereotypes.add( new DefaultArtifactType( "pom" ) );
        stereotypes.add( new DefaultArtifactType( "maven-plugin", "jar", "", "java" ) );
        stereotypes.add( new DefaultArtifactType( "jar", "jar", "", "java" ) );
        stereotypes.add( new DefaultArtifactType( "ejb", "jar", "", "java" ) );
        stereotypes.add( new DefaultArtifactType( "ejb-client", "jar", "client", "java" ) );
        stereotypes.add( new DefaultArtifactType( "test-jar", "jar", "tests", "java" ) );
        stereotypes.add( new DefaultArtifactType( "javadoc", "jar", "javadoc", "java" ) );
        stereotypes.add( new DefaultArtifactType( "java-source", "jar", "sources", "java", false, false ) );
        stereotypes.add( new DefaultArtifactType( "war", "war", "", "java", false, true ) );
        stereotypes.add( new DefaultArtifactType( "ear", "ear", "", "java", false, true ) );
        stereotypes.add( new DefaultArtifactType( "rar", "rar", "", "java", false, true ) );
        stereotypes.add( new DefaultArtifactType( "par", "par", "", "java", false, true ) );
        session.setArtifactTypeRegistry( stereotypes );

        session.setIgnoreInvalidArtifactDescriptor( true );
        session.setIgnoreMissingArtifactDescriptor( true );

        session.setSystemProperties( (Map<String, String>) (Map) System.getProperties() );

        return session;
    }

    public DefaultRepositorySystemSession()
    {
        // enables default constructor
        setId( null );
    }

    public DefaultRepositorySystemSession( RepositorySystemSession session )
    {
        setUserAgent( session.getUserAgent() );
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
        setCache( session.getCache() );
    }

    public String getId()
    {
        return id;
    }

    public DefaultRepositorySystemSession setId( String id )
    {
        this.id = ( id != null ) ? id : UUID.randomUUID().toString().replace( "-", "" );
        return this;
    }

    public String getUserAgent()
    {
        return userAgent;
    }

    public DefaultRepositorySystemSession setUserAgent( String userAgent )
    {
        this.userAgent = userAgent;
        return this;
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

    private Map<String, String> toMap( Hashtable<?, ?> table )
    {
        Map<String, String> map;
        if ( table == null || table.isEmpty() )
        {
            map = Collections.emptyMap();
        }
        else
        {
            map = new LinkedHashMap<String, String>();
            for ( Object key : table.keySet() )
            {
                if ( key instanceof String )
                {
                    Object value = table.get( key );
                    if ( value instanceof String )
                    {
                        map.put( key.toString(), value.toString() );
                    }
                }
            }
            map = Collections.unmodifiableMap( map );
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
            this.systemProperties = Collections.emptyMap();
        }
        else
        {
            this.systemProperties = Collections.unmodifiableMap( systemProperties );
        }
        return this;
    }

    public DefaultRepositorySystemSession setSystemProperties( Hashtable<?, ?> systemProperties )
    {
        this.systemProperties = toMap( systemProperties );
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
            this.userProperties = Collections.emptyMap();
        }
        else
        {
            this.userProperties = Collections.unmodifiableMap( userProperties );
        }
        return this;
    }

    public DefaultRepositorySystemSession setUserProperties( Hashtable<?, ?> userProperties )
    {
        this.userProperties = toMap( userProperties );
        return this;
    }

    public Map<String, String> getConfigProperties()
    {
        return configProperties;
    }

    public DefaultRepositorySystemSession setConfigProperties( Map<String, String> configProperties )
    {
        if ( configProperties == null )
        {
            this.configProperties = Collections.emptyMap();
        }
        else
        {
            this.configProperties = Collections.unmodifiableMap( configProperties );
        }
        return this;
    }

    public DefaultRepositorySystemSession setConfigProperties( Hashtable<?, ?> configProperties )
    {
        this.configProperties = toMap( configProperties );
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

    public DefaultRepositorySystemSession setDependencyGraphTransformer(
                                                                         DependencyGraphTransformer dependencyGraphTransformer )
    {
        this.dependencyGraphTransformer = dependencyGraphTransformer;
        return this;
    }

    public RepositoryCache getCache()
    {
        return cache;
    }

    public DefaultRepositorySystemSession setCache( RepositoryCache cache )
    {
        this.cache = cache;
        return this;
    }

}
