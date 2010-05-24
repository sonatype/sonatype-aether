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

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Benjamin Bentmann
 */
public class DeployRequest
{

    private Collection<Artifact> artifacts = new ArrayList<Artifact>();

    private Collection<Metadata> metadata = new ArrayList<Metadata>();

    private RemoteRepository repository;

    public Collection<Artifact> getArtifacts()
    {
        return artifacts;
    }

    public DeployRequest setArtifacts( Collection<Artifact> artifacts )
    {
        this.artifacts = ( artifacts != null ) ? artifacts : new ArrayList<Artifact>();
        return this;
    }

    public DeployRequest addArtifact( Artifact artifact )
    {
        if ( artifact != null )
        {
            artifacts.add( artifact );
        }
        return this;
    }

    public Collection<Metadata> getMetadata()
    {
        return metadata;
    }

    public DeployRequest setMetadata( Collection<Metadata> metadata )
    {
        this.metadata = ( metadata != null ) ? metadata : new ArrayList<Metadata>();
        return this;
    }

    public DeployRequest addMetadata( Metadata metadata )
    {
        if ( metadata != null )
        {
            this.metadata.add( metadata );
        }
        return this;
    }

    public RemoteRepository getRepository()
    {
        return repository;
    }

    public DeployRequest setRepository( RemoteRepository repository )
    {
        this.repository = repository;
        return this;
    }

}
