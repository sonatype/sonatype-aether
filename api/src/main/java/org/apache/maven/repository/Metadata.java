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
public interface Metadata
{

    String getGroupId();

    String getArtifactId();

    String getVersion();

    // e.g. "maven-metadata.xml", "archetype-catalog.xml" or "nexus-maven-repository-index.properties", i.e the simple file name used by classical URL-based repos
    String getKind();

    File getFile();

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
