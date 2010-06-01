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
import java.util.Arrays;
import java.util.List;

import org.apache.maven.repository.Artifact;
import org.apache.maven.repository.DeployRequest;
import org.apache.maven.repository.DeploymentException;
import org.apache.maven.repository.LocalRepositoryManager;
import org.apache.maven.repository.MergeableMetadata;
import org.apache.maven.repository.Metadata;
import org.apache.maven.repository.MetadataNotFoundException;
import org.apache.maven.repository.MetadataTransferException;
import org.apache.maven.repository.NoRepositoryConnectorException;
import org.apache.maven.repository.RemoteRepository;
import org.apache.maven.repository.RepositoryException;
import org.apache.maven.repository.RepositoryListener;
import org.apache.maven.repository.RepositoryPolicy;
import org.apache.maven.repository.RepositorySession;
import org.apache.maven.repository.SubArtifact;
import org.apache.maven.repository.spi.ArtifactUpload;
import org.apache.maven.repository.spi.Deployer;
import org.apache.maven.repository.spi.Logger;
import org.apache.maven.repository.spi.MetadataDownload;
import org.apache.maven.repository.spi.MetadataUpload;
import org.apache.maven.repository.spi.NullLogger;
import org.apache.maven.repository.spi.RemoteRepositoryManager;
import org.apache.maven.repository.spi.RepositoryConnector;
import org.apache.maven.repository.spi.Transfer;
import org.apache.maven.repository.spi.UpdateCheck;
import org.apache.maven.repository.spi.UpdateCheckManager;
import org.apache.maven.repository.util.DefaultRepositoryEvent;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.FileUtils;

/**
 * @author Benjamin Bentmann
 */
@Component( role = Deployer.class )
public class DefaultDeployer
    implements Deployer
{

    @Requirement
    private Logger logger = NullLogger.INSTANCE;

    @Requirement
    private RemoteRepositoryManager remoteRepositoryManager;

    @Requirement
    private UpdateCheckManager updateCheckManager;

    public DefaultDeployer setLogger( Logger logger )
    {
        this.logger = ( logger != null ) ? logger : NullLogger.INSTANCE;
        return this;
    }

    public DefaultDeployer setRemoteRepositoryManager( RemoteRepositoryManager remoteRepositoryManager )
    {
        if ( remoteRepositoryManager == null )
        {
            throw new IllegalArgumentException( "remote repository manager has not been specified" );
        }
        this.remoteRepositoryManager = remoteRepositoryManager;
        return this;
    }

    public DefaultDeployer setUpdateCheckManager( UpdateCheckManager updateCheckManager )
    {
        if ( updateCheckManager == null )
        {
            throw new IllegalArgumentException( "update check manager has not been specified" );
        }
        this.updateCheckManager = updateCheckManager;
        return this;
    }

    public void deploy( RepositorySession session, DeployRequest request )
        throws DeploymentException
    {
        if ( session.isOffline() )
        {
            throw new DeploymentException( "The repository system is in offline mode, artifact deployment impossible" );
        }

        RemoteRepository repository = request.getRepository();

        RepositoryConnector connector;
        try
        {
            connector = remoteRepositoryManager.getRepositoryConnector( session, repository );
        }
        catch ( NoRepositoryConnectorException e )
        {
            throw new DeploymentException( "Failed to deploy artifacts/metadata: " + e.getMessage(), e );
        }

        try
        {
            List<ArtifactUpload> artifactUploads = new ArrayList<ArtifactUpload>();
            List<MetadataUpload> metadataUploads = new ArrayList<MetadataUpload>();

            EventCatapult catapult = new EventCatapult( session, repository );

            for ( Artifact artifact : request.getArtifacts() )
            {
                upload( metadataUploads, session, new VersionsMetadata( artifact ), repository, connector, catapult );

                if ( artifact.isSnapshot() && !( artifact instanceof SubArtifact ) )
                {
                    RemoteSnapshotMetadata snapshotMetadata = new RemoteSnapshotMetadata( artifact );
                    upload( metadataUploads, session, snapshotMetadata, repository, connector, catapult );
                    artifact.setVersion( snapshotMetadata.getExpandedVersion() );
                }

                artifactUploads.add( new ArtifactUploadEx( artifact, artifact.getFile(), catapult ) );
            }

            for ( Metadata metadata : request.getMetadata() )
            {
                upload( metadataUploads, session, metadata, repository, connector, catapult );
            }

            connector.put( artifactUploads, metadataUploads );

            for ( ArtifactUpload upload : artifactUploads )
            {
                if ( upload.getException() != null )
                {
                    throw new DeploymentException( "Failed to deploy artifacts/metadata: "
                        + upload.getException().getMessage(), upload.getException() );
                }
            }
            for ( MetadataUpload upload : metadataUploads )
            {
                if ( upload.getException() != null )
                {
                    throw new DeploymentException( "Failed to deploy artifacts/metadata: "
                        + upload.getException().getMessage(), upload.getException() );
                }
            }
        }
        finally
        {
            connector.close();
        }
    }

    private void upload( List<MetadataUpload> metadataUploads, RepositorySession session, Metadata metadata,
                         RemoteRepository repository, RepositoryConnector connector, EventCatapult catapult )
        throws DeploymentException
    {
        LocalRepositoryManager lrm = session.getLocalRepositoryManager();
        File basedir = lrm.getRepository().getBasedir();

        File dstFile = new File( basedir, lrm.getPathForRemoteMetadata( metadata, repository, "" ) );

        if ( metadata instanceof MergeableMetadata )
        {
            RepositoryListener listener = session.getRepositoryListener();
            if ( listener != null )
            {
                DefaultRepositoryEvent event = new DefaultRepositoryEvent( session, metadata );
                event.setRepository( repository );
                listener.metadataResolving( event );
            }

            RepositoryPolicy policy = getPolicy( session, repository, metadata.getNature() );
            MetadataDownload download = new MetadataDownload( metadata, "", dstFile, policy.getChecksumPolicy() );
            connector.get( null, Arrays.asList( download ) );

            if ( listener != null )
            {
                DefaultRepositoryEvent event = new DefaultRepositoryEvent( session, metadata );
                event.setRepository( repository );
                event.setException( download.getException() );
                listener.metadataResolved( event );
            }

            Exception error = download.getException();
            if ( error != null && !( error instanceof MetadataNotFoundException ) )
            {
                throw new DeploymentException( "Failed to retrieve remote metadata " + metadata + ": "
                    + error.getMessage(), error );
            }

            try
            {
                ( (MergeableMetadata) metadata ).merge( dstFile, dstFile );
            }
            catch ( RepositoryException e )
            {
                throw new DeploymentException( "Failed to update metadata " + metadata + ": " + e.getMessage(), e );
            }
        }
        else
        {
            try
            {
                FileUtils.copyFile( metadata.getFile(), dstFile );
            }
            catch ( IOException e )
            {
                throw new DeploymentException( "Failed to update metadata " + metadata + ": " + e.getMessage(), e );
            }
        }

        UpdateCheck<Metadata, MetadataTransferException> check = new UpdateCheck<Metadata, MetadataTransferException>();
        check.setItem( metadata );
        check.setFile( dstFile );
        check.setRepository( repository );
        updateCheckManager.touchMetadata( session, check );

        metadataUploads.add( new MetadataUploadEx( metadata, dstFile, catapult ) );
    }

    private RepositoryPolicy getPolicy( RepositorySession session, RemoteRepository repository, Metadata.Nature nature )
    {
        boolean releases = !Metadata.Nature.SNAPSHOT.equals( nature );
        boolean snapshots = !Metadata.Nature.RELEASE.equals( nature );
        return remoteRepositoryManager.getPolicy( session, repository, releases, snapshots );
    }

    static class EventCatapult
    {

        private final RepositorySession session;

        private final RemoteRepository repository;

        public EventCatapult( RepositorySession session, RemoteRepository repository )
        {
            this.session = session;
            this.repository = repository;
        }

        public void artifactDeploying( Artifact artifact, File file )
        {
            RepositoryListener listener = session.getRepositoryListener();
            if ( listener != null )
            {
                DefaultRepositoryEvent event = new DefaultRepositoryEvent( session, artifact );
                event.setRepository( repository );
                event.setFile( file );
                listener.artifactDeploying( event );
            }
        }

        public void artifactDeployed( Artifact artifact, File file )
        {
            RepositoryListener listener = session.getRepositoryListener();
            if ( listener != null )
            {
                DefaultRepositoryEvent event = new DefaultRepositoryEvent( session, artifact );
                event.setRepository( repository );
                event.setFile( file );
                listener.artifactDeployed( event );
            }
        }

        public void metadataDeploying( Metadata metadata, File file )
        {
            RepositoryListener listener = session.getRepositoryListener();
            if ( listener != null )
            {
                DefaultRepositoryEvent event = new DefaultRepositoryEvent( session, metadata );
                event.setRepository( repository );
                event.setFile( file );
                listener.metadataDeploying( event );
            }
        }

        public void metadataDeployed( Metadata metadata, File file )
        {
            RepositoryListener listener = session.getRepositoryListener();
            if ( listener != null )
            {
                DefaultRepositoryEvent event = new DefaultRepositoryEvent( session, metadata );
                event.setRepository( repository );
                event.setFile( file );
                listener.metadataDeployed( event );
            }
        }

    }

    static class ArtifactUploadEx
        extends ArtifactUpload
    {

        private final EventCatapult catapult;

        public ArtifactUploadEx( Artifact artifact, File file, EventCatapult catapult )
        {
            super( artifact, file );
            this.catapult = catapult;
        }

        @Override
        public Transfer setState( State state )
        {
            super.setState( state );

            if ( State.ACTIVE.equals( state ) )
            {
                catapult.artifactDeploying( getArtifact(), getFile() );
            }
            else if ( State.DONE.equals( state ) )
            {
                catapult.artifactDeployed( getArtifact(), getFile() );
            }

            return this;
        }

    }

    static class MetadataUploadEx
        extends MetadataUpload
    {

        private final EventCatapult catapult;

        public MetadataUploadEx( Metadata metadata, File file, EventCatapult catapult )
        {
            super( metadata, file );
            this.catapult = catapult;
        }

        @Override
        public Transfer setState( State state )
        {
            super.setState( state );

            if ( State.ACTIVE.equals( state ) )
            {
                catapult.metadataDeploying( getMetadata(), getFile() );
            }
            else if ( State.DONE.equals( state ) )
            {
                catapult.metadataDeployed( getMetadata(), getFile() );
            }

            return this;
        }

    }

}
