package org.sonatype.aether.test.impl;

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
import java.util.Map;

import org.sonatype.aether.RepositoryCache;
import org.sonatype.aether.RepositoryException;
import org.sonatype.aether.RepositoryListener;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.SessionData;
import org.sonatype.aether.artifact.ArtifactTypeRegistry;
import org.sonatype.aether.collection.DependencyCollectionContext;
import org.sonatype.aether.collection.DependencyGraphTransformationContext;
import org.sonatype.aether.collection.DependencyGraphTransformer;
import org.sonatype.aether.collection.DependencyManagement;
import org.sonatype.aether.collection.DependencyManager;
import org.sonatype.aether.collection.DependencySelector;
import org.sonatype.aether.collection.DependencyTraverser;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.repository.AuthenticationSelector;
import org.sonatype.aether.repository.LocalRepository;
import org.sonatype.aether.repository.LocalRepositoryManager;
import org.sonatype.aether.repository.MirrorSelector;
import org.sonatype.aether.repository.ProxySelector;
import org.sonatype.aether.repository.RepositoryPolicy;
import org.sonatype.aether.repository.WorkspaceReader;
import org.sonatype.aether.transfer.TransferListener;

public class TestRepositorySystemSession
    implements RepositorySystemSession
{

    private SessionData data = new TestSessionData();

    private TransferListener listener;

    private RepositoryListener repositoryListener;

    private AuthenticationSelector authenticator = new TestAuthenticationSelector();

    private ProxySelector proxySelector = new TestProxySelector();

    private LocalRepositoryManager localRepositoryManager = new TestLocalRepositoryManager();

    private boolean transferErrorCaching;

    private boolean notFoundCaching;

    private DependencyManager dependencyManager;

    public TransferListener getTransferListener()
    {
        return listener;
    }

    public Map<String, Object> getConfigProperties()
    {
        return Collections.emptyMap();
    }

    public boolean isOffline()
    {
        return false;
    }

    public boolean isTransferErrorCachingEnabled()
    {
        return transferErrorCaching;
    }

    public boolean isNotFoundCachingEnabled()
    {
        return notFoundCaching;
    }

    public boolean isIgnoreMissingArtifactDescriptor()
    {
        return false;
    }

    public boolean isIgnoreInvalidArtifactDescriptor()
    {
        return false;
    }

    public String getChecksumPolicy()
    {
        return RepositoryPolicy.CHECKSUM_POLICY_FAIL;
    }

    public String getUpdatePolicy()
    {
        return RepositoryPolicy.UPDATE_POLICY_ALWAYS;
    }

    public LocalRepository getLocalRepository()
    {
        return this.localRepositoryManager.getRepository();
    }

    public LocalRepositoryManager getLocalRepositoryManager()
    {
        return localRepositoryManager;
    }

    public WorkspaceReader getWorkspaceReader()
    {
        // throw new UnsupportedOperationException( "getWorkspaceReader()" );
        return null;
    }

    public RepositoryListener getRepositoryListener()
    {
        return repositoryListener;
    }

    public Map<String, String> getSystemProperties()
    {
        return Collections.emptyMap();
    }

    public Map<String, String> getUserProperties()
    {
        return Collections.emptyMap();
    }

    public MirrorSelector getMirrorSelector()
    {
        return null;
    }

    public ProxySelector getProxySelector()
    {
        return proxySelector;
    }

    public AuthenticationSelector getAuthenticationSelector()
    {
        return authenticator;
    }

    public ArtifactTypeRegistry getArtifactTypeRegistry()
    {
        return null;
    }

    public DependencyTraverser getDependencyTraverser()
    {
        return new DependencyTraverser()
        {

            public boolean traverseDependency( Dependency dependency )
            {
                return true;
            }

            public DependencyTraverser deriveChildTraverser( DependencyCollectionContext context )
            {
                return this;
            }
        };
    }

    public DependencyManager getDependencyManager()
    {
        if ( dependencyManager == null )
        {
            return new DependencyManager()
            {

                public DependencyManagement manageDependency( Dependency dependency )
                {
                    return null;
                }

                public DependencyManager deriveChildManager( DependencyCollectionContext context )
                {
                    return this;
                }
            };
        }
        else
        {
            return dependencyManager;
        }
    }

    public DependencySelector getDependencySelector()
    {
        return new DependencySelector()
        {

            public boolean selectDependency( Dependency dependency )
            {
                return true;
            }

            public DependencySelector deriveChildSelector( DependencyCollectionContext context )
            {
                return this;
            }
        };
    }

    public DependencyGraphTransformer getDependencyGraphTransformer()
    {
        return new DependencyGraphTransformer()
        {

            public DependencyNode transformGraph( DependencyNode node, DependencyGraphTransformationContext context )
                throws RepositoryException
            {
                return node;
            }
        };
    }

    public SessionData getData()
    {
        return data;
    }

    public RepositoryCache getCache()
    {
        return null;
    }

    public void setRepositoryListener( RepositoryListener repositoryListener )
    {
        this.repositoryListener = repositoryListener;
    }

    public void setTransferListener( TransferListener listener )
    {
        this.listener = listener;
    }

    /**
     * @param b
     */
    public void setTransferErrorCachingEnabled( boolean b )
    {
        this.transferErrorCaching = b;

    }

    /**
     * @param b
     */
    public void setNotFoundCachingEnabled( boolean b )
    {
        this.notFoundCaching = b;
    }

    public void setLocalRepositoryManager( LocalRepositoryManager localRepositoryManager )
    {
        this.localRepositoryManager = localRepositoryManager;
    }

    /**
     * @param classicDependencyManager
     */
    public void setDependencyManager( DependencyManager manager )
    {
        this.dependencyManager = manager;

    }
}
