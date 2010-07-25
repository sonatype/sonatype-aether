package org.sonatype.aether.connector.file;

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

import java.util.Collection;
import java.util.Collections;

import org.sonatype.aether.NoRepositoryConnectorException;
import org.sonatype.aether.RemoteRepository;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.spi.connector.ArtifactDownload;
import org.sonatype.aether.spi.connector.ArtifactUpload;
import org.sonatype.aether.spi.connector.MetadataDownload;
import org.sonatype.aether.spi.connector.MetadataUpload;
import org.sonatype.aether.spi.connector.RepositoryConnector;

public class FileRepositoryConnector
    extends ParallelRepositoryConnector
    implements RepositoryConnector
{


    private RemoteRepository repository;

    private RepositorySystemSession session;

    public FileRepositoryConnector( RepositorySystemSession session, RemoteRepository repository )
        throws NoRepositoryConnectorException
    {
        super( session.getConfigProperties() );
        if ( !"default".equals( repository.getContentType() ) )
        {
            throw new NoRepositoryConnectorException( repository );
        }

        this.session = session;
        this.repository = repository;
    }

    public void get( Collection<? extends ArtifactDownload> artifactDownloads,
                     Collection<? extends MetadataDownload> metadataDownloads )
    {
        artifactDownloads = notNull( artifactDownloads );
        metadataDownloads = notNull( metadataDownloads );

        for ( ArtifactDownload artifactDownload : artifactDownloads )
        {
            ArtifactWorker worker = new ArtifactWorker( artifactDownload, repository, session);
            executor.execute( worker );
        }
        
        for ( MetadataDownload metadataDownload : metadataDownloads )
        {
            MetadataWorker worker = new MetadataWorker (metadataDownload, repository, session);
            executor.execute( worker );
        }
    }

    private <E> Collection<E> notNull( Collection<E> col )
    {
        return col == null ? Collections.<E> emptyList() : col;
    }

    public void put( Collection<? extends ArtifactUpload> artifactUploads,
                     Collection<? extends MetadataUpload> metadataUploads )
    {
        for ( ArtifactUpload artifactUpload : artifactUploads )
        {
            ArtifactWorker worker = new ArtifactWorker( artifactUpload, repository, session );
            executor.execute( worker );
        }
        for ( MetadataUpload metadataUpload : metadataUploads)
        {
            MetadataWorker worker = new MetadataWorker (metadataUpload, repository, session);
            executor.execute( worker );
        }

    }

    public void close()
    {
        // TODO Auto-generated method stub

    }

}
