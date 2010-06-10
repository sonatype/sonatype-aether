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

import java.util.List;

/**
 * The management updates to apply to a dependency.
 * 
 * @author Benjamin Bentmann
 * @see DependencyManager#manageDependency(DependencyNode, Dependency)
 */
public class DependencyManagement
{

    private String version;

    private String scope;

    private List<Exclusion> exclusions;

    /**
     * Creates an empty management update.
     */
    public DependencyManagement()
    {
        // enables default constructor
    }

    /**
     * Gets the managed version to apply to the dependency.
     * 
     * @return The managed version or {@code null} if the version is not managed.
     */
    public String getVersion()
    {
        return version;
    }

    /**
     * Sets the managed version to apply to the dependency.
     * 
     * @param version The managed version, may be {@code null} if the version is not managed.
     * @return This management update for chaining, never {@code null}.
     */
    public DependencyManagement setVersion( String version )
    {
        this.version = version;
        return this;
    }

    /**
     * Gets the managed scope to apply to the dependency.
     * 
     * @return The managed scope or {@code null} if the scope is not managed.
     */
    public String getScope()
    {
        return scope;
    }

    /**
     * Sets the managed scope to apply to the dependency.
     * 
     * @param scope The managed scope, may be {@code null} if the scope is not managed.
     * @return This management update for chaining, never {@code null}.
     */
    public DependencyManagement setScope( String scope )
    {
        this.scope = scope;
        return this;
    }

    /**
     * Gets the managed exclusions to apply to the dependency.
     * 
     * @return The managed exclusions or {@code null} if the exclusions are not managed.
     */
    public List<Exclusion> getExclusions()
    {
        return exclusions;
    }

    /**
     * Sets the managed exclusions to apply to the dependency.
     * 
     * @param exclusions The managed exclusions, may be {@code null} if the exclusions are not managed.
     * @return This management update for chaining, never {@code null}.
     */
    public DependencyManagement setExclusions( List<Exclusion> exclusions )
    {
        this.exclusions = exclusions;
        return this;
    }

}
