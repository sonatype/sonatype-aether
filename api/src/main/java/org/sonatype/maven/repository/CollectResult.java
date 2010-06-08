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
import java.util.List;

/**
 * The result of a dependency collection request.
 * 
 * @author Benjamin Bentmann
 * @see RepositorySystem#collectDependencies(RepositorySession, CollectRequest)
 */
public class CollectResult
{

    private final CollectRequest request;

    private final List<Exception> exceptions;

    private DependencyNode root;

    /**
     * Creates a new result for the specified request.
     * 
     * @param request The resolution request, must not be {@code null}.
     */
    public CollectResult( CollectRequest request )
    {
        if ( request == null )
        {
            throw new IllegalArgumentException( "dependency collection request has not been specified" );
        }
        this.request = request;
        this.exceptions = new ArrayList<Exception>( 4 );
    }

    /**
     * Gets the collection request that was made.
     * 
     * @return The collection request, never {@code null}.
     */
    public CollectRequest getRequest()
    {
        return request;
    }

    /**
     * Gets the exceptions that occurred while building the dependency graph.
     * 
     * @return The exceptions that occurred, never {@code null}.
     */
    public List<Exception> getExceptions()
    {
        return exceptions;
    }

    /**
     * Records the specified exception while building the dependency graph.
     * 
     * @param exception The exception to record, may be {@code null}.
     * @return This result for chaining, never {@code null}.
     */
    public CollectResult addException( Exception exception )
    {
        if ( exception != null )
        {
            this.exceptions.add( exception );
        }
        return this;
    }

    /**
     * Gets the root node of the dependency graph.
     * 
     * @return The root node of the dependency graph or {@code null} if none.
     */
    public DependencyNode getRoot()
    {
        return root;
    }

    /**
     * Sets the root node of the dependency graph.
     * 
     * @param root The root node of the dependency graph, may be {@code null}.
     * @return This result for chaining, never {@code null}.
     */
    public CollectResult setRoot( DependencyNode root )
    {
        this.root = root;
        return this;
    }

}
