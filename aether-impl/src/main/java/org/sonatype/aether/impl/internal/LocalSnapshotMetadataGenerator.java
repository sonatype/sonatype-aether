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
import org.sonatype.aether.InstallRequest;
import org.sonatype.aether.Metadata;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.impl.MetadataGenerator;

/**
 * @author Benjamin Bentmann
 */
class LocalSnapshotMetadataGenerator
    implements MetadataGenerator
{

    private Map<Object, LocalSnapshotMetadata> snapshots;

    public LocalSnapshotMetadataGenerator( RepositorySystemSession session, InstallRequest request )
    {
        snapshots = new LinkedHashMap<Object, LocalSnapshotMetadata>();
    }

    public Collection<? extends Metadata> prepare( Collection<? extends Artifact> artifacts )
    {
        for ( Artifact artifact : artifacts )
        {
            if ( artifact.isSnapshot() )
            {
                Object key = LocalSnapshotMetadata.getKey( artifact );
                LocalSnapshotMetadata snapshotMetadata = snapshots.get( key );
                if ( snapshotMetadata == null )
                {
                    snapshotMetadata = new LocalSnapshotMetadata( artifact );
                    snapshots.put( key, snapshotMetadata );
                }
            }
        }

        return Collections.emptyList();
    }

    public Artifact transformArtifact( Artifact artifact )
    {
        return artifact;
    }

    public Collection<? extends Metadata> finish( Collection<? extends Artifact> artifacts )
    {
        return snapshots.values();
    }

}
