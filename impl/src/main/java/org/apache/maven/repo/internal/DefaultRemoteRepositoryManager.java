package org.apache.maven.repo.internal;

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
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import org.apache.maven.repo.AuthenticationSelector;
import org.apache.maven.repo.MirrorSelector;
import org.apache.maven.repo.NoRepositoryConnectorException;
import org.apache.maven.repo.ProxySelector;
import org.apache.maven.repo.RemoteRepository;
import org.apache.maven.repo.RepositoryPolicy;
import org.apache.maven.repo.RepositorySession;
import org.apache.maven.repo.spi.Logger;
import org.apache.maven.repo.spi.NullLogger;
import org.apache.maven.repo.spi.PluggableComponent;
import org.apache.maven.repo.spi.RemoteRepositoryManager;
import org.apache.maven.repo.spi.RepositoryConnector;
import org.apache.maven.repo.spi.RepositoryConnectorFactory;
import org.apache.maven.repo.spi.UpdateCheckManager;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;

/**
 * @author Benjamin Bentmann
 */
@Component( role = RemoteRepositoryManager.class, hint = "default" )
public class DefaultRemoteRepositoryManager
    implements RemoteRepositoryManager
{

    @Requirement
    private Logger logger = NullLogger.INSTANCE;

    @Requirement
    private UpdateCheckManager updateCheckManager;

    @Requirement( role = RepositoryConnectorFactory.class )
    private List<RepositoryConnectorFactory> connectorFactories = new ArrayList<RepositoryConnectorFactory>();

    public DefaultRemoteRepositoryManager setLogger( Logger logger )
    {
        this.logger = ( logger != null ) ? logger : NullLogger.INSTANCE;
        return this;
    }

    public DefaultRemoteRepositoryManager setUpdateCheckManager( UpdateCheckManager updateCheckManager )
    {
        if ( updateCheckManager == null )
        {
            throw new IllegalArgumentException( "update check manager has not been specified" );
        }
        this.updateCheckManager = updateCheckManager;
        return this;
    }

    public DefaultRemoteRepositoryManager addRepositoryConnectorFactory( RepositoryConnectorFactory factory )
    {
        if ( factory == null )
        {
            throw new IllegalArgumentException( "repository connector factory has not been specified" );
        }
        connectorFactories.add( factory );
        return this;
    }

    public List<RemoteRepository> aggregateRepositories( RepositorySession session,
                                                         List<RemoteRepository> effectiveRepositories,
                                                         List<RemoteRepository> rawRepositories )
    {
        if ( rawRepositories.isEmpty() )
        {
            return effectiveRepositories;
        }

        MirrorSelector mirrorSelector = session.getMirrorSelector();
        AuthenticationSelector authSelector = session.getAuthenticationSelector();
        ProxySelector proxySelector = session.getProxySelector();

        List<RemoteRepository> result = new ArrayList<RemoteRepository>( effectiveRepositories );

        next: for ( RemoteRepository rawRepository : rawRepositories )
        {
            RemoteRepository mirrorRepository = mirrorSelector.getMirror( rawRepository );

            RemoteRepository repository = ( mirrorRepository != null ) ? mirrorRepository : rawRepository;

            String key = getKey( repository );

            for ( ListIterator<RemoteRepository> it = result.listIterator(); it.hasNext(); )
            {
                RemoteRepository effectiveRepository = it.next();

                if ( key.equals( getKey( effectiveRepository ) ) )
                {
                    if ( !effectiveRepository.getMirroredRepositories().isEmpty()
                        && !repository.getMirroredRepositories().isEmpty() )
                    {
                        RemoteRepository mergedRepository = new RemoteRepository();

                        mergedRepository.setRepositoryManager( effectiveRepository.isRepositoryManager() );

                        mergedRepository.setId( effectiveRepository.getId() );
                        mergedRepository.setType( effectiveRepository.getType() );
                        mergedRepository.setUrl( effectiveRepository.getUrl() );

                        mergedRepository.setAuthentication( effectiveRepository.getAuthentication() );
                        mergedRepository.setProxy( effectiveRepository.getProxy() );

                        mergedRepository.setPolicy( true, merge( session, effectiveRepository.getPolicy( true ),
                                                                 repository.getPolicy( true ) ) );
                        mergedRepository.setPolicy( false, merge( session, effectiveRepository.getPolicy( false ),
                                                                  repository.getPolicy( false ) ) );

                        List<RemoteRepository> mirroredRepositories = effectiveRepository.getMirroredRepositories();
                        String rawKey = getKey( rawRepository );
                        RemoteRepository mirroredRepository = null;
                        for ( RemoteRepository repo : mirroredRepositories )
                        {
                            if ( rawKey.equals( getKey( repo ) ) )
                            {
                                mirroredRepository = repo;
                                break;
                            }
                        }
                        if ( mirroredRepository == null )
                        {
                            mirroredRepositories = new ArrayList<RemoteRepository>( mirroredRepositories );
                            mirroredRepositories.add( rawRepository );
                        }
                        mergedRepository.setMirroredRepositories( mirroredRepositories );

                        it.set( mergedRepository );
                    }

                    continue next;
                }
            }

            repository.setAuthentication( authSelector.getAuthentication( repository ) );
            repository.setProxy( proxySelector.getProxy( repository ) );
            result.add( repository );
        }

        return result;
    }

    private String getKey( RemoteRepository repository )
    {
        return repository.getId();
    }

    public RepositoryPolicy getPolicy( RepositorySession session, RemoteRepository repository, boolean releases,
                                       boolean snapshots )
    {
        RepositoryPolicy policy;

        // get effective per-repository policy
        if ( releases && snapshots )
        {
            policy = merge( session, repository.getPolicy( false ), repository.getPolicy( true ) );
        }
        else
        {
            policy = new RepositoryPolicy( repository.getPolicy( snapshots ) );
        }

        // superimpose global policy
        if ( StringUtils.isNotEmpty( session.getChecksumPolicy() ) )
        {
            policy.setChecksumPolicy( session.getChecksumPolicy() );
        }
        if ( StringUtils.isNotEmpty( session.getUpdatePolicy() ) )
        {
            policy.setUpdatePolicy( session.getUpdatePolicy() );
        }

        return policy;
    }

    private RepositoryPolicy merge( RepositorySession session, RepositoryPolicy policy1, RepositoryPolicy policy2 )
    {
        RepositoryPolicy policy = new RepositoryPolicy( policy1 );

        if ( policy2.isEnabled() )
        {
            policy.setEnabled( true );

            if ( ordinalOfChecksumPolicy( policy2.getChecksumPolicy() ) < ordinalOfChecksumPolicy( policy.getChecksumPolicy() ) )
            {
                policy.setChecksumPolicy( policy2.getChecksumPolicy() );
            }

            policy.setChecksumPolicy( updateCheckManager.getEffectiveUpdatePolicy( session, policy.getChecksumPolicy(),
                                                                                   policy2.getChecksumPolicy() ) );
        }

        return policy;
    }

    private int ordinalOfChecksumPolicy( String policy )
    {
        if ( RepositoryPolicy.CHECKSUM_POLICY_FAIL.equals( policy ) )
        {
            return 2;
        }
        else if ( RepositoryPolicy.CHECKSUM_POLICY_IGNORE.equals( policy ) )
        {
            return 0;
        }
        else
        {
            return 1;
        }
    }

    public RepositoryConnector getRepositoryConnector( RepositorySession session, RemoteRepository repository )
        throws NoRepositoryConnectorException
    {
        List<RepositoryConnectorFactory> factories = new ArrayList<RepositoryConnectorFactory>( connectorFactories );
        Collections.sort( factories, PluggableComponent.COMPARATOR );

        for ( RepositoryConnectorFactory factory : factories )
        {
            try
            {
                return factory.newInstance( session, repository );
            }
            catch ( NoRepositoryConnectorException e )
            {
                // continue and try next factory
            }
        }

        throw new NoRepositoryConnectorException( repository );
    }

}
