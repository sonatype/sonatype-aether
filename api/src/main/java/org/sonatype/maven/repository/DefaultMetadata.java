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
 * @author Benjamin Bentmann
 */
public class DefaultMetadata
    implements Metadata
{

    private String groupId = "";

    private String artifactId = "";

    private String version = "";

    // e.g. "maven-metadata.xml", "archetype-catalog.xml" or "nexus-maven-repository-index.properties", i.e the simple
    // file name used by classical URL-based repos
    private String type = "";

    private Nature nature = Nature.RELEASE;

    private File file;

    public DefaultMetadata()
    {
        // enables default constructor
    }

    public DefaultMetadata( Metadata original )
    {
        setGroupId( original.getGroupId() );
        setArtifactId( original.getArtifactId() );
        setVersion( original.getVersion() );
        setType( original.getType() );
        setNature( original.getNature() );
        setFile( original.getFile() );
    }

    public String getGroupId()
    {
        return groupId;
    }

    public DefaultMetadata setGroupId( String groupId )
    {
        this.groupId = ( groupId != null ) ? groupId : "";
        return this;
    }

    public String getArtifactId()
    {
        return artifactId;
    }

    public DefaultMetadata setArtifactId( String artifactId )
    {
        this.artifactId = ( artifactId != null ) ? artifactId : "";
        return this;
    }

    public String getVersion()
    {
        return version;
    }

    public DefaultMetadata setVersion( String version )
    {
        this.version = ( version != null ) ? version : "";
        return this;
    }

    public String getType()
    {
        return type;
    }

    public DefaultMetadata setType( String type )
    {
        this.type = ( type != null ) ? type : "";
        return this;
    }

    public Nature getNature()
    {
        return nature;
    }

    public DefaultMetadata setNature( Nature nature )
    {
        if ( nature == null )
        {
            throw new IllegalArgumentException( "metadata nature was not specified" );
        }
        this.nature = nature;
        return this;
    }

    public File getFile()
    {
        return file;
    }

    public void setFile( File file )
    {
        this.file = file;
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
