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

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.ProxyServer;
import com.ning.http.client.ProxyServer.Protocol;
import com.ning.http.client.Realm;
import com.ning.http.client.providers.netty.NettyAsyncHttpProvider;
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
import org.sonatype.aether.util.layout.MavenDefaultLayout;
import org.sonatype.aether.util.layout.RepositoryLayout;

import java.io.File;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

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

    private final AsyncHttpClient httpClient;

    private final Map<String, String> checksumAlgos;

    private final AtomicBoolean closed = new AtomicBoolean( false );

    private final RepositoryLayout layout = new MavenDefaultLayout();

    private final TransferListener listener;

    private final RepositorySystemSession session;

    private boolean useCache = false;

    private final boolean disableResumeSupport;


    private final int maxIOExceptionRetry;

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

        AsyncHttpClientConfig config = createConfig( session, repository, true );
        httpClient = new AsyncHttpClient( new NettyAsyncHttpProvider( config ) );

        checksumAlgos = new LinkedHashMap<String, String>();
        checksumAlgos.put( "SHA-1", ".sha1" );
        checksumAlgos.put( "MD5", ".md5" );

        disableResumeSupport = ConfigurationProperties.get( session, "aether.connector.ahc.disableResumable", false );
        maxIOExceptionRetry = ConfigurationProperties.get( session, "aether.connector.ahc.resumeRetry", 3 );
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

    private Realm getRealm( RemoteRepository repository )
    {
        Realm realm = null;

        Authentication a = repository.getAuthentication();
        if ( a != null && a.getUsername() != null )
        {
            realm = new Realm.RealmBuilder().setPrincipal( a.getUsername() ).setPassword(
                a.getPassword() ).setUsePreemptiveAuth( false ).build();
        }

        return realm;
    }

    private ProxyServer getProxy( RemoteRepository repository )
    {
        ProxyServer proxyServer = null;

        Proxy proxy = repository.getProxy();
        if ( proxy != null )
        {
            boolean useSSL = repository.getProtocol().equalsIgnoreCase( "https" ) ||
                repository.getProtocol().equalsIgnoreCase( "dav:https" );
            proxyServer = createProxyServer( proxy, proxy.getAuthentication(), useSSL );
        }

        return proxyServer;
    }

    private ProxyServer createProxyServer( Proxy p, Authentication a, boolean useSSL )
    {
        ProxyServer proxyServer;
        if ( a == null )
        {
            proxyServer = new ProxyServer( useSSL ? Protocol.HTTPS : Protocol.HTTP, p.getHost(), p.getPort() );
        }
        else
        {
            proxyServer =
                new ProxyServer( useSSL ? Protocol.HTTPS : Protocol.HTTP, p.getHost(), p.getPort(), a.getUsername(),
                                 a.getPassword() );
        }
        return proxyServer;
    }

    /**
     * Create an {@link AsyncHttpClientConfig} instance based on the values from {@link RepositorySystemSession}
     * 
     * @param session {link RepositorySystemSession}
     * @return a AHC configuration based on the session's values
     */
    private AsyncHttpClientConfig createConfig( RepositorySystemSession session, RemoteRepository repository,
                                                boolean useCompression )
    {
        AsyncHttpClientConfig.Builder configBuilder = new AsyncHttpClientConfig.Builder();

        String userAgent = ConfigurationProperties.get( session, ConfigurationProperties.USER_AGENT,
                                                        ConfigurationProperties.DEFAULT_USER_AGENT );
        if ( !StringUtils.isEmpty( userAgent ) )
        {
            configBuilder.setUserAgent( userAgent );
        }
        int connectTimeout = ConfigurationProperties.get( session, ConfigurationProperties.CONNECT_TIMEOUT,
                                                          ConfigurationProperties.DEFAULT_CONNECT_TIMEOUT );

        configBuilder.setConnectionTimeoutInMs( connectTimeout );
        configBuilder.setCompressionEnabled( useCompression );
        configBuilder.setFollowRedirects( true );
        configBuilder.setRequestTimeoutInMs(
            ConfigurationProperties.get( session, ConfigurationProperties.REQUEST_TIMEOUT,
                                         ConfigurationProperties.DEFAULT_REQUEST_TIMEOUT ) );

        configBuilder.setProxyServer( getProxy( repository ) );
        configBuilder.setRealm( getRealm( repository ) );

        return configBuilder.build();
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

        CountDownLatch latch = new CountDownLatch( artifactDownloads.size() + metadataDownloads.size() );

        Collection<GetTask<?>> tasks = new ArrayList<GetTask<?>>();

        ConnectorConfiguration configuration =
            new ConnectorConfiguration( httpClient, repository, fileProcessor, session, logger, listener,
                                        checksumAlgos, this.disableResumeSupport, this.maxIOExceptionRetry,
                                        this.useCache );

        for ( MetadataDownload download : metadataDownloads )
        {
            String resource = layout.getPath( download.getMetadata() ).getPath();
            GetTask<?> task = GetTask.metadataTask( resource, download, latch, configuration );
            tasks.add( task );
            task.run();
        }

        for ( ArtifactDownload download : artifactDownloads )
        {
            String resource = layout.getPath( download.getArtifact() ).getPath();
            GetTask<?> task = GetTask.artifactTask( resource, download, latch, configuration );
            tasks.add( task );
            task.run();
        }

        try
        {
            latch.await();

            for ( GetTask<?> task : tasks )
            {
                task.flush();
            }
        }
        catch ( InterruptedException e )
        {
            for ( GetTask<?> task : tasks )
            {
                task.flush( e );
            }
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

        CountDownLatch latch = new CountDownLatch( artifactUploads.size() + metadataUploads.size() );

        Collection<PutTask<?>> tasks = new ArrayList<PutTask<?>>();

        for ( ArtifactUpload upload : artifactUploads )
        {
            String path = layout.getPath( upload.getArtifact() ).getPath();

            PutTask<?> task =
                new PutTask<ArtifactTransfer>( path, upload.getFile(), latch, upload, ARTIFACT_EXCEPTION_WRAPPER,
                                               httpClient, repository, listener, checksumAlgos, logger );
            tasks.add( task );
            task.run();
        }

        for ( MetadataUpload upload : metadataUploads )
        {
            String path = layout.getPath( upload.getMetadata() ).getPath();

            PutTask<?> task =
                new PutTask<MetadataTransfer>( path, upload.getFile(), latch, upload, METADATA_EXCEPTION_WRAPPER,
                                               httpClient, repository, listener, checksumAlgos, logger );
            tasks.add( task );
            task.run();
        }

        try
        {
            latch.await();

            for ( PutTask<?> task : tasks )
            {
                task.flush();
            }
        }
        catch ( InterruptedException e )
        {
            for ( PutTask<?> task : tasks )
            {
                task.flush( e );
            }
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

    /**
     * Simple placeholder for a File and it's associated lock.
     */
    static class FileLockCompanion
    {
    
        private final File file;
    
        private final FileLock lock;
    
        private final String lockPathName;
    
        public FileLockCompanion( File file, FileLock lock )
        {
            this.file = file;
            this.lock = lock;
            this.lockPathName = null;
        }
    
        public FileLockCompanion( File file, FileLock lock, String lockPathName )
        {
            this.file = file;
            this.lock = lock;
            this.lockPathName = lockPathName;
        }
    
        public File getFile()
        {
            return file;
        }
    
        public FileLock getLock()
        {
            return lock;
        }
    
        public String getLockedPathFile()
        {
            return lockPathName;
        }
    
    }

}
