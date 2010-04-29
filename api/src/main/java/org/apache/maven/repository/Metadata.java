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
public class Metadata
{

    private String groupId;
    
    private String artifactId;
    
    private String version;
    
    // e.g. "maven-metadata.xml", "archetype-catalog.xml" or "nexus-maven-repository-index.properties", i.e the simple file name used by classical URL-based repos
    private String type;
    
    private File file;

    public String getGroupId()
    {
        return groupId;
    }

    public Metadata setGroupId( String groupId )
    {
        this.groupId = groupId;
        return this;
    }

    public String getArtifactId()
    {
        return artifactId;
    }

    public Metadata setArtifactId( String artifactId )
    {
        this.artifactId = artifactId;
        return this;
    }

    public String getVersion()
    {
        return version;
    }

    public Metadata setVersion( String version )
    {
        this.version = version;
        return this;
    }

    public String getType()
    {
        return type;
    }

    public Metadata setType( String type )
    {
        this.type = type;
        return this;
    }

    public File getFile()
    {
        return file;
    }

    public Metadata setFile( File file )
    {
        this.file = file;
        return this;
    }

/*

<metadata>
  <versioning>
    <snapshot>
      <timestamp>20100419.164221</timestamp>
      <buildNumber>46</buildNumber>
    </snapshot>
    <snapshotVersions>
      <snapshotVersion>
        <classifier>win</classifier>
        <format>${timeStamp}-${buildNo}</format>
        <timeStamp>20100419.164221</timeStamp>
        <buildNo>46</buildNo>
      </snapshotVersion>
    </snapshotVersions>
    <lastUpdated>20100419164659</lastUpdated>
  </versioning>
</metadata>

 */
}
