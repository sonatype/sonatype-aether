package org.sonatype.aether.connector.async;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *******************************************************************************/

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.sonatype.aether.ConfigurationProperties;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.repository.Authentication;
import org.sonatype.aether.repository.Proxy;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.spi.connector.ArtifactDownload;
import org.sonatype.aether.spi.connector.ArtifactTransfer;
import org.sonatype.aether.spi.connector.ArtifactUpload;
import org.sonatype.aether.spi.connector.MetadataDownload;
import org.sonatype.aether.spi.connector.MetadataTransfer;
import org.sonatype.aether.spi.connector.MetadataUpload;
import org.sonatype.aether.spi.connector.RepositoryConnector;
import org.sonatype.aether.spi.io.FileProcessor;
import org.sonatype.aether.spi.log.Logger;
import org.sonatype.aether.transfer.ArtifactNotFoundException;
import org.sonatype.aether.transfer.ArtifactTransferException;
import org.sonatype.aether.transfer.MetadataNotFoundException;
import org.sonatype.aether.transfer.MetadataTransferException;
import org.sonatype.aether.transfer.NoRepositoryConnectorException;
import org.sonatype.aether.transfer.TransferListener;
import org.sonatype.aether.util.StringUtils;

import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.ProxyServer.Protocol;
import com.ning.http.client.SimpleAsyncHttpClient.ErrorDocumentBehaviour;
import com.ning.http.client.Realm;
import com.ning.http.client.SimpleAsyncHttpClient;

/**
 * A repository connector that uses the Async Http Client.
 * 
 * @author Jeanfrancois Arcand
 */
class AsyncRepositoryConnector
    implements RepositoryConnector
{
    private final Logger logger;

    private final FileProcessor fileProcessor;

    private final RemoteRepository repository;

    private final SimpleAsyncHttpClient httpClient;

    private final Map<String, String> checksumAlgos;

    private final AtomicBoolean closed = new AtomicBoolean( false );

    private final TransferListener listener;

    private final RepositorySystemSession session;

    private final boolean resumeTransfers;

    // private final int maxIOExceptionRetry;

    /**
     * Create an {@link org.sonatype.aether.connector.async.AsyncRepositoryConnector} instance which connect to the
     * {@link RemoteRepository}
     *
     * @param repository the remote repository
     * @param session    the {@link RepositorySystemSession}
     * @param logger     the logger.
     * @throws NoRepositoryConnectorException
     */
    public AsyncRepositoryConnector( RemoteRepository repository, RepositorySystemSession session,
                                     FileProcessor fileProcessor, Logger logger )
        throws NoRepositoryConnectorException
    {
        this.logger = logger;
        this.repository = repository;
        this.listener = session.getTransferListener();
        this.fileProcessor = fileProcessor;
        this.session = session;

        if ( !"default".equals( repository.getContentType() ) )
        {
            throw new NoRepositoryConnectorException( repository );
        }

        validateProtocol( repository );

        httpClient = createClient( session, repository, true );

        checksumAlgos = new LinkedHashMap<String, String>();
        checksumAlgos.put( "SHA-1", ".sha1" );
        checksumAlgos.put( "MD5", ".md5" );

        // support old 'disable' option
        resumeTransfers =
            ConfigurationProperties.get( session, "aether.connector.ahc.resumeTransfers", false )
                && ( !ConfigurationProperties.get( session, "aether.connector.ahc.disableResumable", false ) );

    }

    private void validateProtocol( RemoteRepository repository )
        throws NoRepositoryConnectorException
    {
        if ( !repository.getProtocol().regionMatches( true, 0, "http", 0, "http".length() ) &&
            !repository.getProtocol().regionMatches( true, 0, "dav", 0, "dav".length() ) )
        {
            throw new NoRepositoryConnectorException( repository );
        }
    }

    private Realm setRealm( RemoteRepository repository, SimpleAsyncHttpClient.Builder sahc )
    {
        Realm realm = null;

        Authentication authentication = repository.getAuthentication();
        if ( authentication != null && authentication.getUsername() != null )
        {
            sahc.setRealmPrincipal( authentication.getUsername() );
            sahc.setRealmPassword( authentication.getPassword() );
            sahc.setRealmUsePreemptiveAuth( false );
        }

        return realm;
    }

    /**
     * Create an {@link AsyncHttpClientConfig} instance based on the values from {@link RepositorySystemSession}
     * 
     * @param session {link RepositorySystemSession}
     * @return a AHC configuration based on the session's values
     */
    SimpleAsyncHttpClient createClient( RepositorySystemSession session, RemoteRepository repository,
                                                boolean useCompression )
    {

        SimpleAsyncHttpClient.Builder configBuilder = new SimpleAsyncHttpClient.Builder();

        setUserAgent( session, configBuilder );

        setTimeouts( session, configBuilder );

        setProxy( repository, configBuilder );
        setRealm( repository, configBuilder );

        configBuilder.setCompressionEnabled( useCompression );
        configBuilder.setFollowRedirects( true );

        configBuilder.setHeader( "Pragma", "no-cache" );
        configBuilder.setHeader( "Accept", "text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2" );

        configBuilder.setErrorDocumentBehaviour( ErrorDocumentBehaviour.OMIT );

        configBuilder.setResumableDownload( resumeTransfers );

        configBuilder.setMaximumConnectionsPerHost( 10 );

        configBuilder.setIdleConnectionInPoolTimeoutInMs( 30000 );

        // This is not a throttling limit, but fails if the limit is reached
        // configBuilder.setMaximumConnectionsTotal( 100 );
        // TODO add throttling limit as soon as it exists.

        return configBuilder.build();
    }

    private void setTimeouts( RepositorySystemSession session, SimpleAsyncHttpClient.Builder configBuilder )
    {
        int connectTimeout = ConfigurationProperties.get( session, ConfigurationProperties.CONNECT_TIMEOUT,
                                                          ConfigurationProperties.DEFAULT_CONNECT_TIMEOUT );

        configBuilder.setConnectionTimeoutInMs( connectTimeout );
        configBuilder.setRequestTimeoutInMs(
            ConfigurationProperties.get( session, ConfigurationProperties.REQUEST_TIMEOUT,
                                         ConfigurationProperties.DEFAULT_REQUEST_TIMEOUT ) );
    }

    private void setUserAgent( RepositorySystemSession session, SimpleAsyncHttpClient.Builder configBuilder )
    {
        String userAgent =
            ConfigurationProperties.get( session, ConfigurationProperties.USER_AGENT,
                                         ConfigurationProperties.DEFAULT_USER_AGENT );
        if ( !StringUtils.isEmpty( userAgent ) )
        {
            configBuilder.setUserAgent( userAgent );
        }
    }

    private void setProxy( RemoteRepository repository, SimpleAsyncHttpClient.Builder configBuilder )
    {
        Proxy proxy = repository.getProxy();
        if ( proxy != null )
        {
            configBuilder.setProxyHost( proxy.getHost() );
            configBuilder.setProxyPort( proxy.getPort() );
            Protocol protocol =
                repository.getProtocol().equalsIgnoreCase( "https" )
                    || repository.getProtocol().equalsIgnoreCase( "dav:https" ) ? Protocol.HTTPS : Protocol.HTTP;
            configBuilder.setProxyProtocol( protocol );
            Authentication proxyAuth = proxy.getAuthentication();
            if ( proxyAuth != null )
            {
                configBuilder.setProxyPrincipal( proxyAuth.getUsername() );
                configBuilder.setProxyPassword( proxyAuth.getPassword() );
            }
        }
    }

    /**
     * Use the async http client library to download artifacts and metadata.
     *
     * @param artifactDownloads The artifact downloads to perform, may be {@code null} or empty.
     * @param metadataDownloads The metadata downloads to perform, may be {@code null} or empty.
     */
    public void get( Collection<? extends ArtifactDownload> artifactDownloads,
                     Collection<? extends MetadataDownload> metadataDownloads )
    {
        if ( closed.get() )
        {
            throw new IllegalStateException( "connector closed" );
        }

        artifactDownloads = safe( artifactDownloads );
        metadataDownloads = safe( metadataDownloads );

        Collection<SimpleGetTask> tasks = new ArrayList<SimpleGetTask>();

        ConnectorConfiguration configuration =
            new ConnectorConfiguration( httpClient, repository, fileProcessor, session, logger, listener, checksumAlgos );

        for ( MetadataDownload download : metadataDownloads )
        {
            SimpleGetTask task = new SimpleGetTask( download, configuration );

            tasks.add( task );
            task.run();
        }

        for ( ArtifactDownload download : artifactDownloads )
        {
            SimpleGetTask task = new SimpleGetTask( download, configuration );
            tasks.add( task );
            task.run();
        }

        for ( SimpleGetTask task : tasks )
        {
            task.flush();
        }
    }

    /**
     * Use the async http client library to upload artifacts and metadata.
     *
     * @param artifactUploads The artifact uploads to perform, may be {@code null} or empty.
     * @param metadataUploads The metadata uploads to perform, may be {@code null} or empty.
     */
    public void put( Collection<? extends ArtifactUpload> artifactUploads,
                     Collection<? extends MetadataUpload> metadataUploads )
    {
        if ( closed.get() )
        {
            throw new IllegalStateException( "connector closed" );
        }

        artifactUploads = safe( artifactUploads );
        metadataUploads = safe( metadataUploads );

        Collection<SimplePutTask> tasks = new ArrayList<SimplePutTask>();

        ConnectorConfiguration configuration =
            new ConnectorConfiguration( httpClient, repository, fileProcessor, session, logger, listener, checksumAlgos );


        for ( ArtifactUpload upload : artifactUploads )
        {
            SimplePutTask task = new SimplePutTask( upload, configuration );
            tasks.add( task );
            task.run();
        }

        for ( MetadataUpload upload : metadataUploads )
        {
            SimplePutTask task = new SimplePutTask( upload, configuration );
            tasks.add( task );
            task.run();
        }

        for ( SimplePutTask task : tasks )
        {
            task.flush();
        }
    }

    public void close()
    {
        closed.set( true );
        httpClient.close();
    }

    private <T> Collection<T> safe( Collection<T> items )
    {
        return ( items != null ) ? items : Collections.<T>emptyList();
    }

    static final ExceptionWrapper<MetadataTransfer> METADATA_EXCEPTION_WRAPPER =
        new ExceptionWrapper<MetadataTransfer>()
    {
        public void wrap( MetadataTransfer transfer, Exception e, RemoteRepository repository )
        {
            MetadataTransferException ex = null;
            if ( e instanceof ResourceDoesNotExistException )
            {
                ex = new MetadataNotFoundException( transfer.getMetadata(), repository );
            }
            else if ( e != null )
            {
                ex = new MetadataTransferException( transfer.getMetadata(), repository, e );
            }
            transfer.setException( ex );
        }
    };

    static final ExceptionWrapper<ArtifactTransfer> ARTIFACT_EXCEPTION_WRAPPER =
        new ExceptionWrapper<ArtifactTransfer>()
    {
        public void wrap( ArtifactTransfer transfer, Exception e, RemoteRepository repository )
        {
            ArtifactTransferException ex = null;
            if ( e instanceof ResourceDoesNotExistException )
            {
                ex = new ArtifactNotFoundException( transfer.getArtifact(), repository );
            }
            else if ( e != null )
            {
                ex = new ArtifactTransferException( transfer.getArtifact(), repository, e );
            }
            transfer.setException( ex );
        }
    };

    static interface ExceptionWrapper<T>
    {
        void wrap( T transfer, Exception e, RemoteRepository repository );
    }

}
