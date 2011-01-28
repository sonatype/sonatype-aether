package org.sonatype.aether.impl.internal;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *******************************************************************************/

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.impl.RemoteRepositoryManager;
import org.sonatype.aether.impl.UpdateCheck;
import org.sonatype.aether.impl.UpdateCheckManager;
import org.sonatype.aether.metadata.Metadata;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.repository.RepositoryPolicy;
import org.sonatype.aether.test.impl.SysoutLogger;
import org.sonatype.aether.test.impl.TestRepositorySystemSession;
import org.sonatype.aether.transfer.ArtifactTransferException;
import org.sonatype.aether.transfer.MetadataTransferException;

/**
 * 
 */
public class DefaultRemoteRepositoryManagerTest
{

    private TestRepositorySystemSession session;

    private RemoteRepositoryManager manager;

    @Before
    public void setup()
        throws Exception
    {
        session = new TestRepositorySystemSession();
        session.setChecksumPolicy( null );
        session.setUpdatePolicy( null );
        manager = new DefaultRemoteRepositoryManager( new SysoutLogger(), new StubUpdateCheckManager(), null );
    }

    public void teardown()
        throws Exception
    {
        manager = null;
        session = null;
    }

    @Test
    public void testGetPolicy()
    {
        RepositoryPolicy snapshotPolicy =
            new RepositoryPolicy( true, RepositoryPolicy.UPDATE_POLICY_ALWAYS, RepositoryPolicy.CHECKSUM_POLICY_IGNORE );
        RepositoryPolicy releasePolicy =
            new RepositoryPolicy( true, RepositoryPolicy.UPDATE_POLICY_NEVER, RepositoryPolicy.CHECKSUM_POLICY_FAIL );

        RemoteRepository repo = new RemoteRepository( "id", "type", "http://localhost" );
        repo.setPolicy( true, snapshotPolicy );
        repo.setPolicy( false, releasePolicy );

        RepositoryPolicy effectivePolicy = manager.getPolicy( session, repo, true, true );
        assertEquals( true, effectivePolicy.isEnabled() );
        assertEquals( RepositoryPolicy.CHECKSUM_POLICY_IGNORE, effectivePolicy.getChecksumPolicy() );
        assertEquals( RepositoryPolicy.UPDATE_POLICY_ALWAYS, effectivePolicy.getUpdatePolicy() );
    }

    private static class StubUpdateCheckManager
        implements UpdateCheckManager
    {

        public String getEffectiveUpdatePolicy( RepositorySystemSession session, String policy1, String policy2 )
        {
            return ordinalOfUpdatePolicy( policy1 ) < ordinalOfUpdatePolicy( policy2 ) ? policy1 : policy2;
        }

        private int ordinalOfUpdatePolicy( String policy )
        {
            if ( RepositoryPolicy.UPDATE_POLICY_DAILY.equals( policy ) )
            {
                return 1440;
            }
            else if ( RepositoryPolicy.UPDATE_POLICY_ALWAYS.equals( policy ) )
            {
                return 0;
            }
            else if ( policy != null && policy.startsWith( RepositoryPolicy.UPDATE_POLICY_INTERVAL ) )
            {
                String s = policy.substring( RepositoryPolicy.UPDATE_POLICY_INTERVAL.length() + 1 );
                return Integer.valueOf( s );
            }
            else
            {
                // assume "never"
                return Integer.MAX_VALUE;
            }
        }

        public boolean isUpdatedRequired( RepositorySystemSession session, long lastModified, String policy )
        {
            return false;
        }

        public void checkArtifact( RepositorySystemSession session,
                                   UpdateCheck<Artifact, ArtifactTransferException> check )
        {
        }

        public void touchArtifact( RepositorySystemSession session,
                                   UpdateCheck<Artifact, ArtifactTransferException> check )
        {
        }

        public void checkMetadata( RepositorySystemSession session,
                                   UpdateCheck<Metadata, MetadataTransferException> check )
        {
        }

        public void touchMetadata( RepositorySystemSession session,
                                   UpdateCheck<Metadata, MetadataTransferException> check )
        {
        }

    }

}
