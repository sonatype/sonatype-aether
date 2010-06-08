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
 * A registry of known artifact stereotypes.
 * 
 * @author Benjamin Bentmann
 */
public interface ArtifactStereotypeRegistry
{

    /**
     * Gets the stereotype with the specified identifier.
     * 
     * @param stereotypeId The identifier of the stereotype, must not be {@code null}.
     * @return The artifact stereotype or {@code null} if no stereotype with the requested identifier exists.
     */
    ArtifactStereotype get( String stereotypeId );

}
