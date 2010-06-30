package org.sonatype.maven.repository.internal;

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
public class OverlayedDependencyInfo
    implements DependencyInfo
{

    private final DependencyInfo delegate;

    private List<RemoteRepository> repositories;

    private String context;

    public OverlayedDependencyInfo( DependencyInfo delegate )
    {
        this.delegate = delegate;
    }

    public List<Artifact> getAliases()
    {
        return delegate.getAliases();
    }

    public Object getConflictId()
    {
        return delegate.getConflictId();
    }

    public String getContext()
    {
        return ( context == null ) ? delegate.getContext() : context;
    }

    public Dependency getDependency()
    {
        return delegate.getDependency();
    }

    public DependencyInfo setArtifact( Artifact artifact )
    {
        return delegate.setArtifact( artifact );
    }

    public String getPremanagedScope()
    {
        return delegate.getPremanagedScope();
    }

    public String getPremanagedVersion()
    {
        return delegate.getPremanagedVersion();
    }

    public Map<String, Object> getProperties()
    {
        return delegate.getProperties();
    }

    public List<Artifact> getRelocations()
    {
        return delegate.getRelocations();
    }

    public List<RemoteRepository> getRepositories()
    {
        return ( repositories == null ) ? delegate.getRepositories() : repositories;
    }

    public Version getVersion()
    {
        return delegate.getVersion();
    }

    public VersionConstraint getVersionConstraint()
    {
        return delegate.getVersionConstraint();
    }

    public DependencyInfo setAliases( List<Artifact> aliases )
    {
        delegate.setAliases( aliases );
        return this;
    }

    public DependencyInfo setConflictId( Object conflictId )
    {
        delegate.setConflictId( conflictId );
        return this;
    }

    public DependencyInfo setContext( String context )
    {
        this.context = ( context != null ) ? context : "";
        return this;
    }

    public DependencyInfo setPremanagedScope( String premanagedScope )
    {
        delegate.setPremanagedScope( premanagedScope );
        return this;
    }

    public DependencyInfo setPremanagedVersion( String premanagedVersion )
    {
        delegate.setPremanagedVersion( premanagedVersion );
        return this;
    }

    public DependencyInfo setProperties( Map<String, Object> properties )
    {
        delegate.setProperties( properties );
        return this;
    }

    public DependencyInfo setRelocations( List<Artifact> relocations )
    {
        delegate.setRelocations( relocations );
        return this;
    }

    public DependencyInfo setRepositories( List<RemoteRepository> repositories )
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

    public DependencyInfo setVersion( Version version )
    {
        delegate.setVersion( version );
        return this;
    }

    public DependencyInfo setVersionConstraint( VersionConstraint versionConstraint )
    {
        delegate.setVersionConstraint( versionConstraint );
        return this;
    }

    public DependencyInfo setScope( String scope )
    {
        delegate.setScope( scope );
        return this;
    }

}
