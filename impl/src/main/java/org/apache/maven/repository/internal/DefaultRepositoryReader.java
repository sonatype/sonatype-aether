package org.apache.maven.repository.internal;

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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

import org.apache.maven.repository.Artifact;
import org.apache.maven.repository.ArtifactMultiTransferException;
import org.apache.maven.repository.ArtifactRequest;
import org.apache.maven.repository.ArtifactTransferException;
import org.apache.maven.repository.Authentication;
import org.apache.maven.repository.AuthenticationSelector;
import org.apache.maven.repository.MetadataMultiTransferException;
import org.apache.maven.repository.MetadataRequest;
import org.apache.maven.repository.Proxy;
import org.apache.maven.repository.ProxySelector;
import org.apache.maven.repository.RemoteRepository;
import org.apache.maven.repository.RepositoryContext;
import org.apache.maven.repository.RepositoryReader;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.authentication.AuthenticationInfo;
import org.apache.maven.wagon.proxy.ProxyInfo;
import org.apache.maven.wagon.proxy.ProxyInfoProvider;
import org.apache.maven.wagon.repository.Repository;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLifecycleException;

/**
 * @author Benjamin Bentmann
 */
class DefaultRepositoryReader
    implements RepositoryReader
{

    private final PlexusContainer container;

    private final RemoteRepository repository;

    private final RepositoryContext context;

    private Wagon wagon;

    public DefaultRepositoryReader( PlexusContainer container, RemoteRepository repository, RepositoryContext context )
    {
        this.container = container;
        this.repository = repository;
        this.context = context;
    }

    public void getArtifacts( ArtifactRequest request )
        throws ArtifactMultiTransferException
    {
        Collection<? extends Artifact> artifacts = request.getArtifacts();
        if ( artifacts.isEmpty() )
        {
            return;
        }

        try
        {
            connect();
        }
        catch ( Exception e )
        {
            Collection<ArtifactTransferException> exceptions = new ArrayList<ArtifactTransferException>();
            for ( Artifact artifact : artifacts )
            {
                exceptions.add( new ArtifactTransferException( artifact, e ) );
            }
            throw new ArtifactMultiTransferException( exceptions );
        }

        // TODO Auto-generated method stub

    }

    public void getMetadata( MetadataRequest request )
        throws MetadataMultiTransferException
    {
        // TODO Auto-generated method stub

    }

    private void connect()
        throws Exception
    {
        if ( wagon == null )
        {
            Repository repo = new Repository( repository.getId(), repository.getUrl() );

            AuthenticationInfo auth = null;
            AuthenticationSelector authSelector = context.getAuthenticationSelector();
            if ( authSelector != null )
            {
                Authentication a = authSelector.getAuthentication( repository );
                if ( a != null )
                {
                    auth = new AuthenticationInfo();
                    auth.setUserName( a.getUsername() );
                    auth.setPassword( a.getPassword() );
                    auth.setPrivateKey( a.getPrivateKeyFile() );
                    auth.setPassphrase( a.getPassphrase() );
                }
            }

            ProxyInfoProvider proxy = null;
            ProxySelector proxySelector = context.getProxySelector();
            if ( proxySelector != null )
            {
                Proxy p = proxySelector.getProxy( repository );
                if ( p != null )
                {
                    final ProxyInfo prox = new ProxyInfo();
                    prox.setType( p.getType() );
                    prox.setHost( p.getHost() );
                    prox.setPort( p.getPort() );
                    if ( p.getAuthentication() != null )
                    {
                        prox.setUserName( p.getAuthentication().getUsername() );
                        prox.setPassword( p.getAuthentication().getPassword() );
                    }
                    proxy = new ProxyInfoProvider()
                    {
                        public ProxyInfo getProxyInfo( String protocol )
                        {
                            return prox;
                        }
                    };
                }
            }

            wagon = container.lookup( Wagon.class, repo.getProtocol().toLowerCase( Locale.ENGLISH ) );

            wagon.connect( repo, auth, proxy );
        }
    }

    public void close()
    {
        if ( wagon != null )
        {
            try
            {
                container.release( wagon );
            }
            catch ( ComponentLifecycleException e )
            {
                // who cares
            }
        }
    }

}
