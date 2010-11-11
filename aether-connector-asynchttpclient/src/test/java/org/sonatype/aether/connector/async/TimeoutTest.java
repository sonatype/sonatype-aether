package org.sonatype.aether.connector.async;

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

import static org.junit.Assert.*;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.RepositoryPolicy;
import org.sonatype.aether.spi.connector.ArtifactDownload;
import org.sonatype.aether.test.util.TestFileUtils;
import org.sonatype.tests.jetty.runner.ConfigurationRunner;
import org.sonatype.tests.jetty.server.behaviour.Pause;
import org.sonatype.tests.server.api.ServerProvider;

/**
 * @author Benjamin Hanzelmann
 *
 */
@RunWith( ConfigurationRunner.class )
public class TimeoutTest
    extends AsyncConnectorSuiteConfiguration
{

    @Override
    public void configureProvider( ServerProvider provider )
    {
        provider.addBehaviour( "/repo/*", new Pause( 100000 ) );
    }

    @Test( timeout = 50000 )
    public void testTimeout()
        throws Exception
    {
        File f = TestFileUtils.createTempFile( "" );
        Artifact a = artifact( "foo" );

        ArtifactDownload down = new ArtifactDownload( a, null, f, RepositoryPolicy.CHECKSUM_POLICY_FAIL );
        Collection<? extends ArtifactDownload> downs = Arrays.asList( down );
        connector().get( downs, null );

        assertNotNull( down.getException() );
    }

}
