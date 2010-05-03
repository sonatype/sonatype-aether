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
public class ResolveRequest
{

    private Artifact artifact;

    private List<? extends RemoteRepository> repositories;

    public ResolveRequest()
    {
        // enables default constructor
    }

    public ResolveRequest( Artifact artifact, List<? extends RemoteRepository> repositories )
    {
        this.artifact = artifact;
        this.repositories = repositories;
    }

    public Artifact getArtifact()
    {
        return artifact;
    }

    public ResolveRequest setArtifact( Artifact artifact )
    {
        this.artifact = artifact;
        return this;
    }

    public List<? extends RemoteRepository> getRemoteRepositories()
    {
        return ( repositories != null ) ? repositories : Collections.<RemoteRepository> emptyList();
    }

    public ResolveRequest setRemoteRepositories( List<? extends RemoteRepository> repositories )
    {
        this.repositories = repositories;
        return this;
    }

}
