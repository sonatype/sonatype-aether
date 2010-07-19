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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A request to resolve an artifact.
 * 
 * @author Benjamin Bentmann
 * @see RepositorySystem#resolveArtifacts(RepositorySystemSession, java.util.Collection)
 * @see Artifact#getFile()
 */
public class ArtifactRequest
{

    private Artifact artifact;

    private DependencyNode node;

    private List<RemoteRepository> repositories = Collections.emptyList();

    private String context = "";

    /**
     * Creates an unitialized request.
     */
    public ArtifactRequest()
    {
        // enables default constructor
    }

    /**
     * Creates a request with the specified properties.
     * 
     * @param artifact The artifact to resolve, may be {@code null}.
     * @param repositories The repositories to resolve the artifact from, may be {@code null}.
     * @param context The context in which this request is made, may be {@code null}.
     */
    public ArtifactRequest( Artifact artifact, List<RemoteRepository> repositories, String context )
    {
        setArtifact( artifact );
        setRepositories( repositories );
        setRequestContext( context );
    }

    /**
     * Creates a request from the specified dependency node.
     * 
     * @param node The dependency node to resolve, may be {@code null}.
     */
    public ArtifactRequest( DependencyNode node )
    {
        setDependencyNode( node );
        setRepositories( node.getRepositories() );
        setRequestContext( node.getContext() );
    }

    /**
     * Gets the artifact to resolve.
     * 
     * @return The artifact to resolve or {@code null}.
     */
    public Artifact getArtifact()
    {
        return artifact;
    }

    /**
     * Sets the artifact to resolve.
     * 
     * @param artifact The artifact to resolve, may be {@code null}.
     * @return This request for chaining, never {@code null}.
     */
    public ArtifactRequest setArtifact( Artifact artifact )
    {
        this.artifact = artifact;
        return this;
    }

    /**
     * Gets the dependency node (if any) for which to resolve the artifact.
     * 
     * @return The dependency node to resolve or {@code null} if unknown.
     */
    public DependencyNode getDependencyNode()
    {
        return node;
    }

    /**
     * Sets the dependency node to resolve.
     * 
     * @param node The dependency node to resolve, may be {@code null}.
     * @return This request for chaining, never {@code null}.
     */
    public ArtifactRequest setDependencyNode( DependencyNode node )
    {
        this.node = node;
        if ( node != null )
        {
            setArtifact( node.getDependency().getArtifact() );
        }
        return this;
    }

    /**
     * Gets the repositories to resolve the version range from.
     * 
     * @return The repositories, never {@code null}.
     */
    public List<RemoteRepository> getRepositories()
    {
        return repositories;
    }

    /**
     * Sets the repositories to resolve the artifact from.
     * 
     * @param repositories The repositories, may be {@code null}.
     * @return This request for chaining, never {@code null}.
     */
    public ArtifactRequest setRepositories( List<RemoteRepository> repositories )
    {
        if ( repositories == null )
        {
            this.repositories = Collections.emptyList();
        }
        else
        {
            this.repositories = repositories;
        }
        return this;
    }

    /**
     * Adds the specified repository for the resolution.
     * 
     * @param repository The repository to add, may be {@code null}.
     * @return This request for chaining, never {@code null}.
     */
    public ArtifactRequest addRepository( RemoteRepository repository )
    {
        if ( repository != null )
        {
            if ( this.repositories.isEmpty() )
            {
                this.repositories = new ArrayList<RemoteRepository>();
            }
            this.repositories.add( repository );
        }
        return this;
    }

    /**
     * Gets the context in which this request is made.
     * 
     * @return The context, never {@code null}.
     */
    public String getRequestContext()
    {
        return context;
    }

    /**
     * Sets the context in which this request is made.
     * 
     * @param context The context, may be {@code null}.
     * @return This request for chaining, never {@code null}.
     */
    public ArtifactRequest setRequestContext( String context )
    {
        this.context = ( context != null ) ? context : "";
        return this;
    }

}
