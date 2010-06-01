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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.repository.Artifact;
import org.apache.maven.repository.InstallRequest;
import org.apache.maven.repository.InstallationException;
import org.apache.maven.repository.LocalRepositoryManager;
import org.apache.maven.repository.MergeableMetadata;
import org.apache.maven.repository.Metadata;
import org.apache.maven.repository.RepositoryListener;
import org.apache.maven.repository.RepositorySession;
import org.apache.maven.repository.spi.Installer;
import org.apache.maven.repository.spi.Logger;
import org.apache.maven.repository.spi.NullLogger;
import org.apache.maven.repository.util.DefaultRepositoryEvent;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.FileUtils;

/**
 * @author Benjamin Bentmann
 */
@Component( role = Installer.class )
public class DefaultInstaller
    implements Installer
{

    @Requirement
    private Logger logger = NullLogger.INSTANCE;

    public DefaultInstaller setLogger( Logger logger )
    {
        this.logger = ( logger != null ) ? logger : NullLogger.INSTANCE;
        return this;
    }

    public void install( RepositorySession session, InstallRequest request )
        throws InstallationException
    {
        for ( Artifact artifact : request.getArtifacts() )
        {
            install( session, artifact );
        }

        for ( Metadata metadata : request.getMetadata() )
        {
            install( session, metadata );
        }
    }

    private void install( RepositorySession session, Artifact artifact )
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
                "pom".equals( artifact.getType() ) || !dstFile.exists()
                    || srcFile.lastModified() != dstFile.lastModified() || srcFile.length() != dstFile.length();

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

    private void install( RepositorySession session, Metadata metadata )
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

    private void artifactInstalling( RepositorySession session, Artifact artifact, File dstFile )
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

    private void artifactInstalled( RepositorySession session, Artifact artifact, File dstFile, Exception exception )
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

    private void metadataInstalling( RepositorySession session, Metadata metadata, File dstFile )
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

    private void metadataInstalled( RepositorySession session, Metadata metadata, File dstFile, Exception exception )
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
