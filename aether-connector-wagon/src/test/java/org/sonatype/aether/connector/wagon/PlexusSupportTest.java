package org.sonatype.aether.connector.wagon;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import org.codehaus.plexus.PlexusTestCase;
import org.sonatype.aether.spi.connector.RepositoryConnectorFactory;
import org.sonatype.aether.spi.io.FileProcessor;
import org.sonatype.aether.spi.log.Logger;
import org.sonatype.aether.test.impl.SysoutLogger;
import org.sonatype.aether.test.impl.TestFileProcessor;

/**
 */
public class PlexusSupportTest
    extends PlexusTestCase
{

    public void testExistenceOfPlexusComponentMetadata()
        throws Exception
    {
        getContainer().addComponent( new SysoutLogger(), Logger.class, null );
        getContainer().addComponent( new TestFileProcessor(), FileProcessor.class, null );

        RepositoryConnectorFactory factory = lookup( RepositoryConnectorFactory.class, "wagon" );
        assertNotNull( factory );
        assertEquals( WagonRepositoryConnectorFactory.class, factory.getClass() );
    }

}
