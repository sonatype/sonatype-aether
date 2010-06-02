package org.apache.maven.repo.internal;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.Snapshot;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.apache.maven.repo.Artifact;

/**
 * @author Benjamin Bentmann
 */
class RemoteSnapshotMetadata
    extends MavenMetadata
{

    private final Artifact artifact;

    public RemoteSnapshotMetadata( Artifact artifact )
    {
        this.artifact = artifact;

        DateFormat utcDateFormatter = new SimpleDateFormat( "yyyyMMdd.HHmmss" );
        utcDateFormatter.setTimeZone( TimeZone.getTimeZone( "UTC" ) );

        Snapshot snapshot = new Snapshot();
        snapshot.setBuildNumber( 1 );
        snapshot.setTimestamp( utcDateFormatter.format( new Date() ) );

        Versioning versioning = new Versioning();
        versioning.setSnapshot( snapshot );

        Metadata metadata = new Metadata();
        metadata.setVersioning( versioning );
        metadata.setGroupId( artifact.getGroupId() );
        metadata.setArtifactId( artifact.getArtifactId() );
        metadata.setVersion( artifact.getBaseVersion() );

        this.metadata = metadata;
    }

    public String getExpandedVersion()
    {
        String version = metadata.getVersion();

        Snapshot snapshot = metadata.getVersioning().getSnapshot();
        String qualifier = snapshot.getTimestamp() + "-" + snapshot.getBuildNumber();
        version = version.substring( 0, version.length() - "SNAPSHOT".length() ) + qualifier;

        return version;
    }

    @Override
    protected void merge( Metadata recessive )
    {
        Versioning versioning = recessive.getVersioning();
        if ( versioning != null )
        {
            Snapshot snapshot = versioning.getSnapshot();
            if ( snapshot != null )
            {
                metadata.getVersioning().getSnapshot().setBuildNumber( snapshot.getBuildNumber() + 1 );
                versioning.setSnapshot( null );
            }
        }

        super.merge( recessive );
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
