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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.sonatype.aether.Artifact;
import org.sonatype.aether.DeployRequest;
import org.sonatype.aether.InstallRequest;
import org.sonatype.aether.Metadata;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.impl.MetadataGenerator;

/**
 * @author Benjamin Bentmann
 */
class VersionsMetadataGenerator
    implements MetadataGenerator
{

    private Map<Object, VersionsMetadata> versions;

    public VersionsMetadataGenerator( RepositorySystemSession session, InstallRequest request )
    {
        this( session, request.getMetadata() );
    }

    public VersionsMetadataGenerator( RepositorySystemSession session, DeployRequest request )
    {
        this( session, request.getMetadata() );
    }

    private VersionsMetadataGenerator( RepositorySystemSession session, Collection<? extends Metadata> metadatas )
    {
        versions = new LinkedHashMap<Object, VersionsMetadata>();

        /*
         * NOTE: This should be considered a quirk to support interop with Maven's legacy ArtifactDeployer which
         * processes one artifact at a time and hence cannot associate the artifacts from the same project to use the
         * same timestamp+buildno for the snapshot versions. Allowing the caller to pass in metadata from a previous
         * deployment allows to re-establish the association between the artifacts of the same project.
         */
        for ( Metadata metadata : metadatas )
        {
            if ( metadata instanceof VersionsMetadata )
            {
                VersionsMetadata versionsMetadata = (VersionsMetadata) metadata;
                versions.put( versionsMetadata.getKey(), versionsMetadata );
            }
        }
    }

    public Collection<? extends Metadata> prepare( Collection<? extends Artifact> artifacts )
    {
        return Collections.emptyList();
    }

    public Artifact transformArtifact( Artifact artifact )
    {
        return artifact;
    }

    public Collection<? extends Metadata> finish( Collection<? extends Artifact> artifacts )
    {
        for ( Artifact artifact : artifacts )
        {
            Object key = VersionsMetadata.getKey( artifact );
            VersionsMetadata versionsMetadata = versions.get( key );
            if ( versionsMetadata == null )
            {
                versionsMetadata = new VersionsMetadata( artifact );
                versions.put( key, versionsMetadata );
            }
        }

        return versions.values();
    }

}
