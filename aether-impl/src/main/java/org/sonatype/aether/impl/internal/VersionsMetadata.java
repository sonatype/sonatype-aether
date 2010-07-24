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

import java.io.File;

import org.sonatype.aether.Artifact;
import org.sonatype.aether.impl.metadata.Metadata;
import org.sonatype.aether.impl.metadata.Versioning;

/**
 * @author Benjamin Bentmann
 */
final class VersionsMetadata
    extends MavenMetadata
{

    private final Artifact artifact;

    public VersionsMetadata( Artifact artifact )
    {
        super( createMetadata( artifact ), null );
        this.artifact = artifact;
    }

    public VersionsMetadata( Artifact artifact, File file )
    {
        super( createMetadata( artifact ), file );
        this.artifact = artifact;
    }

    private static Metadata createMetadata( Artifact artifact )
    {
        Versioning versioning = new Versioning();
        versioning.addVersion( artifact.getBaseVersion() );
        if ( !artifact.isSnapshot() )
        {
            versioning.setRelease( artifact.getVersion() );
        }

        Metadata metadata = new Metadata();
        metadata.setModelVersion( "1.0.0" );
        metadata.setVersioning( versioning );
        metadata.setGroupId( artifact.getGroupId() );
        metadata.setArtifactId( artifact.getArtifactId() );

        return metadata;
    }

    public Object getKey()
    {
        return getGroupId() + ':' + getArtifactId();
    }

    public static Object getKey( Artifact artifact )
    {
        return artifact.getGroupId() + ':' + artifact.getArtifactId();
    }

    public MavenMetadata setFile( File file )
    {
        return new VersionsMetadata( artifact, file );
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
        return "";
    }

    public Nature getNature()
    {
        return artifact.isSnapshot() ? Nature.RELEASE_OR_SNAPSHOT : Nature.RELEASE;
    }

}
