package org.sonatype.aether.artifact;

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

import java.util.Map;

/**
 * An artifact type describing artifact characteristics that are common for certain artifacts. Artifact types are a
 * means to simplify the description of an artifact by referring to an artifact type instead of specifying the various
 * properties individually.
 * 
 * @author Benjamin Bentmann
 */
public interface ArtifactType
{

    /**
     * Gets the identifier of this type, e.g. "maven-plugin" or "test-jar".
     * 
     * @return The identifier of this type, never {@code null}.
     */
    String getId();

    /**
     * Gets the file extension to use for artifacts of this type (unless explicitly overridden by the artifact).
     * 
     * @return The file extension, never {@code null}.
     */
    String getExtension();

    /**
     * Gets the classifier to use for artifacts of this type (unless explicitly overridden by the artifact).
     * 
     * @return The classifier or an empty string if none, never {@code null}.
     */
    String getClassifier();

    /**
     * Gets the properties to use for artifacts of this type (unless explicitly overridden by the artifact).
     * 
     * @return The properties, never {@code null}.
     */
    Map<String, String> getProperties();

}
