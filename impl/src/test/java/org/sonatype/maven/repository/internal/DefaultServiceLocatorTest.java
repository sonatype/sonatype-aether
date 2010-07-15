package org.sonatype.maven.repository.internal;

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
import org.sonatype.maven.repository.ArtifactDescriptorException;
import org.sonatype.maven.repository.ArtifactDescriptorRequest;
import org.sonatype.maven.repository.ArtifactDescriptorResult;
import org.sonatype.maven.repository.RepositorySystem;
import org.sonatype.maven.repository.RepositorySystemSession;
import org.sonatype.maven.repository.impl.DefaultServiceLocator;
import org.sonatype.maven.repository.spi.ArtifactDescriptorReader;

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

}
