package org.apache.maven.repo.internal;

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

import java.io.File;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.DistributionManagement;
import org.apache.maven.model.License;
import org.apache.maven.model.Model;
import org.apache.maven.model.Relocation;
import org.apache.maven.model.Repository;
import org.apache.maven.model.building.DefaultModelBuildingRequest;
import org.apache.maven.model.building.FileModelSource;
import org.apache.maven.model.building.ModelBuilder;
import org.apache.maven.model.building.ModelBuildingException;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.model.building.ModelProblem;
import org.apache.maven.model.resolution.UnresolvableModelException;
import org.apache.maven.repo.Artifact;
import org.apache.maven.repo.ArtifactDescriptorException;
import org.apache.maven.repo.ArtifactDescriptorRequest;
import org.apache.maven.repo.ArtifactDescriptorResult;
import org.apache.maven.repo.ArtifactNotFoundException;
import org.apache.maven.repo.ArtifactRequest;
import org.apache.maven.repo.ArtifactResolutionException;
import org.apache.maven.repo.ArtifactResult;
import org.apache.maven.repo.ArtifactStereotype;
import org.apache.maven.repo.ArtifactStereotypeManager;
import org.apache.maven.repo.DefaultArtifact;
import org.apache.maven.repo.DefaultSubArtifact;
import org.apache.maven.repo.Dependency;
import org.apache.maven.repo.Exclusion;
import org.apache.maven.repo.RemoteRepository;
import org.apache.maven.repo.RepositoryException;
import org.apache.maven.repo.RepositoryListener;
import org.apache.maven.repo.RepositoryPolicy;
import org.apache.maven.repo.RepositorySession;
import org.apache.maven.repo.VersionRequest;
import org.apache.maven.repo.VersionResolutionException;
import org.apache.maven.repo.WorkspaceRepository;
import org.apache.maven.repo.spi.ArtifactDescriptorReader;
import org.apache.maven.repo.spi.ArtifactResolver;
import org.apache.maven.repo.spi.Logger;
import org.apache.maven.repo.spi.NullLogger;
import org.apache.maven.repo.spi.RemoteRepositoryManager;
import org.apache.maven.repo.spi.VersionResolver;
import org.apache.maven.repo.util.DefaultArtifactStereotype;
import org.apache.maven.repo.util.DefaultRepositoryEvent;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

/**
 * @author Benjamin Bentmann
 */
@Component( role = ArtifactDescriptorReader.class )
public class DefaultArtifactDescriptorReader
    implements ArtifactDescriptorReader
{

    @Requirement
    private Logger logger = NullLogger.INSTANCE;

    @Requirement
    private RemoteRepositoryManager remoteRepositoryManager;

    @Requirement
    private VersionResolver versionResolver;

    @Requirement
    private ArtifactResolver artifactResolver;

    @Requirement
    private ModelBuilder modelBuilder;

    public DefaultArtifactDescriptorReader setLogger( Logger logger )
    {
        this.logger = ( logger != null ) ? logger : NullLogger.INSTANCE;
        return this;
    }

    public DefaultArtifactDescriptorReader setRemoteRepositoryManager( RemoteRepositoryManager remoteRepositoryManager )
    {
        if ( remoteRepositoryManager == null )
        {
            throw new IllegalArgumentException( "remote repository manager has not been specified" );
        }
        this.remoteRepositoryManager = remoteRepositoryManager;
        return this;
    }

    public DefaultArtifactDescriptorReader setVersionResolver( VersionResolver versionResolver )
    {
        if ( versionResolver == null )
        {
            throw new IllegalArgumentException( "version resolver has not been specified" );
        }
        this.versionResolver = versionResolver;
        return this;
    }

    public DefaultArtifactDescriptorReader setArtifactResolver( ArtifactResolver artifactResolver )
    {
        if ( artifactResolver == null )
        {
            throw new IllegalArgumentException( "artifact resolver has not been specified" );
        }
        this.artifactResolver = artifactResolver;
        return this;
    }

    public DefaultArtifactDescriptorReader setModelBuilder( ModelBuilder modelBuilder )
    {
        if ( modelBuilder == null )
        {
            throw new IllegalArgumentException( "model builder has not been specified" );
        }
        this.modelBuilder = modelBuilder;
        return this;
    }

    public ArtifactDescriptorResult readArtifactDescriptor( RepositorySession session, ArtifactDescriptorRequest request )
        throws ArtifactDescriptorException
    {
        ArtifactDescriptorResult result = new ArtifactDescriptorResult( request );

        Model model = loadPom( session, request, result );

        if ( model != null )
        {
            ArtifactStereotypeManager stereotypes = session.getArtifactStereotypeManager();

            for ( Repository r : model.getRepositories() )
            {
                result.addRepository( convert( r ) );
            }

            for ( org.apache.maven.model.Dependency dependency : model.getDependencies() )
            {
                result.addDependency( convert( dependency, stereotypes ) );
            }

            DependencyManagement mngt = model.getDependencyManagement();
            if ( mngt != null )
            {
                for ( org.apache.maven.model.Dependency dependency : mngt.getDependencies() )
                {
                    result.addManagedDependency( convert( dependency, stereotypes ) );
                }
            }

            Map<String, Object> properties = new LinkedHashMap<String, Object>();

            List<License> licenses = model.getLicenses();
            properties.put( "license.count", Integer.valueOf( licenses.size() ) );
            for ( int i = 0; i < licenses.size(); i++ )
            {
                License license = licenses.get( i );
                properties.put( "license." + i + ".name", license.getName() );
                properties.put( "license." + i + ".url", license.getUrl() );
                properties.put( "license." + i + ".comments", license.getComments() );
                properties.put( "license." + i + ".distribution", license.getDistribution() );
            }

            result.setProperties( properties );
        }

        return result;
    }

    private Model loadPom( RepositorySession session, ArtifactDescriptorRequest request, ArtifactDescriptorResult result )
        throws ArtifactDescriptorException
    {
        Set<String> visited = new LinkedHashSet<String>();
        for ( Artifact artifact = request.getArtifact();; )
        {
            try
            {
                VersionRequest versionRequest =
                    new VersionRequest( artifact, request.getRepositories(), request.getContext() );
                versionResolver.resolveVersion( session, versionRequest );
            }
            catch ( VersionResolutionException e )
            {
                result.addException( e );
                throw new ArtifactDescriptorException( result );
            }

            if ( !visited.add( artifact.getGroupId() + ':' + artifact.getArtifactId() + ':' + artifact.getBaseVersion() ) )
            {
                RepositoryException exception =
                    new RepositoryException( "Artifact relocations form a cycle: " + visited );
                invalidDescriptor( session, artifact, exception );
                if ( session.isIgnoreInvalidArtifactDescriptor() )
                {
                    return null;
                }
                result.addException( exception );
                throw new ArtifactDescriptorException( result );
            }

            Artifact pomArtifact = new DefaultSubArtifact( artifact, "", "pom" );

            ArtifactResult resolveResult;
            try
            {
                ArtifactRequest resolveRequest =
                    new ArtifactRequest( pomArtifact, request.getRepositories(), request.getContext() );
                resolveResult = artifactResolver.resolveArtifact( session, resolveRequest );
                result.setRepository( resolveResult.getRepository() );
            }
            catch ( ArtifactResolutionException e )
            {
                if ( e.getCause() instanceof ArtifactNotFoundException )
                {
                    missingDescriptor( session, artifact );
                    if ( session.isIgnoreMissingArtifactDescriptor() )
                    {
                        return null;
                    }
                }
                result.addException( e );
                throw new ArtifactDescriptorException( result );
            }

            Model model;
            try
            {
                ModelBuildingRequest modelRequest = new DefaultModelBuildingRequest();
                modelRequest.setValidationLevel( ModelBuildingRequest.VALIDATION_LEVEL_MINIMAL );
                modelRequest.setProcessPlugins( false );
                modelRequest.setTwoPhaseBuilding( false );
                modelRequest.setSystemProperties( session.getSystemProperties() );
                modelRequest.setUserProperties( session.getUserProperties() );
                modelRequest.setModelResolver( new DefaultModelResolver( session, request.getContext(),
                                                                         artifactResolver, remoteRepositoryManager,
                                                                         request.getRepositories() ) );
                if ( resolveResult.getRepository() instanceof WorkspaceRepository )
                {
                    modelRequest.setPomFile( pomArtifact.getFile() );
                }
                else
                {
                    modelRequest.setModelSource( new FileModelSource( pomArtifact.getFile() ) );
                }

                model = modelBuilder.build( modelRequest ).getEffectiveModel();
            }
            catch ( ModelBuildingException e )
            {
                for ( ModelProblem problem : e.getProblems() )
                {
                    if ( problem.getException() instanceof UnresolvableModelException )
                    {
                        result.addException( problem.getException() );
                        throw new ArtifactDescriptorException( result );
                    }
                }
                invalidDescriptor( session, artifact, e );
                if ( session.isIgnoreInvalidArtifactDescriptor() )
                {
                    return null;
                }
                result.addException( e );
                throw new ArtifactDescriptorException( result );
            }

            Relocation relocation = getRelocation( model );

            if ( relocation != null )
            {
                result.addRelocation( artifact );
                artifact =
                    new RelocatedArtifact( artifact, relocation.getGroupId(), relocation.getArtifactId(),
                                           relocation.getVersion() );
                result.setArtifact( artifact );
            }
            else
            {
                return model;
            }
        }
    }

    private Relocation getRelocation( Model model )
    {
        Relocation relocation = null;
        DistributionManagement distMngt = model.getDistributionManagement();
        if ( distMngt != null )
        {
            relocation = distMngt.getRelocation();
        }
        return relocation;
    }

    private Dependency convert( org.apache.maven.model.Dependency dependency, ArtifactStereotypeManager stereotypes )
    {
        ArtifactStereotype stereotype = stereotypes.get( dependency.getType() );
        if ( stereotype == null )
        {
            stereotype = new DefaultArtifactStereotype( dependency.getType() );
        }

        DefaultArtifact artifact =
            new DefaultArtifact( dependency.getGroupId(), dependency.getArtifactId(), dependency.getClassifier(), null,
                                 dependency.getVersion(), stereotype );

        if ( dependency.getSystemPath() != null )
        {
            artifact.setFile( new File( dependency.getSystemPath() ) );
        }

        Dependency result = new Dependency( artifact, dependency.getScope(), dependency.isOptional() );

        for ( org.apache.maven.model.Exclusion exclusion : dependency.getExclusions() )
        {
            result.addExclusion( convert( exclusion ) );
        }

        return result;
    }

    private Exclusion convert( org.apache.maven.model.Exclusion exclusion )
    {
        return new Exclusion( exclusion.getGroupId(), exclusion.getArtifactId(), "*", "*" );
    }

    static RemoteRepository convert( Repository repository )
    {
        RemoteRepository result =
            new RemoteRepository( repository.getId(), repository.getLayout(), repository.getUrl() );
        result.setPolicy( true, convert( repository.getSnapshots() ) );
        result.setPolicy( false, convert( repository.getReleases() ) );
        return result;
    }

    private static RepositoryPolicy convert( org.apache.maven.model.RepositoryPolicy policy )
    {
        RepositoryPolicy result = new RepositoryPolicy();
        if ( policy != null )
        {
            result.setEnabled( policy.isEnabled() );
            result.setUpdatePolicy( policy.getUpdatePolicy() );
            result.setChecksumPolicy( policy.getChecksumPolicy() );
        }
        return result;
    }

    private void missingDescriptor( RepositorySession session, Artifact artifact )
    {
        RepositoryListener listener = session.getRepositoryListener();
        if ( listener != null )
        {
            DefaultRepositoryEvent event = new DefaultRepositoryEvent( session, artifact );
            listener.artifactDescriptorMissing( event );
        }
    }

    private void invalidDescriptor( RepositorySession session, Artifact artifact, Exception exception )
    {
        RepositoryListener listener = session.getRepositoryListener();
        if ( listener != null )
        {
            DefaultRepositoryEvent event = new DefaultRepositoryEvent( session, artifact );
            event.setException( exception );
            listener.artifactDescriptorInvalid( event );
        }
    }

}
