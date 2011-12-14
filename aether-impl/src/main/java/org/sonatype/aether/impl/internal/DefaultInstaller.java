package org.sonatype.aether.impl.internal;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import java.io.File;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.aether.RepositoryEvent.EventType;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.RequestTrace;
import org.sonatype.aether.SyncContext;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.impl.Installer;
import org.sonatype.aether.impl.MetadataGenerator;
import org.sonatype.aether.impl.MetadataGeneratorFactory;
import org.sonatype.aether.impl.RepositoryEventDispatcher;
import org.sonatype.aether.impl.SyncContextFactory;
import org.sonatype.aether.installation.InstallRequest;
import org.sonatype.aether.installation.InstallResult;
import org.sonatype.aether.installation.InstallationException;
import org.sonatype.aether.metadata.MergeableMetadata;
import org.sonatype.aether.metadata.Metadata;
import org.sonatype.aether.repository.LocalArtifactRegistration;
import org.sonatype.aether.repository.LocalMetadataRegistration;
import org.sonatype.aether.repository.LocalRepositoryManager;
import org.sonatype.aether.spi.io.FileProcessor;
import org.sonatype.aether.spi.locator.Service;
import org.sonatype.aether.spi.locator.ServiceLocator;
import org.sonatype.aether.spi.log.Logger;
import org.sonatype.aether.spi.log.NullLogger;
import org.sonatype.aether.util.DefaultRequestTrace;
import org.sonatype.aether.util.listener.DefaultRepositoryEvent;

/**
 * @author Benjamin Bentmann
 */
@Component( role = Installer.class )
public class DefaultInstaller
    implements Installer, Service
{

    @Requirement
    private Logger logger = NullLogger.INSTANCE;

    @Requirement
    private FileProcessor fileProcessor;

    @Requirement
    private RepositoryEventDispatcher repositoryEventDispatcher;

    @Requirement( role = MetadataGeneratorFactory.class )
    private List<MetadataGeneratorFactory> metadataFactories = new ArrayList<MetadataGeneratorFactory>();

    @Requirement
    private SyncContextFactory syncContextFactory;

    public DefaultInstaller()
    {
        // enables default constructor
    }

    public DefaultInstaller( Logger logger, FileProcessor fileProcessor,
                             RepositoryEventDispatcher repositoryEventDispatcher,
                             List<MetadataGeneratorFactory> metadataFactories, SyncContextFactory syncContextFactory )
    {
        setLogger( logger );
        setFileProcessor( fileProcessor );
        setRepositoryEventDispatcher( repositoryEventDispatcher );
        setMetadataFactories( metadataFactories );
        setSyncContextFactory( syncContextFactory );
    }

    public void initService( ServiceLocator locator )
    {
        setLogger( locator.getService( Logger.class ) );
        setFileProcessor( locator.getService( FileProcessor.class ) );
        setRepositoryEventDispatcher( locator.getService( RepositoryEventDispatcher.class ) );
        setMetadataFactories( locator.getServices( MetadataGeneratorFactory.class ) );
        setSyncContextFactory( locator.getService( SyncContextFactory.class ) );
    }

    public DefaultInstaller setLogger( Logger logger )
    {
        this.logger = ( logger != null ) ? logger : NullLogger.INSTANCE;
        return this;
    }

    public DefaultInstaller setFileProcessor( FileProcessor fileProcessor )
    {
        if ( fileProcessor == null )
        {
            throw new IllegalArgumentException( "file processor has not been specified" );
        }
        this.fileProcessor = fileProcessor;
        return this;
    }

    public DefaultInstaller setRepositoryEventDispatcher( RepositoryEventDispatcher repositoryEventDispatcher )
    {
        if ( repositoryEventDispatcher == null )
        {
            throw new IllegalArgumentException( "repository event dispatcher has not been specified" );
        }
        this.repositoryEventDispatcher = repositoryEventDispatcher;
        return this;
    }

    public DefaultInstaller addMetadataGeneratorFactory( MetadataGeneratorFactory factory )
    {
        if ( factory == null )
        {
            throw new IllegalArgumentException( "metadata generator factory has not been specified" );
        }
        metadataFactories.add( factory );
        return this;
    }

    public DefaultInstaller setMetadataFactories( List<MetadataGeneratorFactory> metadataFactories )
    {
        if ( metadataFactories == null )
        {
            this.metadataFactories = new ArrayList<MetadataGeneratorFactory>();
        }
        else
        {
            this.metadataFactories = metadataFactories;
        }
        return this;
    }

    public DefaultInstaller setSyncContextFactory( SyncContextFactory syncContextFactory )
    {
        if ( syncContextFactory == null )
        {
            throw new IllegalArgumentException( "sync context factory has not been specified" );
        }
        this.syncContextFactory = syncContextFactory;
        return this;
    }

    public InstallResult install( RepositorySystemSession session, InstallRequest request )
        throws InstallationException
    {
        SyncContext syncContext = syncContextFactory.newInstance( session, false );

        try
        {
            return install( syncContext, session, request );
        }
        finally
        {
            syncContext.release();
        }
    }

    private InstallResult install( SyncContext syncContext, RepositorySystemSession session, InstallRequest request )
        throws InstallationException
    {
        InstallResult result = new InstallResult( request );

        RequestTrace trace = DefaultRequestTrace.newChild( request.getTrace(), request );

        List<MetadataGenerator> generators = getMetadataGenerators( session, request );

        List<Artifact> artifacts = new ArrayList<Artifact>( request.getArtifacts() );

        IdentityHashMap<Metadata, Object> processedMetadata = new IdentityHashMap<Metadata, Object>();

        List<Metadata> metadatas = Utils.prepareMetadata( generators, artifacts );

        syncContext.acquire( artifacts, Utils.combine( request.getMetadata(), metadatas ) );

        for ( Metadata metadata : metadatas )
        {
            install( session, trace, metadata );
            processedMetadata.put( metadata, null );
            result.addMetadata( metadata );
        }

        for ( int i = 0; i < artifacts.size(); i++ )
        {
            Artifact artifact = artifacts.get( i );

            for ( MetadataGenerator generator : generators )
            {
                artifact = generator.transformArtifact( artifact );
            }

            artifacts.set( i, artifact );

            install( session, trace, artifact );
            result.addArtifact( artifact );
        }

        metadatas = Utils.finishMetadata( generators, artifacts );

        syncContext.acquire( null, metadatas );

        for ( Metadata metadata : metadatas )
        {
            install( session, trace, metadata );
            processedMetadata.put( metadata, null );
            result.addMetadata( metadata );
        }

        for ( Metadata metadata : request.getMetadata() )
        {
            if ( !processedMetadata.containsKey( metadata ) )
            {
                install( session, trace, metadata );
                result.addMetadata( metadata );
            }
        }

        return result;
    }

    private List<MetadataGenerator> getMetadataGenerators( RepositorySystemSession session, InstallRequest request )
    {
        List<MetadataGeneratorFactory> factories = Utils.sortMetadataGeneratorFactories( this.metadataFactories );

        List<MetadataGenerator> generators = new ArrayList<MetadataGenerator>();

        for ( MetadataGeneratorFactory factory : factories )
        {
            MetadataGenerator generator = factory.newInstance( session, request );
            if ( generator != null )
            {
                generators.add( generator );
            }
        }

        return generators;
    }

    private void install( RepositorySystemSession session, RequestTrace trace, Artifact artifact )
        throws InstallationException
    {
        LocalRepositoryManager lrm = session.getLocalRepositoryManager();

        File srcFile = artifact.getFile();
        if ( srcFile != null && !srcFile.exists() )
        {
            throw new InstallationException( "File does not exist: " + srcFile.getAbsolutePath() );
        }

        File dstFile = new File( lrm.getRepository().getBasedir(), lrm.getPathForLocalArtifact( artifact ) );

        artifactInstalling( session, trace, artifact, dstFile );

        Exception exception = null;
        try
        {
            boolean copy =
                "pom".equals( artifact.getExtension() ) || srcFile.lastModified() != dstFile.lastModified()
                    || srcFile.length() != dstFile.length();

            if ( copy )
            {
                fileProcessor.copy( srcFile, dstFile, null );
                dstFile.setLastModified( srcFile.lastModified() );
            }
            else
            {
                logger.debug( "Skipped re-installing " + srcFile + " to " + dstFile + ", seems unchanged" );
            }

            lrm.add( session, new LocalArtifactRegistration( artifact ) );
        }
        catch ( Exception e )
        {
            exception = e;
            throw new InstallationException( "Failed to install artifact " + artifact + ": " + e.getMessage(), e );
        }
        finally
        {
            artifactInstalled( session, trace, artifact, dstFile, exception );
        }
    }

    private void install( RepositorySystemSession session, RequestTrace trace, Metadata metadata )
        throws InstallationException
    {
        LocalRepositoryManager lrm = session.getLocalRepositoryManager();

        File dstFile = new File( lrm.getRepository().getBasedir(), lrm.getPathForLocalMetadata( metadata ) );

        metadataInstalling( session, trace, metadata, dstFile );

        Exception exception = null;
        try
        {
            if ( metadata instanceof MergeableMetadata )
            {
                ( (MergeableMetadata) metadata ).merge( dstFile, dstFile );
            }
            else
            {
                fileProcessor.copy( metadata.getFile(), dstFile, null );
            }

            lrm.add( session, new LocalMetadataRegistration( metadata ) );
        }
        catch ( Exception e )
        {
            exception = e;
            throw new InstallationException( "Failed to install metadata " + metadata + ": " + e.getMessage(), e );
        }
        finally
        {
            metadataInstalled( session, trace, metadata, dstFile, exception );
        }
    }

    private void artifactInstalling( RepositorySystemSession session, RequestTrace trace, Artifact artifact,
                                     File dstFile )
    {
        DefaultRepositoryEvent event = new DefaultRepositoryEvent( EventType.ARTIFACT_INSTALLING, session, trace );
        event.setArtifact( artifact );
        event.setRepository( session.getLocalRepositoryManager().getRepository() );
        event.setFile( dstFile );

        repositoryEventDispatcher.dispatch( event );
    }

    private void artifactInstalled( RepositorySystemSession session, RequestTrace trace, Artifact artifact,
                                    File dstFile, Exception exception )
    {
        DefaultRepositoryEvent event = new DefaultRepositoryEvent( EventType.ARTIFACT_INSTALLED, session, trace );
        event.setArtifact( artifact );
        event.setRepository( session.getLocalRepositoryManager().getRepository() );
        event.setFile( dstFile );
        event.setException( exception );

        repositoryEventDispatcher.dispatch( event );
    }

    private void metadataInstalling( RepositorySystemSession session, RequestTrace trace, Metadata metadata,
                                     File dstFile )
    {
        DefaultRepositoryEvent event = new DefaultRepositoryEvent( EventType.METADATA_INSTALLING, session, trace );
        event.setMetadata( metadata );
        event.setRepository( session.getLocalRepositoryManager().getRepository() );
        event.setFile( dstFile );

        repositoryEventDispatcher.dispatch( event );
    }

    private void metadataInstalled( RepositorySystemSession session, RequestTrace trace, Metadata metadata,
                                    File dstFile, Exception exception )
    {
        DefaultRepositoryEvent event = new DefaultRepositoryEvent( EventType.METADATA_INSTALLED, session, trace );
        event.setMetadata( metadata );
        event.setRepository( session.getLocalRepositoryManager().getRepository() );
        event.setFile( dstFile );
        event.setException( exception );

        repositoryEventDispatcher.dispatch( event );
    }

}
