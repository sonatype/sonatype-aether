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

import java.util.Map;

/**
 * An artifact stereotype describing artifact characteristics that are common for certain artifacts.
 * 
 * @author Benjamin Bentmann
 */
public interface ArtifactStereotype
{

    /**
     * Gets the identifier of this stereotype, e.g. "maven-plugin" or "test-jar".
     * 
     * @return The identifier of this stereotype, never {@code null}.
     */
    String getId();

    /**
     * Gets the file type to use for artifacts with this stereotype.
     * 
     * @return The file type, never {@code null}.
     */
    String getType();

    /**
     * Gets the classifier to use for artifacts with this stereotype.
     * 
     * @return The classifier or an empty string if none, never {@code null}.
     */
    String getClassifier();

    /**
     * Gets the properties to use for artifacts with this stereotype.
     * 
     * @return The properties, never {@code null}.
     */
    Map<String, Object> getProperties();

}
