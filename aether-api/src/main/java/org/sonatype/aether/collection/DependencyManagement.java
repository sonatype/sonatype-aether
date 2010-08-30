package org.sonatype.aether.collection;

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

import java.util.Collection;
import java.util.Map;

import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.graph.Exclusion;

/**
 * The management updates to apply to a dependency.
 * 
 * @author Benjamin Bentmann
 * @see DependencyManager#manageDependency(Dependency)
 */
public class DependencyManagement
{

    private String version;

    private String scope;

    private Collection<Exclusion> exclusions;

    private Map<String, String> properties;

    /**
     * Creates an empty management update.
     */
    public DependencyManagement()
    {
        // enables default constructor
    }

    /**
     * Gets the new version to apply to the dependency.
     * 
     * @return The new version or {@code null} if the version is not managed and the existing dependency version should
     *         remain unchanged.
     */
    public String getVersion()
    {
        return version;
    }

    /**
     * Sets the new version to apply to the dependency.
     * 
     * @param version The new version, may be {@code null} if the version is not managed.
     * @return This management update for chaining, never {@code null}.
     */
    public DependencyManagement setVersion( String version )
    {
        this.version = version;
        return this;
    }

    /**
     * Gets the new scope to apply to the dependency.
     * 
     * @return The new scope or {@code null} if the scope is not managed and the existing dependency scope should remain
     *         unchanged.
     */
    public String getScope()
    {
        return scope;
    }

    /**
     * Sets the new scope to apply to the dependency.
     * 
     * @param scope The new scope, may be {@code null} if the scope is not managed.
     * @return This management update for chaining, never {@code null}.
     */
    public DependencyManagement setScope( String scope )
    {
        this.scope = scope;
        return this;
    }

    /**
     * Gets the new exclusions to apply to the dependency. Note that this collection denotes the complete set of
     * exclusions for the dependency, i.e. the dependency manager controls whether any existing exclusions get merged
     * with information from dependency management or overridden by it.
     * 
     * @return The new exclusions or {@code null} if the exclusions are not managed and the existing dependency
     *         exclusions should remain unchanged.
     */
    public Collection<Exclusion> getExclusions()
    {
        return exclusions;
    }

    /**
     * Sets the new exclusions to apply to the dependency. Note that this collection denotes the complete set of
     * exclusions for the dependency, i.e. the dependency manager controls whether any existing exclusions get merged
     * with information from dependency management or overridden by it.
     * 
     * @param exclusions The new exclusions, may be {@code null} if the exclusions are not managed.
     * @return This management update for chaining, never {@code null}.
     */
    public DependencyManagement setExclusions( Collection<Exclusion> exclusions )
    {
        this.exclusions = exclusions;
        return this;
    }

    /**
     * Gets the new properties to apply to the dependency. Note that this map denotes the complete set of properties,
     * i.e. the dependency manager controls whether any existing properties get merged with the information from
     * dependency management or overridden by it.
     * 
     * @return The new artifact properties or {@code null} if the properties are not managed and the existing properties
     *         should remain unchanged.
     */
    public Map<String, String> getProperties()
    {
        return properties;
    }

    /**
     * Sets the new properties to apply to the dependency. Note that this map denotes the complete set of properties,
     * i.e. the dependency manager controls whether any existing properties get merged with the information from
     * dependency management or overridden by it.
     * 
     * @param properties The new artifact properties, may be {@code null} if the properties are not managed.
     * @return This management update for chaining, never {@code null}.
     */
    public DependencyManagement setProperties( Map<String, String> properties )
    {
        this.properties = properties;
        return this;
    }

}
