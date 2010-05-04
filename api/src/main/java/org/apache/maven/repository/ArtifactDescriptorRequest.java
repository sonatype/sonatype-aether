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

import java.util.List;

/**
 * @author Benjamin Bentmann
 */
public class ArtifactDescriptorRequest
{

    private Artifact artifact;

    private List<RemoteRepository> repositories;

    private boolean ignoreMissingDescriptor;

    private boolean ignoreInvalidDescriptor;

    public ArtifactDescriptorRequest()
    {
        // enables default constructor
    }

    public ArtifactDescriptorRequest( Artifact artifact, List<RemoteRepository> repositories )
    {
        setArtifact( artifact );
        setRemoteRepositories( repositories );
    }

    public Artifact getArtifact()
    {
        return artifact;
    }

    public ArtifactDescriptorRequest setArtifact( Artifact artifact )
    {
        this.artifact = artifact;
        return this;
    }

    public List<RemoteRepository> getRemoteRepositories()
    {
        return repositories;
    }

    public ArtifactDescriptorRequest setRemoteRepositories( List<RemoteRepository> repositories )
    {
        this.repositories = repositories;
        return this;
    }

    public boolean isIgnoreMissingDescriptor()
    {
        return ignoreMissingDescriptor;
    }

    public ArtifactDescriptorRequest setIgnoreMissingDescriptor( boolean ignoreMissingDescriptor )
    {
        this.ignoreMissingDescriptor = ignoreMissingDescriptor;
        return this;
    }

    public boolean isIgnoreInvalidDescriptor()
    {
        return ignoreInvalidDescriptor;
    }

    public ArtifactDescriptorRequest setIgnoreInvalidDescriptor( boolean ignoreInvalidDescriptor )
    {
        this.ignoreInvalidDescriptor = ignoreInvalidDescriptor;
        return this;
    }

}
