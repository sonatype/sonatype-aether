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

import java.util.Collections;
import java.util.List;

/**
 * @author Benjamin Bentmann
 */
public class ArtifactRequest
{

    private Artifact artifact;

    private List<RemoteRepository> repositories;

    public ArtifactRequest()
    {
        // enables default constructor
    }

    public ArtifactRequest( Artifact artifact, List<RemoteRepository> repositories )
    {
        this.artifact = artifact;
        this.repositories = repositories;
    }

    public Artifact getArtifact()
    {
        return artifact;
    }

    public ArtifactRequest setArtifact( Artifact artifact )
    {
        this.artifact = artifact;
        return this;
    }

    public List<RemoteRepository> getRemoteRepositories()
    {
        return ( repositories != null ) ? repositories : Collections.<RemoteRepository> emptyList();
    }

    public ArtifactRequest setRemoteRepositories( List<RemoteRepository> repositories )
    {
        this.repositories = repositories;
        return this;
    }

}
