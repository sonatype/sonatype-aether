package org.apache.maven.repository;

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
public class DefaultArtifact
    implements Artifact
{

    private String groupId;

    private String artifactId;

    private String version;

    private String baseVersion;

    private String classifier;

    private String type;

    private File file;

    public DefaultArtifact()
    {
        // enables default constructor
    }

    public DefaultArtifact( String groupId, String artifactId, String classifier, String type, String version )
    {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.classifier = ( classifier != null ) ? classifier : "";
        this.type = type;
        this.version = version;
        this.baseVersion = version;
    }

    public DefaultArtifact( Artifact artifact )
    {
        this.groupId = artifact.getGroupId();
        this.artifactId = artifact.getArtifactId();
        this.classifier = artifact.getClassifier();
        this.type = artifact.getType();
        this.version = artifact.getVersion();
        this.baseVersion = artifact.getBaseVersion();
        this.file = artifact.getFile();
        // TODO: copy properties
    }

    public String getGroupId()
    {
        return groupId;
    }

    public Artifact setGroupId( String groupId )
    {
        this.groupId = groupId;
        return this;
    }

    public String getArtifactId()
    {
        return artifactId;
    }

    public Artifact setArtifactId( String artifactId )
    {
        this.artifactId = artifactId;
        return this;
    }

    public String getClassifier()
    {
        return classifier;
    }

    public Artifact setClassifier( String classifier )
    {
        this.classifier = ( classifier != null ) ? classifier : "";
        return this;
    }

    public String getType()
    {
        return type;
    }

    public Artifact setType( String type )
    {
        this.type = type;
        return this;
    }

    public String getBaseVersion()
    {
        return baseVersion;
    }

    public Artifact setBaseVersion( String baseVersion )
    {
        this.baseVersion = baseVersion;
        return this;
    }

    public String getVersion()
    {
        return version;
    }

    public File getFile()
    {
        return file;
    }

    public String getProperty( String key, String defaultValue )
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Artifact setFile( File file )
    {
        this.file = file;
        return this;
    }

    public Artifact setVersion( String version )
    {
        this.version = version;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder buffer = new StringBuilder( 128 );
        buffer.append( getGroupId() );
        buffer.append( ':' ).append( getArtifactId() );
        buffer.append( ':' ).append( getType() );
        if ( getClassifier().length() > 0 )
        {
            buffer.append( ':' ).append( getClassifier() );
        }
        buffer.append( ':' ).append( getVersion() );
        return buffer.toString();
    }

}
