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
import java.util.Map;

/**
 * A specific artifact.
 * 
 * @author Benjamin Bentmann
 */
public interface Artifact
{

    /**
     * Gets the group identifier of this artifact, e.g. "org.apache.maven".
     * 
     * @return The group identifier, never {@code null}.
     */
    String getGroupId();

    /**
     * Gets the artifact identifier of this artifact, e.g. "maven-model".
     * 
     * @return The artifact identifier, never {@code null}.
     */
    String getArtifactId();

    /**
     * Gets the base version of this artifact, e.g. "1.0-SNAPSHOT". In contrast to the {@link #getVersion()}, the base
     * version will always refer to the unresolved metaversion, e.g. "1.0-SNAPSHOT".
     * 
     * @return The base version, never {@code null}.
     */
    String getBaseVersion();

    /**
     * Gets the version of this artifact, e.g. "1.0-20100529-1213". Note that in case of metaversions like
     * "1.0-SNAPSHOT", the artifact's version depends on the state of the artifact. Artifacts that have been resolved or
     * deployed will have the metaversion expanded.
     * 
     * @return The version, never {@code null}.
     */
    String getVersion();

    /**
     * Sets the version of this artifact.
     * 
     * @param version The version of this artifact, may be {@code null}.
     */
    void setVersion( String version );

    /**
     * Determines whether this artifact uses a SNAPSHOT version.
     * 
     * @return {@code true} if the artifact is a snapshot, {@code false} otherwise.
     */
    boolean isSnapshot();

    /**
     * Gets the classifier of this artifact, e.g. "sources".
     * 
     * @return The classifier or an empty string if none, never {@code null}.
     */
    String getClassifier();

    /**
     * Gets the (file) extension of this artifact, e.g. "jar".
     * 
     * @return The file extension, never {@code null}.
     */
    String getExtension();

    /**
     * Gets the file of this artifact. Note that only resolved artifacts have a file associated with them.
     * 
     * @return The file or {@code null} if none.
     */
    File getFile();

    /**
     * Sets the file of the artifact.
     * 
     * @param file The file of the artifact, may be {@code null}
     */
    void setFile( File file );

    /**
     * Gets the specified property.
     * 
     * @param key The name of the property, must not be {@code null}.
     * @param defaultValue The default value to return in case the property is not set, may be {@code null}.
     */
    String getProperty( String key, String defaultValue );

    /**
     * Gets the properties of this artifact. While the set of available properties is undefined, the following
     * properties are considered to be common:
     * <dl>
     * <dt>stereotype</dt>
     * <dd>A high-level characterization of the artifact, e.g. "maven-plugin" or "test-jar".</dd>
     * <dt>language</dt>
     * <dd>The programming language this artifact is relevant for, e.g. "java" or "none".</dd>
     * <dt>includesDependencies</dt>
     * <dd>A boolean flag whether the artifact presents some kind of bundle that physically includes its dependencies,
     * e.g. a fat WAR.</dd>
     * <dt>constitutesBuildPath</dt>
     * <dd>A boolean flag whether the artifact is meant to be used for the compile/runtime/test build path of a consumer
     * project.</dd>
     * </dl>
     * 
     * @return The properties, never {@code null}.
     */
    Map<String, String> getProperties();

    /**
     * Creates a deep copy of this artifact.
     * 
     * @return The clone of this artifact, never {@code null}.
     */
    Artifact clone();

}
