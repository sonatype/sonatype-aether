package org.sonatype.aether.impl;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import static org.junit.Assert.*;

import org.junit.Test;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.impl.internal.DefaultServiceLocator;

/**
 * @author Benjamin Bentmann
 */
public class DefaultServiceLocatorTest
{

    @Test
    public void testGetRepositorySystem()
    {
        DefaultServiceLocator locator = new DefaultServiceLocator();
        locator.addService( ArtifactDescriptorReader.class, StubArtifactDescriptorReader.class );
        locator.addService( VersionResolver.class, StubVersionResolver.class );
        locator.addService( VersionRangeResolver.class, StubVersionRangeResolver.class );

        RepositorySystem repoSys = locator.getService( RepositorySystem.class );
        assertNotNull( repoSys );
    }

}
