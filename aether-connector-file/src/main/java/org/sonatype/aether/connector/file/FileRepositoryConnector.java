package org.sonatype.aether.connector.file;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import java.util.Collection;
import java.util.Collections;

import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.spi.connector.ArtifactDownload;
import org.sonatype.aether.spi.connector.ArtifactUpload;
import org.sonatype.aether.spi.connector.MetadataDownload;
import org.sonatype.aether.spi.connector.MetadataUpload;
import org.sonatype.aether.spi.connector.RepositoryConnector;
import org.sonatype.aether.spi.io.FileProcessor;
import org.sonatype.aether.spi.log.Logger;
import org.sonatype.aether.spi.log.NullLogger;
import org.sonatype.aether.transfer.NoRepositoryConnectorException;
import org.sonatype.aether.util.concurrency.RunnableErrorForwarder;

/**
 * A connector for file://-URLs.
 * 
 * @author Benjamin Hanzelmann
 */
public class FileRepositoryConnector
    extends ParallelRepositoryConnector
    implements RepositoryConnector
{

    private RemoteRepository repository;

    private RepositorySystemSession session;

    private Logger logger = NullLogger.INSTANCE;

    private FileProcessor fileProcessor;

    public FileRepositoryConnector( RepositorySystemSession session, RemoteRepository repository,
                                    FileProcessor fileProcessor, Logger logger )
        throws NoRepositoryConnectorException
    {
        if ( !"default".equals( repository.getContentType() ) )
        {
            throw new NoRepositoryConnectorException( repository );
        }

        this.session = session;
        this.repository = repository;
        this.fileProcessor = fileProcessor;
        this.logger = logger;

        initExecutor( session.getConfigProperties() );
    }

    public void get( Collection<? extends ArtifactDownload> artifactDownloads,
                     Collection<? extends MetadataDownload> metadataDownloads )
    {
        checkClosed();

        artifactDownloads = notNull( artifactDownloads );
        metadataDownloads = notNull( metadataDownloads );

        RunnableErrorForwarder errorForwarder = new RunnableErrorForwarder();

        for ( ArtifactDownload artifactDownload : artifactDownloads )
        {
            FileRepositoryWorker worker = new FileRepositoryWorker( artifactDownload, repository, session );
            worker.setLogger( logger );
            worker.setFileProcessor( fileProcessor );
            executor.execute( errorForwarder.wrap( worker ) );
        }

        for ( MetadataDownload metadataDownload : metadataDownloads )
        {
            FileRepositoryWorker worker = new FileRepositoryWorker( metadataDownload, repository, session );
            worker.setLogger( logger );
            worker.setFileProcessor( fileProcessor );
            executor.execute( errorForwarder.wrap( worker ) );
        }

        errorForwarder.await();
    }

    private <E> Collection<E> notNull( Collection<E> col )
    {
        return col == null ? Collections.<E> emptyList() : col;
    }

    public void put( Collection<? extends ArtifactUpload> artifactUploads,
                     Collection<? extends MetadataUpload> metadataUploads )
    {
        checkClosed();

        artifactUploads = notNull( artifactUploads );
        metadataUploads = notNull( metadataUploads );

        RunnableErrorForwarder errorForwarder = new RunnableErrorForwarder();

        for ( ArtifactUpload artifactUpload : artifactUploads )
        {
            FileRepositoryWorker worker = new FileRepositoryWorker( artifactUpload, repository, session );
            worker.setLogger( logger );
            worker.setFileProcessor( fileProcessor );
            executor.execute( errorForwarder.wrap( worker ) );
        }
        for ( MetadataUpload metadataUpload : metadataUploads )
        {
            FileRepositoryWorker worker = new FileRepositoryWorker( metadataUpload, repository, session );
            worker.setLogger( logger );
            worker.setFileProcessor( fileProcessor );
            executor.execute( errorForwarder.wrap( worker ) );
        }

        errorForwarder.await();
    }

    @Override
    public String toString()
    {
        return String.valueOf( repository );
    }

}
