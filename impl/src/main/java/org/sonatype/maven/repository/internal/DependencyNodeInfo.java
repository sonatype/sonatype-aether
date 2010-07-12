package org.sonatype.maven.repository.internal;

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

import java.util.Collections;
import java.util.List;

import org.sonatype.maven.repository.Artifact;
import org.sonatype.maven.repository.Dependency;
import org.sonatype.maven.repository.RemoteRepository;
import org.sonatype.maven.repository.Version;
import org.sonatype.maven.repository.VersionConstraint;

/**
 * @author Benjamin Bentmann
 */
final class DependencyNodeInfo
{

    private Dependency dependency;

    private String context = "";

    private List<Artifact> relocations = Collections.emptyList();

    private List<Artifact> aliases = Collections.emptyList();

    private VersionConstraint versionConstraint;

    private Version version;

    private String premanagedVersion;

    private String premanagedScope;

    private List<RemoteRepository> repositories = Collections.emptyList();

    public DependencyNodeInfo( Dependency dependency )
    {
        this.dependency = dependency;
    }

    public DependencyNodeInfo( DependencyNodeInfo info )
    {
        dependency = info.dependency;
        context = info.context;
        aliases = info.aliases;
        relocations = info.relocations;
        repositories = info.repositories;
        version = info.version;
        versionConstraint = info.versionConstraint;
        premanagedScope = info.premanagedScope;
        premanagedVersion = info.premanagedVersion;
    }

    public Dependency getDependency()
    {
        return dependency;
    }

    public String getContext()
    {
        return context;
    }

    public void setContext( String context )
    {
        this.context = ( context != null ) ? context : "";
    }

    public List<Artifact> getRelocations()
    {
        return relocations;
    }

    public void setRelocations( List<Artifact> relocations )
    {
        if ( relocations == null || relocations.isEmpty() )
        {
            this.relocations = Collections.emptyList();
        }
        else
        {
            this.relocations = relocations;
        }
    }

    public List<Artifact> getAliases()
    {
        return aliases;
    }

    public void setAliases( List<Artifact> aliases )
    {
        if ( aliases == null || aliases.isEmpty() )
        {
            this.aliases = Collections.emptyList();
        }
        else
        {
            this.aliases = aliases;
        }
    }

    public VersionConstraint getVersionConstraint()
    {
        return versionConstraint;
    }

    public void setVersionConstraint( VersionConstraint versionConstraint )
    {
        this.versionConstraint = versionConstraint;
    }

    public Version getVersion()
    {
        return version;
    }

    public void setVersion( Version version )
    {
        this.version = version;
    }

    public void setScope( String scope )
    {
        dependency = dependency.setScope( scope );
    }

    public void setArtifact( Artifact artifact )
    {
        dependency = dependency.setArtifact( artifact );
    }

    public String getPremanagedVersion()
    {
        return premanagedVersion;
    }

    public void setPremanagedVersion( String premanagedVersion )
    {
        this.premanagedVersion = premanagedVersion;
    }

    public String getPremanagedScope()
    {
        return premanagedScope;
    }

    public void setPremanagedScope( String premanagedScope )
    {
        this.premanagedScope = premanagedScope;
    }

    public List<RemoteRepository> getRepositories()
    {
        return repositories;
    }

    public void setRepositories( List<RemoteRepository> repositories )
    {
        if ( repositories == null || repositories.isEmpty() )
        {
            this.repositories = Collections.emptyList();
        }
        else
        {
            this.repositories = repositories;
        }
    }

}
