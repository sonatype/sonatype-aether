package org.sonatype.aether.connector.wagon;

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

import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.providers.file.FileWagon;
import org.junit.Test;
import org.sonatype.aether.test.util.connector.TransferEventTester;
import org.sonatype.aether.transfer.NoRepositoryConnectorException;

public class EventTest
{

    @Test
    public void testFileUrlEvents()
        throws NoRepositoryConnectorException, IOException
    {
        WagonRepositoryConnectorFactory factory = new WagonRepositoryConnectorFactory();
        factory.setWagonProvider( new WagonProvider()
        {

            public void release( Wagon wagon )
            {
            }

            public Wagon lookup( String roleHint )
                throws Exception
            {
                return new FileWagon();
            }
        } );

        TransferEventTester.testTransferEvents( factory );
    }
}
