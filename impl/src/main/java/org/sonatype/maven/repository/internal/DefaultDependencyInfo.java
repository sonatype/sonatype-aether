package org.sonatype.maven.repository.internal;

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
import java.util.Map;

import org.sonatype.maven.repository.Artifact;
import org.sonatype.maven.repository.Dependency;
import org.sonatype.maven.repository.DependencyInfo;
import org.sonatype.maven.repository.RemoteRepository;
import org.sonatype.maven.repository.Version;
import org.sonatype.maven.repository.VersionConstraint;

/**
 * @author Benjamin Bentmann
 */
public class DefaultDependencyInfo
    implements DependencyInfo
{

    private Dependency dependency;

    private String context = "";

    private List<Artifact> relocations = Collections.emptyList();

    private List<Artifact> aliases = Collections.emptyList();

    private Map<String, Object> properties = Collections.emptyMap();

    private Object conflictId;

    private VersionConstraint versionConstraint;

    private Version version;

    private String premanagedVersion;

    private String premanagedScope;

    private List<RemoteRepository> repositories = Collections.emptyList();

    /**
     * Creates a new root node data with the specified dependency.
     * 
     * @param dependency The dependency associated with this node, may be {@code null}.
     */
    public DefaultDependencyInfo( Dependency dependency )
    {
        this.dependency = dependency;
    }

    public Dependency getDependency()
    {
        return dependency;
    }

    public DefaultDependencyInfo setArtifact( Artifact artifact )
    {
        dependency = dependency.setArtifact( artifact );
        return this;
    }

    public String getContext()
    {
        return context;
    }

    public DefaultDependencyInfo setContext( String context )
    {
        this.context = ( context != null ) ? context : "";
        return this;
    }

    public List<Artifact> getRelocations()
    {
        return relocations;
    }

    public DefaultDependencyInfo setRelocations( List<Artifact> relocations )
    {
        if ( relocations == null || relocations.isEmpty() )
        {
            this.relocations = Collections.emptyList();
        }
        else
        {
            this.relocations = relocations;
        }
        return this;
    }

    public List<Artifact> getAliases()
    {
        return aliases;
    }

    public DefaultDependencyInfo setAliases( List<Artifact> aliases )
    {
        if ( aliases == null || aliases.isEmpty() )
        {
            this.aliases = Collections.emptyList();
        }
        else
        {
            this.aliases = aliases;
        }
        return this;
    }

    public Map<String, Object> getProperties()
    {
        return properties;
    }

    public DefaultDependencyInfo setProperties( Map<String, Object> properties )
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

    public Object getConflictId()
    {
        return conflictId;
    }

    public DefaultDependencyInfo setConflictId( Object conflictId )
    {
        this.conflictId = conflictId;
        return this;
    }

    public VersionConstraint getVersionConstraint()
    {
        return versionConstraint;
    }

    public DefaultDependencyInfo setVersionConstraint( VersionConstraint versionConstraint )
    {
        this.versionConstraint = versionConstraint;
        return this;
    }

    public Version getVersion()
    {
        return version;
    }

    public DefaultDependencyInfo setVersion( Version version )
    {
        this.version = version;
        if ( dependency != null )
        {
            dependency.getArtifact().setVersion( version != null ? version.toString() : null );
        }
        return this;
    }

    public DependencyInfo setScope( String scope )
    {
        dependency = dependency.setScope( scope );
        return this;
    }

    public String getPremanagedVersion()
    {
        return premanagedVersion;
    }

    public DefaultDependencyInfo setPremanagedVersion( String premanagedVersion )
    {
        this.premanagedVersion = ( premanagedVersion != null ) ? premanagedVersion : null;
        return this;
    }

    public String getPremanagedScope()
    {
        return premanagedScope;
    }

    public DefaultDependencyInfo setPremanagedScope( String premanagedScope )
    {
        this.premanagedScope = ( premanagedScope != null ) ? premanagedScope : null;
        return this;
    }

    public List<RemoteRepository> getRepositories()
    {
        return repositories;
    }

    public DefaultDependencyInfo setRepositories( List<RemoteRepository> repositories )
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

    @Override
    public String toString()
    {
        return String.valueOf( getDependency() );
    }

}
