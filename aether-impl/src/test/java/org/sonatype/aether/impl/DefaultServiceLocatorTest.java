package org.sonatype.aether.impl;

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

import org.junit.Test;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.impl.ArtifactDescriptorReader;
import org.sonatype.aether.impl.internal.DefaultServiceLocator;
import org.sonatype.aether.resolution.ArtifactDescriptorException;
import org.sonatype.aether.resolution.ArtifactDescriptorRequest;
import org.sonatype.aether.resolution.ArtifactDescriptorResult;
import org.sonatype.aether.resolution.VersionRangeRequest;
import org.sonatype.aether.resolution.VersionRangeResolutionException;
import org.sonatype.aether.resolution.VersionRangeResult;
import org.sonatype.aether.resolution.VersionRequest;
import org.sonatype.aether.resolution.VersionResolutionException;
import org.sonatype.aether.resolution.VersionResult;

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

    public static class StubArtifactDescriptorReader
        implements ArtifactDescriptorReader
    {

        public ArtifactDescriptorResult readArtifactDescriptor( RepositorySystemSession session,
                                                                ArtifactDescriptorRequest request )
            throws ArtifactDescriptorException
        {
            return null;
        }

    }

    public static class StubVersionResolver
        implements VersionResolver
    {

        public VersionResult resolveVersion( RepositorySystemSession session, VersionRequest request )
            throws VersionResolutionException
        {
            return null;
        }

    }

    public static class StubVersionRangeResolver
        implements VersionRangeResolver
    {

        public VersionRangeResult resolveVersionRange( RepositorySystemSession session, VersionRangeRequest request )
            throws VersionRangeResolutionException
        {
            return null;
        }

    }

}
