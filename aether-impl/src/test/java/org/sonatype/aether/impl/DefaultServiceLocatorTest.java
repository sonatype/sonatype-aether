package org.sonatype.aether.impl;

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

import org.junit.Test;
import org.sonatype.aether.ArtifactDescriptorException;
import org.sonatype.aether.ArtifactDescriptorRequest;
import org.sonatype.aether.ArtifactDescriptorResult;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.VersionRangeRequest;
import org.sonatype.aether.VersionRangeResolutionException;
import org.sonatype.aether.VersionRangeResult;
import org.sonatype.aether.VersionRequest;
import org.sonatype.aether.VersionResolutionException;
import org.sonatype.aether.VersionResult;
import org.sonatype.aether.impl.ArtifactDescriptorReader;
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
