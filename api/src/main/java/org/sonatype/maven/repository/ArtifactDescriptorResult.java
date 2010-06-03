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
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Benjamin Bentmann
 */
public class ArtifactDescriptorResult
{

    private final ArtifactDescriptorRequest request;

    private final List<Exception> exceptions;

    private final List<Artifact> relocations;

    private Artifact artifact;

    private ArtifactRepository repository;

    private List<Dependency> dependencies;

    private List<Dependency> managedDependencies;

    private List<RemoteRepository> repositories;

    private Map<String, Object> properties;

    public ArtifactDescriptorResult( ArtifactDescriptorRequest request )
    {
        if ( request == null )
        {
            throw new IllegalArgumentException( "version request has not been specified" );
        }
        this.request = request;
        this.artifact = request.getArtifact();
        this.exceptions = new ArrayList<Exception>( 2 );
        this.relocations = new ArrayList<Artifact>( 2 );
        this.dependencies = new ArrayList<Dependency>();
        this.managedDependencies = new ArrayList<Dependency>();
        this.repositories = new ArrayList<RemoteRepository>();
        this.properties = Collections.emptyMap();
    }

    public ArtifactDescriptorRequest getRequest()
    {
        return request;
    }

    public List<Exception> getExceptions()
    {
        return exceptions;
    }

    public ArtifactDescriptorResult addException( Exception exception )
    {
        if ( exception != null )
        {
            this.exceptions.add( exception );
        }
        return this;
    }

    public List<Artifact> getRelocations()
    {
        return relocations;
    }

    public ArtifactDescriptorResult addRelocation( Artifact artifact )
    {
        if ( artifact != null )
        {
            relocations.add( artifact );
        }
        return this;
    }

    public Artifact getArtifact()
    {
        return artifact;
    }

    public ArtifactDescriptorResult setArtifact( Artifact artifact )
    {
        this.artifact = artifact;
        return this;
    }

    public ArtifactRepository getRepository()
    {
        return repository;
    }

    public ArtifactDescriptorResult setRepository( ArtifactRepository repository )
    {
        this.repository = repository;
        return this;
    }

    public List<Dependency> getDependencies()
    {
        return dependencies;
    }

    public ArtifactDescriptorResult addDependency( Dependency dependency )
    {
        if ( dependency == null )
        {
            throw new IllegalArgumentException( "no dependency specified" );
        }
        dependencies.add( dependency );
        return this;
    }

    public List<Dependency> getManagedDependencies()
    {
        return managedDependencies;
    }

    public ArtifactDescriptorResult addManagedDependency( Dependency dependency )
    {
        if ( dependency == null )
        {
            throw new IllegalArgumentException( "no dependency specified" );
        }
        managedDependencies.add( dependency );
        return this;
    }

    public List<RemoteRepository> getRepositories()
    {
        return repositories;
    }

    public ArtifactDescriptorResult addRepository( RemoteRepository repository )
    {
        if ( repository == null )
        {
            throw new IllegalArgumentException( "no repository specified" );
        }
        repositories.add( repository );
        return this;
    }

    public Map<String, Object> getProperties()
    {
        return properties;
    }

    public ArtifactDescriptorResult setProperties( Map<String, Object> properties )
    {
        if ( properties == null )
        {
            this.properties = Collections.emptyMap();
        }
        else
        {
            this.properties = properties;
        }
        return this;
    }

}
