package org.sonatype.aether.metadata;

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

/**
 * A piece of repository metadata, e.g. an index of available versions. In contrast to an artifact, which usually exists
 * in only one repository, metadata usually exists in multiple repositories and each repository contains a different
 * copy of the metadata. <em>Note:</em> Metadata instances are supposed to be immutable, e.g. any exposed mutator method
 * returns a new metadata instance and leaves the original instance unchanged. Implementors are strongly advised to obey
 * this contract.
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
    File getFile();

    /**
     * Sets the file of the metadata.
     * 
     * @param file The file of the metadata, may be {@code null}
     * @return The new metadata, never {@code null}.
     */
    Metadata setFile( File file );

}
