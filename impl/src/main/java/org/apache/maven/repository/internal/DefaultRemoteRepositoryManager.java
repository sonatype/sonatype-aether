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
import java.util.Collections;
import java.util.List;

import org.apache.maven.repository.NoRepositoryReaderException;
import org.apache.maven.repository.RemoteRepository;
import org.apache.maven.repository.RepositoryContext;
import org.apache.maven.repository.RepositoryPolicy;
import org.apache.maven.repository.spi.Logger;
import org.apache.maven.repository.spi.NullLogger;
import org.apache.maven.repository.spi.PluggableComponent;
import org.apache.maven.repository.spi.RemoteRepositoryManager;
import org.apache.maven.repository.spi.RepositoryReader;
import org.apache.maven.repository.spi.RepositoryReaderFactory;
import org.apache.maven.repository.spi.UpdateCheckManager;
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

    @Requirement( role = RepositoryReaderFactory.class )
    private List<RepositoryReaderFactory> readerFactories = new ArrayList<RepositoryReaderFactory>();

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

    public DefaultRemoteRepositoryManager addRepositoryReaderFactory( RepositoryReaderFactory factory )
    {
        if ( factory == null )
        {
            throw new IllegalArgumentException( "repository reader factory has not been specified" );
        }
        readerFactories.add( factory );
        return this;
    }

    public RepositoryPolicy getPolicy( RepositoryContext context, RemoteRepository repository, boolean releases,
                                       boolean snapshots )
    {
        RepositoryPolicy policy;

        // get effective per-repository policy
        if ( releases && snapshots )
        {
            RepositoryPolicy policy1 = repository.getPolicy( false );
            RepositoryPolicy policy2 = repository.getPolicy( true );
            policy = new RepositoryPolicy( policy1 );
            if ( policy2.isEnabled() )
            {
                policy.setEnabled( true );

                if ( ordinalOfChecksumPolicy( policy2.getChecksumPolicy() ) < ordinalOfChecksumPolicy( policy.getChecksumPolicy() ) )
                {
                    policy.setChecksumPolicy( policy2.getChecksumPolicy() );
                }

                policy.setChecksumPolicy( updateCheckManager.getEffectiveUpdatePolicy( context,
                                                                                       policy.getChecksumPolicy(),
                                                                                       policy2.getChecksumPolicy() ) );
            }
        }
        else
        {
            policy = new RepositoryPolicy( repository.getPolicy( snapshots ) );
        }

        // superimpose global policy
        if ( StringUtils.isNotEmpty( context.getChecksumPolicy() ) )
        {
            policy.setChecksumPolicy( context.getChecksumPolicy() );
        }
        if ( StringUtils.isNotEmpty( context.getUpdatePolicy() ) )
        {
            policy.setUpdatePolicy( context.getUpdatePolicy() );
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

    public RepositoryReader getRepositoryReader( RepositoryContext context, RemoteRepository repository )
        throws NoRepositoryReaderException
    {
        List<RepositoryReaderFactory> factories = new ArrayList<RepositoryReaderFactory>( readerFactories );
        Collections.sort( factories, PluggableComponent.COMPARATOR );

        for ( RepositoryReaderFactory factory : factories )
        {
            try
            {
                return factory.newInstance( context, repository );
            }
            catch ( NoRepositoryReaderException e )
            {
                // continue and try next factory
            }
        }

        throw new NoRepositoryReaderException( repository );
    }

}
