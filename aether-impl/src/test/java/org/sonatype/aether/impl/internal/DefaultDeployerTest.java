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

import org.junit.Before;
import org.junit.Test;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.deployment.DeployRequest;
import org.sonatype.aether.deployment.DeploymentException;
import org.sonatype.aether.impl.UpdateCheckManager;
import org.sonatype.aether.metadata.Metadata;
import org.sonatype.aether.metadata.Metadata.Nature;
import org.sonatype.aether.spi.log.NullLogger;
import org.sonatype.aether.test.impl.TestRepositorySystemSession;
import org.sonatype.aether.test.util.FileUtil;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.util.metadata.DefaultMetadata;

public class DefaultDeployerTest
{

    private Artifact artifact;

    private DefaultMetadata metadata;

    private TestRepositorySystemSession session;

    private StubRemoteRepositoryManager manager;

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
        manager = new StubRemoteRepositoryManager();
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

    @Test( expected = DeploymentException.class )
    public void testNullArtifactFile()
        throws DeploymentException
    {
        RecordingRepositoryConnector connector = new RecordingRepositoryConnector();
        manager.setConnector( connector );

        DefaultDeployer deployer = new DefaultDeployer();
        deployer.setRemoteRepositoryManager( manager );
        deployer.setUpdateCheckManager( new DoNothingUpdateCheckManager() );
        deployer.setLogger( new NullLogger() );

        DeployRequest request = new DeployRequest();
        request.addArtifact( artifact.setFile( null ) );
        deployer.deploy( session, request );
    }

    @Test( expected = DeploymentException.class )
    public void testNullMetadataFile()
        throws DeploymentException
    {
        RecordingRepositoryConnector connector = new RecordingRepositoryConnector();
        manager.setConnector( connector );

        DefaultDeployer deployer = new DefaultDeployer();
        deployer.setRemoteRepositoryManager( manager );
        deployer.setUpdateCheckManager( new DoNothingUpdateCheckManager() );
        deployer.setLogger( new NullLogger() );

        DeployRequest request = new DeployRequest();
        request.addArtifact( artifact.setFile( null ) );
        deployer.deploy( session, request );
    }
}
