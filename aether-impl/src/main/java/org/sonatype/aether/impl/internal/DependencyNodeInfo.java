package org.sonatype.aether.impl.internal;

/*******************************************************************************
 * Copyright (c) 2010 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import java.util.Collections;
import java.util.List;

import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.version.Version;
import org.sonatype.aether.version.VersionConstraint;

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
