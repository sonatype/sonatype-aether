package org.sonatype.aether.connector.wagon;


import java.io.IOException;

import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.providers.file.FileWagon;
import org.junit.Test;
import org.sonatype.aether.NoRepositoryConnectorException;
import org.sonatype.aether.test.util.connector.TransferEventTester;

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
