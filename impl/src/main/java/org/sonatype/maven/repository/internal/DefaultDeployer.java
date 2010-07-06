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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.FileUtils;
import org.sonatype.maven.repository.Artifact;
import org.sonatype.maven.repository.DeployRequest;
import org.sonatype.maven.repository.DeployResult;
import org.sonatype.maven.repository.DeploymentException;
import org.sonatype.maven.repository.LocalRepositoryManager;
import org.sonatype.maven.repository.MergeableMetadata;
import org.sonatype.maven.repository.Metadata;
import org.sonatype.maven.repository.MetadataNotFoundException;
import org.sonatype.maven.repository.MetadataTransferException;
import org.sonatype.maven.repository.NoRepositoryConnectorException;
import org.sonatype.maven.repository.RemoteRepository;
import org.sonatype.maven.repository.RepositoryException;
import org.sonatype.maven.repository.RepositoryListener;
import org.sonatype.maven.repository.RepositoryPolicy;
import org.sonatype.maven.repository.RepositorySystemSession;
import org.sonatype.maven.repository.spi.ArtifactUpload;
import org.sonatype.maven.repository.spi.Deployer;
import org.sonatype.maven.repository.spi.Logger;
import org.sonatype.maven.repository.spi.MetadataDownload;
import org.sonatype.maven.repository.spi.MetadataUpload;
import org.sonatype.maven.repository.spi.NullLogger;
import org.sonatype.maven.repository.spi.RemoteRepositoryManager;
import org.sonatype.maven.repository.spi.RepositoryConnector;
import org.sonatype.maven.repository.spi.Transfer;
import org.sonatype.maven.repository.spi.UpdateCheck;
import org.sonatype.maven.repository.spi.UpdateCheckManager;
import org.sonatype.maven.repository.util.DefaultRepositoryEvent;

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

    public DeployResult deploy( RepositorySystemSession session, DeployRequest request )
        throws DeploymentException
    {
        DeployResult result = new DeployResult( request );

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

            Map<Object, RemoteSnapshotMetadata> snapshots = new LinkedHashMap<Object, RemoteSnapshotMetadata>();
            Collection<Object> versions = new HashSet<Object>();

            /*
             * NOTE: This should be considered a quirk to support interop with Maven's ArtifactDeployer which processes
             * one artifact at a time and hence cannot associate the artifacts from the same project to use the same
             * timestamp+buildno for the snapshot versions. Allowing the caller to pass in metadata from a previous
             * deployment allows to re-establish the association between the artifacts of the same project.
             */
            for ( Metadata metadata : request.getMetadata() )
            {
                if ( metadata instanceof RemoteSnapshotMetadata )
                {
                    RemoteSnapshotMetadata snapshotMetadata = (RemoteSnapshotMetadata) metadata;
                    snapshots.put( snapshotMetadata.getKey(), snapshotMetadata );
                }
                else if ( metadata instanceof VersionsMetadata )
                {
                    VersionsMetadata versionsMetadata = (VersionsMetadata) metadata;
                    versions.add( versionsMetadata.getKey() );
                }
            }

            for ( Artifact artifact : request.getArtifacts() )
            {
                if ( artifact.isSnapshot() )
                {
                    Object key = RemoteSnapshotMetadata.getKey( artifact );
                    RemoteSnapshotMetadata snapshotMetadata = snapshots.get( key );
                    if ( snapshotMetadata == null )
                    {
                        snapshotMetadata = new RemoteSnapshotMetadata( artifact );
                        snapshots.put( key, snapshotMetadata );
                    }
                    snapshotMetadata.bind( artifact );
                }
            }

            for ( RemoteSnapshotMetadata metadata : snapshots.values() )
            {
                upload( metadataUploads, session, metadata, repository, connector, catapult );
            }

            for ( Artifact artifact : request.getArtifacts() )
            {
                if ( artifact.isSnapshot() && artifact.getVersion().equals( artifact.getBaseVersion() ) )
                {
                    Object key = RemoteSnapshotMetadata.getKey( artifact );
                    RemoteSnapshotMetadata snapshotMetadata = snapshots.get( key );
                    artifact = artifact.setVersion( snapshotMetadata.getExpandedVersion( artifact ) );
                }

                Object key = VersionsMetadata.getKey( artifact );
                if ( versions.add( key ) )
                {
                    upload( metadataUploads, session, new VersionsMetadata( artifact ), repository, connector, catapult );
                }

                artifactUploads.add( new ArtifactUploadEx( artifact, artifact.getFile(), catapult ) );
            }

            for ( Metadata metadata : request.getMetadata() )
            {
                if ( !( metadata instanceof MavenMetadata ) )
                {
                    upload( metadataUploads, session, metadata, repository, connector, catapult );
                }
            }

            connector.put( artifactUploads, metadataUploads );

            for ( ArtifactUpload upload : artifactUploads )
            {
                if ( upload.getException() != null )
                {
                    throw new DeploymentException( "Failed to deploy artifacts: " + upload.getException().getMessage(),
                                                   upload.getException() );
                }
                result.addArtifact( upload.getArtifact() );
            }
            for ( MetadataUpload upload : metadataUploads )
            {
                if ( upload.getException() != null )
                {
                    throw new DeploymentException( "Failed to deploy metadata: " + upload.getException().getMessage(),
                                                   upload.getException() );
                }
                result.addMetadata( upload.getMetadata() );
            }
        }
        finally
        {
            connector.close();
        }

        return result;
    }

    private void upload( List<MetadataUpload> metadataUploads, RepositorySystemSession session, Metadata metadata,
                         RemoteRepository repository, RepositoryConnector connector, EventCatapult catapult )
        throws DeploymentException
    {
        LocalRepositoryManager lrm = session.getLocalRepositoryManager();
        File basedir = lrm.getRepository().getBasedir();

        File dstFile = new File( basedir, lrm.getPathForRemoteMetadata( metadata, repository, "" ) );

        if ( metadata instanceof RemoteSnapshotMetadata && ( (RemoteSnapshotMetadata) metadata ).isResolved() )
        {
            /*
             * NOTE: Continued quirk mode to allow reuse of already initialized metadata from previous deployment, no
             * need to refetch remote copy.
             */
            try
            {
                ( (MergeableMetadata) metadata ).merge( dstFile, dstFile );
            }
            catch ( RepositoryException e )
            {
                throw new DeploymentException( "Failed to update metadata " + metadata + ": " + e.getMessage(), e );
            }
        }
        else if ( metadata instanceof MergeableMetadata )
        {
            RepositoryListener listener = session.getRepositoryListener();
            if ( listener != null )
            {
                DefaultRepositoryEvent event = new DefaultRepositoryEvent( session, metadata );
                event.setRepository( repository );
                listener.metadataResolving( event );
            }

            RepositoryPolicy policy = getPolicy( session, repository, metadata.getNature() );
            MetadataDownload download = new MetadataDownload();
            download.setMetadata( metadata );
            download.setFile( dstFile );
            download.setChecksumPolicy( policy.getChecksumPolicy() );
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

    private RepositoryPolicy getPolicy( RepositorySystemSession session, RemoteRepository repository,
                                        Metadata.Nature nature )
    {
        boolean releases = !Metadata.Nature.SNAPSHOT.equals( nature );
        boolean snapshots = !Metadata.Nature.RELEASE.equals( nature );
        return remoteRepositoryManager.getPolicy( session, repository, releases, snapshots );
    }

    static class EventCatapult
    {

        private final RepositorySystemSession session;

        private final RemoteRepository repository;

        public EventCatapult( RepositorySystemSession session, RemoteRepository repository )
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
