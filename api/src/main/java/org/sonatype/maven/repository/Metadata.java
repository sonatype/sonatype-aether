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
 * A piece of repository metadata, e.g. an index of available versions. In contrast to an artifact, which usually exists
 * in only one repository, metadata usually exists in multiple repositories and each repository contains a different
 * copy of the metadata.
 * 
 * @author Benjamin Bentmann
 */
public interface Metadata
{

    /**
     * The nature of the metadata.
     */
    enum Nature
    {
        /**
         * The metadata refers to release artifacts only.
         */
        RELEASE,

        /**
         * The metadata refers to snapshot artifacts only.
         */
        SNAPSHOT,

        /**
         * The metadata refers to either release or snapshot artifacts.
         */
        RELEASE_OR_SNAPSHOT
    }

    /**
     * Gets the group identifier of this metadata.
     * 
     * @return The group identifier or an empty string if the metadata applies to the entire repository, never {@code
     *         null}.
     */
    String getGroupId();

    /**
     * Gets the artifact identifier of this metadata.
     * 
     * @return The artifact identifier or an empty string if the metadata applies to the groupId level only, never
     *         {@code null}.
     */
    String getArtifactId();

    /**
     * Gets the version of this metadata.
     * 
     * @return The version or an empty string if the metadata applies to the groupId:artifactId level only, never
     *         {@code null}.
     */
    String getVersion();

    /**
     * Gets the type of the metadata, e.g. "maven-metadata.xml".
     * 
     * @return The type of the metadata, never {@code null}.
     */
    String getType();

    /**
     * Gets the nature of this metadata. The nature indicates to what artifact versions the metadata refers.
     * 
     * @return The nature, never {@code null}.
     */
    Nature getNature();

    /**
     * Gets the file of this metadata. Note that only resolved metadata has a file associated with it.
     * 
     * @return The file or {@code null} if none.
     */
    public File getFile();

    /**
     * Sets the file of the metadata.
     * 
     * @param file The file of the metadata, may be {@code null}
     */
    void setFile( File file );

    /*
     * <metadata> <versioning> <snapshot> <timestamp>20100419.164221</timestamp> <buildNumber>46</buildNumber>
     * </snapshot> <!-- beware MODELLO-237 --> <snapshotVersions> <snapshotVersion> <classifier>win</classifier>
     * <format>${timeStamp}-${buildNo}</format> <timeStamp>20100419.164221</timeStamp> <buildNo>46</buildNo>
     * </snapshotVersion> </snapshotVersions> <lastUpdated>20100419164659</lastUpdated> </versioning> </metadata>
     */
}
