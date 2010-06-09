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
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.maven.repository.Artifact;
import org.sonatype.maven.repository.ArtifactDescriptorException;
import org.sonatype.maven.repository.ArtifactDescriptorRequest;
import org.sonatype.maven.repository.ArtifactDescriptorResult;
import org.sonatype.maven.repository.ArtifactNotFoundException;
import org.sonatype.maven.repository.ArtifactRequest;
import org.sonatype.maven.repository.ArtifactResolutionException;
import org.sonatype.maven.repository.ArtifactResult;
import org.sonatype.maven.repository.ArtifactStereotype;
import org.sonatype.maven.repository.ArtifactStereotypeRegistry;
import org.sonatype.maven.repository.DefaultArtifact;
import org.sonatype.maven.repository.DefaultSubArtifact;
import org.sonatype.maven.repository.Dependency;
import org.sonatype.maven.repository.Exclusion;
import org.sonatype.maven.repository.RemoteRepository;
import org.sonatype.maven.repository.RepositoryException;
import org.sonatype.maven.repository.RepositoryListener;
import org.sonatype.maven.repository.RepositoryPolicy;
import org.sonatype.maven.repository.RepositorySession;
import org.sonatype.maven.repository.VersionRequest;
import org.sonatype.maven.repository.VersionResolutionException;
import org.sonatype.maven.repository.WorkspaceRepository;
import org.sonatype.maven.repository.spi.ArtifactDescriptorReader;
import org.sonatype.maven.repository.spi.ArtifactResolver;
import org.sonatype.maven.repository.spi.Logger;
import org.sonatype.maven.repository.spi.NullLogger;
import org.sonatype.maven.repository.spi.RemoteRepositoryManager;
import org.sonatype.maven.repository.spi.VersionResolver;
import org.sonatype.maven.repository.util.DefaultArtifactStereotype;
import org.sonatype.maven.repository.util.DefaultRepositoryEvent;

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
            ArtifactStereotypeRegistry stereotypes = session.getArtifactStereotypeRegistry();

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
                                                                         request.getRepositories(), null ) );
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

    private Dependency convert( org.apache.maven.model.Dependency dependency, ArtifactStereotypeRegistry stereotypes )
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
