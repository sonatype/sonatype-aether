package org.sonatype.aether.test.util;

import java.io.IOException;

import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.resolution.ArtifactDescriptorException;
import org.sonatype.aether.resolution.ArtifactDescriptorRequest;
import org.sonatype.aether.resolution.ArtifactDescriptorResult;

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

/**
 * Uses a {@link IniArtifactDataReader} to parse an artifact description.
 * <p>
 * Note: May not directly implement ArtifactDescriptorReader from aether-impl because of circular dependencies, and has
 * to be wrapped for use in test classes.
 * 
 * @author Benjamin Hanzelmann
 */
public class IniArtifactDescriptorReader
{
    private IniArtifactDataReader reader;

    /**
     * Use the given prefix to load the artifact descriptions.
     */
    public IniArtifactDescriptorReader( String prefix )
    {
        reader = new IniArtifactDataReader( prefix );
    }

    /**
     * Parses the resource <code>$prefix/gid_aid_ext_ver.ini</code> from the request artifact as an artifact description
     * and wraps it into an ArtifactDescriptorResult.
     */
    public ArtifactDescriptorResult readArtifactDescriptor( RepositorySystemSession session,
                                                            ArtifactDescriptorRequest request )
        throws ArtifactDescriptorException
    {
        Artifact artifact = request.getArtifact();
        String resourceName =
            String.format( "%s_%s_%s_%s.ini", artifact.getGroupId(), artifact.getArtifactId(), artifact.getExtension(),
                           artifact.getVersion() );

        ArtifactDescriptorResult result = new ArtifactDescriptorResult( request );
        result.setArtifact( artifact );

        try
        {
            ArtifactDescription data = reader.parse( resourceName );
            result.setDependencies( data.getDependencies() );
            result.setManagedDependencies( data.getManagedDependencies() );
            result.setRepositories( data.getRepositories() );
            result.setRelocations( data.getRelocations() );
            return result;
        }
        catch ( IOException e )
        {
            throw new ArtifactDescriptorException( result, e.getMessage() );
        }

    }
}
