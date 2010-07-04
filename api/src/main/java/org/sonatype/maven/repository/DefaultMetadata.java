package org.sonatype.maven.repository;

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

import java.io.File;

/**
 * A basic metadata instance.
 * 
 * @author Benjamin Bentmann
 */
public final class DefaultMetadata
    implements Metadata
{

    private final String groupId;

    private final String artifactId;

    private final String version;

    private final String type;

    private final Nature nature;

    private final File file;

    public DefaultMetadata( String type, Nature nature )
    {
        groupId = artifactId = version = "";
        this.type = ( type != null ) ? type : "";
        if ( nature == null )
        {
            throw new IllegalArgumentException( "metadata nature was not specified" );
        }
        this.nature = nature;
        this.file = null;
    }

    public DefaultMetadata( String groupId, String type, Nature nature )
    {
        this.groupId = ( groupId != null ) ? groupId : "";
        artifactId = version = "";
        this.type = ( type != null ) ? type : "";
        if ( nature == null )
        {
            throw new IllegalArgumentException( "metadata nature was not specified" );
        }
        this.nature = nature;
        this.file = null;
    }

    public DefaultMetadata( String groupId, String artifactId, String type, Nature nature )
    {
        this.groupId = ( groupId != null ) ? groupId : "";
        this.artifactId = ( artifactId != null ) ? artifactId : "";
        version = "";
        this.type = ( type != null ) ? type : "";
        if ( nature == null )
        {
            throw new IllegalArgumentException( "metadata nature was not specified" );
        }
        this.nature = nature;
        this.file = null;
    }

    public DefaultMetadata( String groupId, String artifactId, String version, String type, Nature nature )
    {
        this.groupId = ( groupId != null ) ? groupId : "";
        this.artifactId = ( artifactId != null ) ? artifactId : "";
        this.version = ( version != null ) ? version : "";
        this.type = ( type != null ) ? type : "";
        if ( nature == null )
        {
            throw new IllegalArgumentException( "metadata nature was not specified" );
        }
        this.nature = nature;
        this.file = null;
    }

    public DefaultMetadata( String groupId, String artifactId, String version, String type, Nature nature, File file )
    {
        this.groupId = ( groupId != null ) ? groupId : "";
        this.artifactId = ( artifactId != null ) ? artifactId : "";
        this.version = ( version != null ) ? version : "";
        this.type = ( type != null ) ? type : "";
        if ( nature == null )
        {
            throw new IllegalArgumentException( "metadata nature was not specified" );
        }
        this.nature = nature;
        this.file = file;
    }

    public String getGroupId()
    {
        return groupId;
    }

    public String getArtifactId()
    {
        return artifactId;
    }

    public String getVersion()
    {
        return version;
    }

    public String getType()
    {
        return type;
    }

    public Nature getNature()
    {
        return nature;
    }

    public File getFile()
    {
        return file;
    }

    public Metadata setFile( File file )
    {
        return new DefaultMetadata( groupId, artifactId, version, type, nature, file );
    }

    @Override
    public String toString()
    {
        StringBuilder buffer = new StringBuilder( 128 );
        if ( getGroupId().length() > 0 )
        {
            buffer.append( getGroupId() );
        }
        if ( getArtifactId().length() > 0 )
        {
            buffer.append( ':' ).append( getArtifactId() );
        }
        if ( getVersion().length() > 0 )
        {
            buffer.append( ':' ).append( getVersion() );
        }
        buffer.append( '/' ).append( getType() );
        return buffer.toString();
    }

}
