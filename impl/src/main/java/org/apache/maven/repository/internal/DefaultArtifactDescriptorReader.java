package org.apache.maven.repository.internal;

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
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.DistributionManagement;
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
import org.apache.maven.repository.Artifact;
import org.apache.maven.repository.ArtifactDescriptorException;
import org.apache.maven.repository.ArtifactResolutionException;
import org.apache.maven.repository.ArtifactStereotype;
import org.apache.maven.repository.ArtifactStereotypeManager;
import org.apache.maven.repository.DefaultArtifact;
import org.apache.maven.repository.RepositoryException;
import org.apache.maven.repository.SubArtifact;
import org.apache.maven.repository.ArtifactDescriptorRequest;
import org.apache.maven.repository.ArtifactDescriptorResult;
import org.apache.maven.repository.Dependency;
import org.apache.maven.repository.Exclusion;
import org.apache.maven.repository.RemoteRepository;
import org.apache.maven.repository.RepositoryContext;
import org.apache.maven.repository.RepositoryPolicy;
import org.apache.maven.repository.ArtifactRequest;
import org.apache.maven.repository.ArtifactResult;
import org.apache.maven.repository.WorkspaceRepository;
import org.apache.maven.repository.spi.ArtifactResolver;
import org.apache.maven.repository.spi.Logger;
import org.apache.maven.repository.spi.NullLogger;
import org.apache.maven.repository.spi.ArtifactDescriptorReader;
import org.apache.maven.repository.util.DefaultArtifactStereotype;
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
    private ArtifactResolver artifactResolver;

    @Requirement
    private ModelBuilder modelBuilder;

    public DefaultArtifactDescriptorReader setLogger( Logger logger )
    {
        this.logger = ( logger != null ) ? logger : NullLogger.INSTANCE;
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

    public ArtifactDescriptorResult readArtifactDescriptor( RepositoryContext context, ArtifactDescriptorRequest request )
        throws ArtifactDescriptorException
    {
        ArtifactDescriptorResult result = new ArtifactDescriptorResult( request );

        Model model = loadPom( context, request, result );

        if ( model != null )
        {
            ArtifactStereotypeManager stereotypes = context.getArtifactStereotypeManager();

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
        }

        return result;
    }

    private Model loadPom( RepositoryContext context, ArtifactDescriptorRequest request, ArtifactDescriptorResult result )
        throws ArtifactDescriptorException
    {
        Set<String> visited = new LinkedHashSet<String>();
        for ( Artifact artifact = request.getArtifact();; )
        {
            if ( !visited.add( artifact.getGroupId() + ':' + artifact.getArtifactId() + ':' + artifact.getBaseVersion() ) )
            {
                if ( context.isIgnoreInvalidArtifactDescriptor() )
                {
                    return null;
                }
                result.addException( new RepositoryException( "Artifact relocations form a cycle: " + visited ) );
                throw new ArtifactDescriptorException( result );
            }

            Artifact pomArtifact = new SubArtifact( artifact, "", "pom" );

            ArtifactResult resolveResult;
            try
            {
                ArtifactRequest resolveRequest = new ArtifactRequest( pomArtifact, request.getRemoteRepositories() );
                resolveResult = artifactResolver.resolveArtifact( context, resolveRequest );
                result.setRepository( resolveResult.getRepository() );
            }
            catch ( ArtifactResolutionException e )
            {
                if ( context.isIgnoreMissingArtifactDescriptor() )
                {
                    return null;
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
                modelRequest.setSystemProperties( context.getSystemProperties() );
                modelRequest.setUserProperties( context.getUserProperties() );
                modelRequest.setModelResolver( new DefaultModelResolver( context, artifactResolver,
                                                                         request.getRemoteRepositories() ) );
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
                if ( context.isIgnoreInvalidArtifactDescriptor() )
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

    private RemoteRepository convert( Repository repository )
    {
        RemoteRepository result =
            new RemoteRepository( repository.getId(), repository.getLayout(), repository.getUrl() );
        result.setPolicy( true, convert( repository.getSnapshots() ) );
        result.setPolicy( false, convert( repository.getReleases() ) );
        return result;
    }

    private RepositoryPolicy convert( org.apache.maven.model.RepositoryPolicy policy )
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

}
