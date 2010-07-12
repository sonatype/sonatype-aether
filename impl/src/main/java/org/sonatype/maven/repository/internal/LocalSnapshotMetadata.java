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

import java.io.File;

import org.sonatype.maven.repository.Artifact;
import org.sonatype.maven.repository.internal.metadata.Metadata;
import org.sonatype.maven.repository.internal.metadata.Snapshot;
import org.sonatype.maven.repository.internal.metadata.Versioning;

/**
 * @author Benjamin Bentmann
 */
final class LocalSnapshotMetadata
    extends MavenMetadata
{

    private final Artifact artifact;

    public LocalSnapshotMetadata( Artifact artifact )
    {
        super( createMetadata( artifact ), null );
        this.artifact = artifact;
    }

    public LocalSnapshotMetadata( Artifact artifact, File file )
    {
        super( createMetadata( artifact ), file );
        this.artifact = artifact;
    }

    private static Metadata createMetadata( Artifact artifact )
    {
        Snapshot snapshot = new Snapshot();
        snapshot.setLocalCopy( true );
        Versioning versioning = new Versioning();
        versioning.setSnapshot( snapshot );

        Metadata metadata = new Metadata();
        metadata.setModelVersion( "1.0.0" );
        metadata.setVersioning( versioning );
        metadata.setGroupId( artifact.getGroupId() );
        metadata.setArtifactId( artifact.getArtifactId() );
        metadata.setVersion( artifact.getBaseVersion() );

        return metadata;
    }

    public MavenMetadata setFile( File file )
    {
        return new LocalSnapshotMetadata( artifact, file );
    }

    public String getGroupId()
    {
        return artifact.getGroupId();
    }

    public String getArtifactId()
    {
        return artifact.getArtifactId();
    }

    public String getVersion()
    {
        return artifact.getBaseVersion();
    }

    public Nature getNature()
    {
        return Nature.SNAPSHOT;
    }

}
