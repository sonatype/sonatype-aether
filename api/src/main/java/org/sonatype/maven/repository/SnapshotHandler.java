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

/**
 * A component to handle the notion of snapshot versions. A snapshot version is considered a meta version that gets
 * dynamically expanded upon artifact resolution and deployment. For instance, "1.0-SNAPSHOT" could denote the base
 * version of a snapshot and "1.0-20090208.132618-23" a full version of it.
 * 
 * @author Benjamin Bentmann
 */
public interface SnapshotHandler
{

    /**
     * Indicates whether the specified version denotes a snapshot.
     * 
     * @param version The version to test, must not be {@code null}.
     * @return {@code true} if the version denotes a snapshot, {@code false} otherwise.
     */
    boolean isSnapshot( String version );

    /**
     * Reduces the specified (full) version to its base version. If the given version does not denote a snapshot or is
     * already in base form, the input version is returned,
     * 
     * @param version The version to reduce to its base form, must not be {@code null}.
     * @return The base version, never {@code null}.
     */
    String toBaseVersion( String version );

    /**
     * Expands the specified (base) version to a full version. If the specified version does not denote a base snapshot
     * version, the input version is returned. The handler is supplied with the current UTC timestamp and the build
     * number of the corresponding artifact which it can (but need not) use to build the full version.
     * 
     * @param version The version to expand, must not be {@code null}.
     * @param timestamp The timestamp, must not be {@code null}.
     * @param buildNumber The build number.
     * @return The full version, never {@code null}.
     */
    String toFullVersion( String version, String timestamp, int buildNumber );

}
