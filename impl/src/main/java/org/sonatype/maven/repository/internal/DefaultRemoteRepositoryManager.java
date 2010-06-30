package org.sonatype.maven.repository.internal;

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

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.maven.repository.AuthenticationSelector;
import org.sonatype.maven.repository.MirrorSelector;
import org.sonatype.maven.repository.NoRepositoryConnectorException;
import org.sonatype.maven.repository.ProxySelector;
import org.sonatype.maven.repository.RemoteRepository;
import org.sonatype.maven.repository.RepositoryPolicy;
import org.sonatype.maven.repository.RepositorySystemSession;
import org.sonatype.maven.repository.spi.Logger;
import org.sonatype.maven.repository.spi.NullLogger;
import org.sonatype.maven.repository.spi.PluggableComponent;
import org.sonatype.maven.repository.spi.RemoteRepositoryManager;
import org.sonatype.maven.repository.spi.RepositoryConnector;
import org.sonatype.maven.repository.spi.RepositoryConnectorFactory;
import org.sonatype.maven.repository.spi.UpdateCheckManager;

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

    public List<RemoteRepository> aggregateRepositories( RepositorySystemSession session,
                                                         List<RemoteRepository> dominantRepositories,
                                                         List<RemoteRepository> recessiveRepositories,
                                                         boolean recessiveIsRaw )
    {
        if ( recessiveRepositories.isEmpty() )
        {
            return dominantRepositories;
        }

        MirrorSelector mirrorSelector = session.getMirrorSelector();
        AuthenticationSelector authSelector = session.getAuthenticationSelector();
        ProxySelector proxySelector = session.getProxySelector();

        List<RemoteRepository> result = new ArrayList<RemoteRepository>( dominantRepositories );

        next: for ( RemoteRepository recessiveRepository : recessiveRepositories )
        {
            RemoteRepository repository = recessiveRepository;

            if ( recessiveIsRaw )
            {
                RemoteRepository mirrorRepository = mirrorSelector.getMirror( recessiveRepository );

                repository = ( mirrorRepository != null ) ? mirrorRepository : recessiveRepository;
            }

            String key = getKey( repository );

            for ( ListIterator<RemoteRepository> it = result.listIterator(); it.hasNext(); )
            {
                RemoteRepository dominantRepository = it.next();

                if ( key.equals( getKey( dominantRepository ) ) )
                {
                    if ( !dominantRepository.getMirroredRepositories().isEmpty()
                        && !repository.getMirroredRepositories().isEmpty() )
                    {
                        RemoteRepository mergedRepository = new RemoteRepository();

                        mergedRepository.setRepositoryManager( dominantRepository.isRepositoryManager() );

                        mergedRepository.setId( dominantRepository.getId() );
                        mergedRepository.setContentType( dominantRepository.getContentType() );
                        mergedRepository.setUrl( dominantRepository.getUrl() );

                        mergedRepository.setAuthentication( dominantRepository.getAuthentication() );
                        mergedRepository.setProxy( dominantRepository.getProxy() );

                        mergedRepository.setPolicy( true, merge( session, dominantRepository.getPolicy( true ),
                                                                 repository.getPolicy( true ) ) );
                        mergedRepository.setPolicy( false, merge( session, dominantRepository.getPolicy( false ),
                                                                  repository.getPolicy( false ) ) );

                        List<RemoteRepository> mirroredRepositories = dominantRepository.getMirroredRepositories();
                        String rawKey = getKey( recessiveRepository );
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
                            mirroredRepositories.add( recessiveRepository );
                        }
                        mergedRepository.setMirroredRepositories( mirroredRepositories );

                        it.set( mergedRepository );
                    }

                    continue next;
                }
            }

            if ( recessiveIsRaw )
            {
                repository.setAuthentication( authSelector.getAuthentication( repository ) );
                repository.setProxy( proxySelector.getProxy( repository ) );
            }

            result.add( repository );
        }

        return result;
    }

    private String getKey( RemoteRepository repository )
    {
        return repository.getId();
    }

    public RepositoryPolicy getPolicy( RepositorySystemSession session, RemoteRepository repository, boolean releases,
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
            policy = repository.getPolicy( snapshots );
        }

        // superimpose global policy
        if ( StringUtils.isNotEmpty( session.getChecksumPolicy() ) )
        {
            policy = policy.setChecksumPolicy( session.getChecksumPolicy() );
        }
        if ( StringUtils.isNotEmpty( session.getUpdatePolicy() ) )
        {
            policy = policy.setUpdatePolicy( session.getUpdatePolicy() );
        }

        return policy;
    }

    private RepositoryPolicy merge( RepositorySystemSession session, RepositoryPolicy policy1, RepositoryPolicy policy2 )
    {
        RepositoryPolicy policy;

        if ( policy1.isEnabled() && policy2.isEnabled() )
        {
            String checksums;
            if ( ordinalOfChecksumPolicy( policy2.getChecksumPolicy() ) < ordinalOfChecksumPolicy( policy1.getChecksumPolicy() ) )
            {
                checksums = policy2.getChecksumPolicy();
            }
            else
            {
                checksums = policy1.getChecksumPolicy();
            }

            String updates =
                updateCheckManager.getEffectiveUpdatePolicy( session, policy1.getChecksumPolicy(),
                                                             policy2.getChecksumPolicy() );

            policy = new RepositoryPolicy( true, updates, checksums );
        }
        else if ( policy2.isEnabled() )
        {
            policy = policy2;
        }
        else
        {
            policy = policy1;
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

    public RepositoryConnector getRepositoryConnector( RepositorySystemSession session, RemoteRepository repository )
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
