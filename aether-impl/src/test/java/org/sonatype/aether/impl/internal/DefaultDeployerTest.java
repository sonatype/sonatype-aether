package org.sonatype.aether.impl.internal;

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

import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.sonatype.aether.Artifact;
import org.sonatype.aether.ArtifactTransferException;
import org.sonatype.aether.DefaultArtifact;
import org.sonatype.aether.DefaultMetadata;
import org.sonatype.aether.DeployRequest;
import org.sonatype.aether.DeploymentException;
import org.sonatype.aether.Metadata;
import org.sonatype.aether.MetadataTransferException;
import org.sonatype.aether.Metadata.Nature;
import org.sonatype.aether.NoRepositoryConnectorException;
import org.sonatype.aether.RemoteRepository;
import org.sonatype.aether.RepositoryPolicy;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.impl.RemoteRepositoryManager;
import org.sonatype.aether.impl.UpdateCheck;
import org.sonatype.aether.impl.UpdateCheckManager;
import org.sonatype.aether.spi.connector.RepositoryConnector;
import org.sonatype.aether.spi.log.NullLogger;
import org.sonatype.aether.test.impl.TestRepositorySystemSession;
import org.sonatype.aether.test.util.FileUtil;

public class DefaultDeployerTest
{

    private Artifact artifact;

    private DefaultMetadata metadata;

    private TestRepositorySystemSession session;

    private TestRemoteRepositoryManager manager;

    @Before
    public void setup()
        throws IOException
    {
        artifact = new DefaultArtifact( "gid", "aid", "jar", "ver" );
        artifact = artifact.setFile( FileUtil.createTempFile( "artifact" ) );
        metadata =
            new DefaultMetadata( "gid", "aid", "ver", "type", Nature.RELEASE_OR_SNAPSHOT,
                                 FileUtil.createTempFile( "metadata" ) );

        session = new TestRepositorySystemSession();
        manager = new TestRemoteRepositoryManager();
    }

    @Test
    public void testSuccessfulDeploy()
        throws DeploymentException
    {
        Artifact[] expectPut = new Artifact[] { artifact };
        Metadata[] expectPutMD = new Metadata[] { metadata };
        RecordingRepositoryConnector connector = new RecordingRepositoryConnector( null, expectPut, null, expectPutMD );
        manager.setConnector( connector );

        DefaultDeployer deployer = new DefaultDeployer();
        deployer.setRemoteRepositoryManager( manager );
        UpdateCheckManager updateCheckManager = new DoNothingUpdateCheckManager();
        deployer.setUpdateCheckManager( updateCheckManager );
        deployer.setLogger( new NullLogger() );

        DeployRequest request = new DeployRequest();
        request.addArtifact( artifact );
        request.addMetadata( metadata );
        deployer.deploy( session, request );

        connector.assertSeenExpected();
    }
    
    @Test(expected=DeploymentException.class)
    public void testNullArtifactFile() throws DeploymentException
    {
        RecordingRepositoryConnector connector = new RecordingRepositoryConnector( null, null, null, null);
        manager.setConnector( connector );
        
        DefaultDeployer deployer = new DefaultDeployer();
        deployer.setRemoteRepositoryManager( manager );
        deployer.setUpdateCheckManager( new DoNothingUpdateCheckManager() );
        deployer.setLogger( new NullLogger() );
        
        DeployRequest request = new DeployRequest();
        request.addArtifact( artifact.setFile( null ) );
        deployer.deploy( session, request );
    }
    
    @Test(expected=DeploymentException.class)
    public void testNullMetadataFile() throws DeploymentException
    {
        RecordingRepositoryConnector connector = new RecordingRepositoryConnector( null, null, null, null);
        manager.setConnector( connector );
        
        DefaultDeployer deployer = new DefaultDeployer();
        deployer.setRemoteRepositoryManager( manager );
        deployer.setUpdateCheckManager( new DoNothingUpdateCheckManager() );
        deployer.setLogger( new NullLogger() );
        
        DeployRequest request = new DeployRequest();
        request.addArtifact( artifact.setFile( null ) );
        deployer.deploy( session, request );
    }

    private final class DoNothingUpdateCheckManager
        implements UpdateCheckManager
    {
        public void touchMetadata( RepositorySystemSession session,
                                   UpdateCheck<Metadata, MetadataTransferException> check )
        {
        }

        public void touchArtifact( RepositorySystemSession session,
                                   UpdateCheck<Artifact, ArtifactTransferException> check )
        {
        }

        public String getEffectiveUpdatePolicy( RepositorySystemSession session, String policy1, String policy2 )
        {
            return null;
        }

        public void checkMetadata( RepositorySystemSession session,
                                   UpdateCheck<Metadata, MetadataTransferException> check )
        {
        }

        public void checkArtifact( RepositorySystemSession session,
                                   UpdateCheck<Artifact, ArtifactTransferException> check )
        {
        }
    }

    private static class TestRemoteRepositoryManager
        implements RemoteRepositoryManager
    {

        private RepositoryConnector connector;

        public RepositoryConnector getConnector()
        {
            return connector;
        }

        public void setConnector( RepositoryConnector connector )
        {
            this.connector = connector;
        }

        public List<RemoteRepository> aggregateRepositories( RepositorySystemSession session,
                                                             List<RemoteRepository> dominantRepositories,
                                                             List<RemoteRepository> recessiveRepositories,
                                                             boolean recessiveIsRaw )
        {
            throw new UnsupportedOperationException( "aggregateRepositories" );
        }

        public RepositoryPolicy getPolicy( RepositorySystemSession session, RemoteRepository repository,
                                           boolean releases, boolean snapshots )
        {
            return new RepositoryPolicy( true, RepositoryPolicy.UPDATE_POLICY_ALWAYS,
                                         RepositoryPolicy.CHECKSUM_POLICY_FAIL );
        }

        public RepositoryConnector getRepositoryConnector( RepositorySystemSession session, RemoteRepository repository )
            throws NoRepositoryConnectorException
        {
            return connector;
        }

    }
}
