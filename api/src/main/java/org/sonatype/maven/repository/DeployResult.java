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

import java.util.ArrayList;
import java.util.Collection;

/**
 * The result of deploying artifacts and their accompanying metadata into the a remote repository.
 * 
 * @author Benjamin Bentmann
 * @see RepositorySystem#deploy(RepositorySession, DeployRequest)
 */
public class DeployResult
{

    private final DeployRequest request;

    private Collection<Artifact> artifacts = new ArrayList<Artifact>();

    private Collection<Metadata> metadata = new ArrayList<Metadata>();

    /**
     * Creates a new result for the specified request.
     * 
     * @param request The deployment request, must not be {@code null}.
     */
    public DeployResult( DeployRequest request )
    {
        if ( request == null )
        {
            throw new IllegalArgumentException( "deploy request has not been specified" );
        }
        this.request = request;
    }

    /**
     * Gets the deploy request that was made.
     * 
     * @return The deploy request, never {@code null}.
     */
    public DeployRequest getRequest()
    {
        return request;
    }

    /**
     * Gets the artifact that got deployed.
     * 
     * @return The artifacts to deploy, never {@code null}.
     */
    public Collection<Artifact> getArtifacts()
    {
        return artifacts;
    }

    /**
     * Sets the artifacts that got deployed.
     * 
     * @param artifacts The deployed artifacts, may be {@code null}.
     * @return This result for chaining, never {@code null}.
     */
    public DeployResult setArtifacts( Collection<Artifact> artifacts )
    {
        if ( artifacts == null )
        {
            this.artifacts = new ArrayList<Artifact>();
        }
        else
        {
            this.artifacts = artifacts;
        }
        return this;
    }

    /**
     * Adds the specified artifacts to the result.
     * 
     * @param artifact The deployed artifact to add, may be {@code null}.
     * @return This result for chaining, never {@code null}.
     */
    public DeployResult addArtifact( Artifact artifact )
    {
        if ( artifact != null )
        {
            artifacts.add( artifact );
        }
        return this;
    }

    /**
     * Gets the metadata that got deployed.
     * 
     * @return The deployed metadata, never {@code null}.
     */
    public Collection<Metadata> getMetadata()
    {
        return metadata;
    }

    /**
     * Sets the metadata that got deployed.
     * 
     * @param metadata The deployed metadata, may be {@code null}.
     * @return This result for chaining, never {@code null}.
     */
    public DeployResult setMetadata( Collection<Metadata> metadata )
    {
        if ( metadata == null )
        {
            this.metadata = new ArrayList<Metadata>();
        }
        else
        {
            this.metadata = metadata;
        }
        return this;
    }

    /**
     * Adds the specified metadata to this result.
     * 
     * @param metadata The deployed metadata to add, may be {@code null}.
     * @return This result for chaining, never {@code null}.
     */
    public DeployResult addMetadata( Metadata metadata )
    {
        if ( metadata != null )
        {
            this.metadata.add( metadata );
        }
        return this;
    }

}
