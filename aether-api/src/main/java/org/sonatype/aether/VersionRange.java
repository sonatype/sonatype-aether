package org.sonatype.aether;

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

/**
 * A range of versions.
 * 
 * @author Benjamin Bentmann
 */
public interface VersionRange
{

    /**
     * Determines whether the specified version is contained within this range.
     * 
     * @param version The version to test, must not be {@code null}.
     * @return {@code true} if this range contains the specified version, {@code false} otherwise.
     */
    boolean containsVersion( Version version );

    /**
     * Determines whether this range can contain snapshot versions.
     * 
     * @return {@code true} if this range can contain snapshot versions, {@code false} otherwise.
     */
    boolean containsSnapshots();

}
