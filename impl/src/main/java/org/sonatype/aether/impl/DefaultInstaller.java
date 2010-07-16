package org.sonatype.aether.impl;

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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.FileUtils;
import org.sonatype.aether.Artifact;
import org.sonatype.aether.InstallRequest;
import org.sonatype.aether.InstallResult;
import org.sonatype.aether.InstallationException;
import org.sonatype.aether.LocalRepositoryManager;
import org.sonatype.aether.MergeableMetadata;
import org.sonatype.aether.Metadata;
import org.sonatype.aether.RepositoryListener;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.spi.Installer;
import org.sonatype.aether.spi.LocalRepositoryMaintainer;
import org.sonatype.aether.spi.Logger;
import org.sonatype.aether.spi.NullLogger;
import org.sonatype.aether.spi.Service;
import org.sonatype.aether.spi.ServiceLocator;
import org.sonatype.aether.util.DefaultRepositoryEvent;

/**
 * @author Benjamin Bentmann
 */
@Component( role = Installer.class )
public class DefaultInstaller
    implements Installer, Service
{

    @Requirement
    private Logger logger = NullLogger.INSTANCE;

    @Requirement( optional = true )
    private LocalRepositoryMaintainer maintainer;

    public void initService( ServiceLocator locator )
    {
        setLogger( locator.getService( Logger.class ) );
        setLocalRepositoryMaintainer( locator.getService( LocalRepositoryMaintainer.class ) );
    }

    public DefaultInstaller setLogger( Logger logger )
    {
        this.logger = ( logger != null ) ? logger : NullLogger.INSTANCE;
        return this;
    }

    public DefaultInstaller setLocalRepositoryMaintainer( LocalRepositoryMaintainer maintainer )
    {
        this.maintainer = maintainer;
        return this;
    }

    public InstallResult install( RepositorySystemSession session, InstallRequest request )
        throws InstallationException
    {
        InstallResult result = new InstallResult( request );

        for ( Artifact artifact : request.getArtifacts() )
        {
            install( session, artifact );
            result.addArtifact( artifact );
        }

        for ( Metadata metadata : request.getMetadata() )
        {
            install( session, metadata );
            result.addMetadata( metadata );
        }

        return result;
    }

    private void install( RepositorySystemSession session, Artifact artifact )
        throws InstallationException
    {
        LocalRepositoryManager lrm = session.getLocalRepositoryManager();

        File srcFile = artifact.getFile();
        File dstFile = new File( lrm.getRepository().getBasedir(), lrm.getPathForLocalArtifact( artifact ) );

        artifactInstalling( session, artifact, dstFile );

        Exception exception = null;
        try
        {
            boolean copy =
                "pom".equals( artifact.getExtension() ) || srcFile.lastModified() != dstFile.lastModified()
                    || srcFile.length() != dstFile.length();

            if ( copy )
            {
                if ( !dstFile.getParentFile().exists() )
                {
                    dstFile.getParentFile().mkdirs();
                }

                FileUtils.copyFile( srcFile, dstFile );
                dstFile.setLastModified( srcFile.lastModified() );
            }
            else
            {
                logger.debug( "Skipped re-installing " + srcFile + " to " + dstFile + ", seems unchanged" );
            }

            lrm.addLocalArtifact( artifact );

            if ( maintainer != null )
            {
                maintainer.artifactInstalled( new DefaultLocalRepositoryEvent( session, artifact ) );
            }
        }
        catch ( IOException e )
        {
            exception = e;
            throw new InstallationException( "Failed to install artifact " + artifact + ": " + e.getMessage(), e );
        }
        finally
        {
            artifactInstalled( session, artifact, dstFile, exception );
        }

        List<MergeableMetadata> versionMetadata = generateVersionMetadata( artifact );
        for ( MergeableMetadata metadata : versionMetadata )
        {
            install( session, metadata );
        }
    }

    private void install( RepositorySystemSession session, Metadata metadata )
        throws InstallationException
    {
        LocalRepositoryManager lrm = session.getLocalRepositoryManager();

        File dstFile = new File( lrm.getRepository().getBasedir(), lrm.getPathForLocalMetadata( metadata ) );

        metadataInstalling( session, metadata, dstFile );

        Exception exception = null;
        try
        {
            if ( metadata instanceof MergeableMetadata )
            {
                ( (MergeableMetadata) metadata ).merge( dstFile, dstFile );
            }
            else
            {
                FileUtils.copyFile( metadata.getFile(), dstFile );
            }
        }
        catch ( Exception e )
        {
            exception = e;
            throw new InstallationException( "Failed to install metadata " + metadata + ": " + e.getMessage(), e );
        }
        finally
        {
            metadataInstalled( session, metadata, dstFile, exception );
        }
    }

    private List<MergeableMetadata> generateVersionMetadata( Artifact artifact )
    {
        List<MergeableMetadata> result = new ArrayList<MergeableMetadata>();

        result.add( new VersionsMetadata( artifact ) );
        if ( artifact.isSnapshot() )
        {
            result.add( new LocalSnapshotMetadata( artifact ) );
        }

        return result;
    }

    private void artifactInstalling( RepositorySystemSession session, Artifact artifact, File dstFile )
    {
        RepositoryListener listener = session.getRepositoryListener();
        if ( listener != null )
        {
            DefaultRepositoryEvent event = new DefaultRepositoryEvent( session, artifact );
            event.setRepository( session.getLocalRepositoryManager().getRepository() );
            event.setFile( dstFile );
            listener.artifactInstalling( event );
        }
    }

    private void artifactInstalled( RepositorySystemSession session, Artifact artifact, File dstFile,
                                    Exception exception )
    {
        RepositoryListener listener = session.getRepositoryListener();
        if ( listener != null )
        {
            DefaultRepositoryEvent event = new DefaultRepositoryEvent( session, artifact );
            event.setRepository( session.getLocalRepositoryManager().getRepository() );
            event.setFile( dstFile );
            event.setException( exception );
            listener.artifactInstalled( event );
        }
    }

    private void metadataInstalling( RepositorySystemSession session, Metadata metadata, File dstFile )
    {
        RepositoryListener listener = session.getRepositoryListener();
        if ( listener != null )
        {
            DefaultRepositoryEvent event = new DefaultRepositoryEvent( session, metadata );
            event.setRepository( session.getLocalRepositoryManager().getRepository() );
            event.setFile( dstFile );
            listener.metadataInstalling( event );
        }
    }

    private void metadataInstalled( RepositorySystemSession session, Metadata metadata, File dstFile,
                                    Exception exception )
    {
        RepositoryListener listener = session.getRepositoryListener();
        if ( listener != null )
        {
            DefaultRepositoryEvent event = new DefaultRepositoryEvent( session, metadata );
            event.setRepository( session.getLocalRepositoryManager().getRepository() );
            event.setFile( dstFile );
            event.setException( exception );
            listener.metadataInstalled( event );
        }
    }

}
