package org.sonatype.aether.artifact;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *******************************************************************************/

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
