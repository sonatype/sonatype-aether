package org.sonatype.aether.connector.wagon;

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
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.WagonException;
import org.apache.maven.wagon.authentication.AuthenticationInfo;
import org.apache.maven.wagon.observers.ChecksumObserver;
import org.apache.maven.wagon.proxy.ProxyInfo;
import org.apache.maven.wagon.proxy.ProxyInfoProvider;
import org.apache.maven.wagon.repository.Repository;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.aether.ArtifactNotFoundException;
import org.sonatype.aether.ArtifactTransferException;
import org.sonatype.aether.Authentication;
import org.sonatype.aether.AuthenticationSelector;
import org.sonatype.aether.ChecksumFailureException;
import org.sonatype.aether.MetadataNotFoundException;
import org.sonatype.aether.MetadataTransferException;
import org.sonatype.aether.NoRepositoryConnectorException;
import org.sonatype.aether.Proxy;
import org.sonatype.aether.ProxySelector;
import org.sonatype.aether.RemoteRepository;
import org.sonatype.aether.RepositoryPolicy;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.TransferEvent;
import org.sonatype.aether.TransferListener;
import org.sonatype.aether.spi.connector.ArtifactDownload;
import org.sonatype.aether.spi.connector.ArtifactTransfer;
import org.sonatype.aether.spi.connector.ArtifactUpload;
import org.sonatype.aether.spi.connector.MetadataDownload;
import org.sonatype.aether.spi.connector.MetadataTransfer;
import org.sonatype.aether.spi.connector.MetadataUpload;
import org.sonatype.aether.spi.connector.RepositoryConnector;
import org.sonatype.aether.spi.connector.Transfer;
import org.sonatype.aether.spi.log.Logger;
import org.sonatype.aether.util.ChecksumUtils;
import org.sonatype.aether.util.layout.MavenDefaultLayout;
import org.sonatype.aether.util.layout.RepositoryLayout;
import org.sonatype.aether.util.listener.DefaultTransferEvent;

/**
 * A repository connector that uses Maven Wagon for the transfer.
 * 
 * @author Benjamin Bentmann
 */
class WagonRepositoryConnector
    implements RepositoryConnector
{

    private final Logger logger;

    private final RemoteRepository repository;

    private final RepositorySystemSession session;

    private final WagonProvider wagonProvider;

    private final String wagonHint;

    private final Repository wagonRepo;

    private final AuthenticationInfo wagonAuth;

    private final ProxyInfoProvider wagonProxy;

    private final RepositoryLayout layout = new MavenDefaultLayout();

    private final TransferListener listener;

    private final Queue<Wagon> wagons = new ConcurrentLinkedQueue<Wagon>();

    private final Executor executor;

    private boolean closed;

    private Map<String, String> checksumAlgos;

    public WagonRepositoryConnector( WagonProvider wagonProvider, RemoteRepository repository,
                                     RepositorySystemSession session, Logger logger )
        throws NoRepositoryConnectorException
    {
        this.logger = logger;
        this.wagonProvider = wagonProvider;
        this.repository = repository;
        this.session = session;
        this.listener = session.getTransferListener();

        if ( !"default".equals( repository.getContentType() ) )
        {
            throw new NoRepositoryConnectorException( repository );
        }

        wagonRepo = new Repository( repository.getId(), repository.getUrl() );
        wagonHint = wagonRepo.getProtocol().toLowerCase( Locale.ENGLISH );
        try
        {
            wagons.add( lookupWagon() );
        }
        catch ( Exception e )
        {
            throw new NoRepositoryConnectorException( repository );
        }

        wagonAuth = getAuthenticationInfo( repository, session.getAuthenticationSelector() );
        wagonProxy = getProxy( repository, session.getProxySelector() );

        int threads = getOption( "maven.artifact.threads", 5 );
        if ( threads <= 1 )
        {
            executor = new Executor()
            {
                public void execute( Runnable command )
                {
                    command.run();
                }
            };
        }
        else
        {
            executor =
                new ThreadPoolExecutor( threads, threads, 3, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>() );
        }

        checksumAlgos = new LinkedHashMap<String, String>();
        checksumAlgos.put( "SHA-1", ".sha1" );
        checksumAlgos.put( "MD5", ".md5" );
    }

    private int getOption( String key, int defaultValue )
    {
        String value = session.getConfigProperties().get( key );
        try
        {
            return Integer.valueOf( value );
        }
        catch ( Exception e )
        {
            return defaultValue;
        }
    }

    private AuthenticationInfo getAuthenticationInfo( RemoteRepository repository, AuthenticationSelector selector )
    {
        AuthenticationInfo auth = null;

        if ( selector != null )
        {
            Authentication a = selector.getAuthentication( repository );
            if ( a != null )
            {
                auth = new AuthenticationInfo();
                auth.setUserName( a.getUsername() );
                auth.setPassword( a.getPassword() );
                auth.setPrivateKey( a.getPrivateKeyFile() );
                auth.setPassphrase( a.getPassphrase() );
            }
        }

        return auth;
    }

    private ProxyInfoProvider getProxy( RemoteRepository repository, ProxySelector selector )
    {
        ProxyInfoProvider proxy = null;

        if ( selector != null )
        {
            Proxy p = selector.getProxy( repository );
            if ( p != null )
            {
                final ProxyInfo prox = new ProxyInfo();
                prox.setType( p.getType() );
                prox.setHost( p.getHost() );
                prox.setPort( p.getPort() );
                if ( p.getAuthentication() != null )
                {
                    prox.setUserName( p.getAuthentication().getUsername() );
                    prox.setPassword( p.getAuthentication().getPassword() );
                }
                proxy = new ProxyInfoProvider()
                {
                    public ProxyInfo getProxyInfo( String protocol )
                    {
                        return prox;
                    }
                };
            }
        }

        return proxy;
    }

    private Wagon lookupWagon()
        throws Exception
    {
        return wagonProvider.lookup( wagonHint );
    }

    private void releaseWagon( Wagon wagon )
    {
        wagonProvider.release( wagon );
    }

    private void connectWagon( Wagon wagon )
        throws Exception
    {
        if ( StringUtils.isNotEmpty( session.getUserAgent() ) )
        {
            try
            {
                Method setHttpHeaders = wagon.getClass().getMethod( "setHttpHeaders", Properties.class );
                Properties headers = new Properties();
                headers.setProperty( "User-Agent", session.getUserAgent() );
                setHttpHeaders.invoke( wagon, headers );
            }
            catch ( Exception e )
            {
                logger.debug( "Could not set user agent for wagon " + wagon.getClass().getName() + ": " + e );
            }
        }
        if ( session.getConnectTimeout() > 0 )
        {
            wagon.setTimeout( session.getConnectTimeout() );
        }
        wagon.connect( wagonRepo, wagonAuth, wagonProxy );
    }

    private void disconnectWagon( Wagon wagon )
    {
        try
        {
            if ( wagon != null )
            {
                wagon.disconnect();
            }
        }
        catch ( Exception e )
        {
            // too bad
        }
    }

    Wagon pollWagon()
        throws Exception
    {
        Wagon wagon = wagons.poll();

        if ( wagon == null )
        {
            try
            {
                wagon = lookupWagon();
                connectWagon( wagon );
            }
            catch ( Exception e )
            {
                releaseWagon( wagon );
                throw e;
            }
        }
        else if ( wagon.getRepository() == null )
        {
            try
            {
                connectWagon( wagon );
            }
            catch ( Exception e )
            {
                wagons.add( wagon );
                throw e;
            }
        }

        return wagon;
    }

    private <T> Collection<T> safe( Collection<T> items )
    {
        return ( items != null ) ? items : Collections.<T> emptyList();
    }

    public void get( Collection<? extends ArtifactDownload> artifactDownloads,
                     Collection<? extends MetadataDownload> metadataDownloads )
    {
        if ( closed )
        {
            throw new IllegalStateException( "connector closed" );
        }

        artifactDownloads = safe( artifactDownloads );
        metadataDownloads = safe( metadataDownloads );

        CountDownLatch latch = new CountDownLatch( artifactDownloads.size() + metadataDownloads.size() );

        Collection<GetTask<?>> tasks = new ArrayList<GetTask<?>>();

        for ( MetadataDownload download : metadataDownloads )
        {
            String resource = layout.getPath( download.getMetadata() ).getPath();
            GetTask<?> task =
                new GetTask<MetadataTransfer>( resource, download.getFile(), download.getChecksumPolicy(), latch,
                                               download, METADATA );
            tasks.add( task );
            executor.execute( task );
        }

        for ( ArtifactDownload download : artifactDownloads )
        {
            String resource = layout.getPath( download.getArtifact() ).getPath();
            GetTask<?> task =
                new GetTask<ArtifactTransfer>( resource, download.isExistenceCheck() ? null : download.getFile(),
                                               download.getChecksumPolicy(), latch, download, ARTIFACT );
            tasks.add( task );
            executor.execute( task );
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

    public void put( Collection<? extends ArtifactUpload> artifactUploads,
                     Collection<? extends MetadataUpload> metadataUploads )
    {
        if ( closed )
        {
            throw new IllegalStateException( "connector closed" );
        }

        artifactUploads = safe( artifactUploads );
        metadataUploads = safe( metadataUploads );

        for ( ArtifactUpload upload : artifactUploads )
        {
            String path = layout.getPath( upload.getArtifact() ).getPath();

            PutTask<?> task = new PutTask<ArtifactTransfer>( path, upload.getFile(), upload, ARTIFACT );
            task.run();
            task.flush();
        }

        for ( MetadataUpload upload : metadataUploads )
        {
            String path = layout.getPath( upload.getMetadata() ).getPath();

            PutTask<?> task = new PutTask<MetadataTransfer>( path, upload.getFile(), upload, METADATA );
            task.run();
            task.flush();
        }
    }

    public void close()
    {
        closed = true;

        for ( Wagon wagon = wagons.poll(); wagon != null; wagon = wagons.poll() )
        {
            disconnectWagon( wagon );
            releaseWagon( wagon );
        }

        if ( executor instanceof ExecutorService )
        {
            ( (ExecutorService) executor ).shutdown();
        }
    }

    class GetTask<T extends Transfer>
        implements Runnable
    {

        private final T download;

        private final String path;

        private final File file;

        private final String checksumPolicy;

        private final CountDownLatch latch;

        private volatile Exception exception;

        private final ExceptionWrapper<T> wrapper;

        public GetTask( String path, File file, String checksumPolicy, CountDownLatch latch, T download,
                        ExceptionWrapper<T> wrapper )
        {
            this.path = path;
            this.file = file;
            this.checksumPolicy = checksumPolicy;
            this.latch = latch;
            this.download = download;
            this.wrapper = wrapper;
        }

        public T getDownload()
        {
            return download;
        }

        public Exception getException()
        {
            return exception;
        }

        public void run()
        {
            download.setState( Transfer.State.ACTIVE );

            WagonTransferListenerAdapter wagonListener = null;
            if ( listener != null )
            {
                wagonListener = new WagonTransferListenerAdapter( listener, wagonRepo.getUrl(), path, file );
            }

            try
            {
                if ( listener != null )
                {
                    DefaultTransferEvent event = wagonListener.newEvent();
                    event.setRequestType( TransferEvent.RequestType.GET );
                    event.setType( TransferEvent.EventType.INITIATED );
                    listener.transferInitiated( event );
                }

                File tmp = ( file != null ) ? new File( file.getPath() + ".tmp" + System.currentTimeMillis() ) : null;

                Wagon wagon = pollWagon();

                try
                {
                    if ( file == null )
                    {
                        if ( !wagon.resourceExists( path ) )
                        {
                            throw new ResourceDoesNotExistException( "Could not find " + path + " in "
                                + wagonRepo.getUrl() );
                        }
                    }
                    else
                    {
                        for ( int trial = 1; trial >= 0; trial-- )
                        {
                            ChecksumObserver sha1 = new ChecksumObserver( "SHA-1" );
                            ChecksumObserver md5 = new ChecksumObserver( "MD5" );
                            try
                            {
                                wagon.addTransferListener( wagonListener );
                                wagon.addTransferListener( md5 );
                                wagon.addTransferListener( sha1 );

                                /*
                                 * NOTE: AbstractWagon.createParentDirectories() seems to occasionally fail when
                                 * executed concurrently, so we try a little harder.
                                 */
                                File dir = tmp.getParentFile();
                                for ( int i = 0; i < 5 && !dir.exists(); i++ )
                                {
                                    dir.mkdirs();
                                }

                                wagon.get( path, tmp );
                            }
                            finally
                            {
                                wagon.removeTransferListener( wagonListener );
                                wagon.removeTransferListener( md5 );
                                wagon.removeTransferListener( sha1 );
                            }

                            if ( !RepositoryPolicy.CHECKSUM_POLICY_IGNORE.equals( checksumPolicy ) )
                            {
                                try
                                {
                                    if ( !verifyChecksum( wagon, sha1.getActualChecksum(), ".sha1" )
                                        && !verifyChecksum( wagon, md5.getActualChecksum(), ".md5" ) )
                                    {
                                        trial = 0;
                                        throw new ChecksumFailureException( "Checksum validation failed"
                                            + ", no checksums available from the repository" );
                                    }
                                    break;
                                }
                                catch ( ChecksumFailureException e )
                                {
                                    if ( trial <= 0 && RepositoryPolicy.CHECKSUM_POLICY_FAIL.equals( checksumPolicy ) )
                                    {
                                        throw e;
                                    }
                                    if ( listener != null )
                                    {
                                        DefaultTransferEvent event = wagonListener.newEvent();
                                        event.setRequestType( TransferEvent.RequestType.GET );
                                        event.setType( TransferEvent.EventType.CORRUPTED );
                                        event.setException( e );
                                        listener.transferCorrupted( event );
                                    }
                                }
                            }
                        }

                        rename( tmp, file );
                    }

                    if ( listener != null )
                    {
                        DefaultTransferEvent event = wagonListener.newEvent();
                        event.setRequestType( TransferEvent.RequestType.GET );
                        event.setType( TransferEvent.EventType.SUCCEEDED );
                        listener.transferSucceeded( event );
                    }
                }
                finally
                {
                    if ( tmp != null )
                    {
                        tmp.delete();
                    }
                    wagons.add( wagon );
                }
            }
            catch ( Exception e )
            {
                exception = e;

                if ( listener != null )
                {
                    DefaultTransferEvent event = wagonListener.newEvent();
                    event.setRequestType( TransferEvent.RequestType.GET );
                    event.setType( TransferEvent.EventType.FAILED );
                    event.setException( e );
                    listener.transferFailed( event );
                }
            }
            finally
            {
                latch.countDown();
            }
        }

        public void flush()
        {
            flush( null );
        }

        public void flush( Exception exception )
        {
            Exception e = this.exception;
            wrapper.wrap( download, ( e != null ) ? e : exception, repository );
            download.setState( Transfer.State.DONE );
        }

        private boolean verifyChecksum( Wagon wagon, String actual, String ext )
            throws ChecksumFailureException
        {
            File tmp = new File( file.getPath() + ext + ".tmp" + System.currentTimeMillis() );

            try
            {
                try
                {
                    wagon.get( path + ext, tmp );
                }
                catch ( ResourceDoesNotExistException e )
                {
                    return false;
                }
                catch ( WagonException e )
                {
                    throw new ChecksumFailureException( e );
                }

                String expected;

                try
                {
                    expected = ChecksumUtils.read( tmp );
                }
                catch ( IOException e )
                {
                    throw new ChecksumFailureException( e );
                }

                if ( expected.equalsIgnoreCase( actual ) )
                {
                    try
                    {
                        rename( tmp, new File( file.getPath() + ext ) );
                    }
                    catch ( IOException e )
                    {
                        // ignored, non-critical
                    }
                }
                else
                {
                    throw new ChecksumFailureException( expected, actual );
                }
            }
            finally
            {
                tmp.delete();
            }

            return true;
        }

        private void rename( File from, File to )
            throws IOException
        {
            if ( !from.renameTo( to ) )
            {
                FileUtils.copyFile( from, to );
            }
        }

    }

    class PutTask<T extends Transfer>
        implements Runnable
    {

        private final T upload;

        private final ExceptionWrapper<T> wrapper;

        private final String path;

        private final File file;

        private volatile Exception exception;

        public PutTask( String path, File file, T upload, ExceptionWrapper<T> wrapper )
        {
            this.path = path;
            this.file = file;
            this.upload = upload;
            this.wrapper = wrapper;
        }

        public void run()
        {
            upload.setState( Transfer.State.ACTIVE );

            WagonTransferListenerAdapter wagonListener = null;
            if ( listener != null )
            {
                wagonListener = new WagonTransferListenerAdapter( listener, wagonRepo.getUrl(), path, file );
            }

            try
            {
                if ( listener != null )
                {
                    DefaultTransferEvent event = wagonListener.newEvent();
                    event.setRequestType( TransferEvent.RequestType.PUT );
                    event.setType( TransferEvent.EventType.INITIATED );
                    listener.transferInitiated( event );
                }

                Wagon wagon = pollWagon();

                try
                {
                    try
                    {
                        wagon.addTransferListener( wagonListener );

                        wagon.put( file, path );
                    }
                    finally
                    {
                        wagon.removeTransferListener( wagonListener );
                    }

                    uploadChecksums( wagon, file, path );

                    if ( listener != null )
                    {
                        DefaultTransferEvent event = wagonListener.newEvent();
                        event.setRequestType( TransferEvent.RequestType.PUT );
                        event.setType( TransferEvent.EventType.SUCCEEDED );
                        listener.transferSucceeded( event );
                    }
                }
                finally
                {
                    wagons.add( wagon );
                }
            }
            catch ( Exception e )
            {
                exception = e;

                if ( listener != null )
                {
                    DefaultTransferEvent event = wagonListener.newEvent();
                    event.setRequestType( TransferEvent.RequestType.PUT );
                    event.setType( TransferEvent.EventType.FAILED );
                    event.setException( e );
                    listener.transferFailed( event );
                }
            }
        }

        public void flush()
        {
            wrapper.wrap( upload, exception, repository );
            upload.setState( Transfer.State.DONE );
        }

        private void uploadChecksums( Wagon wagon, File file, String path )
        {
            try
            {
                Map<String, Object> checksums = ChecksumUtils.calc( file, checksumAlgos.keySet() );
                for ( Map.Entry<String, Object> entry : checksums.entrySet() )
                {
                    uploadChecksum( wagon, file, path, entry.getKey(), entry.getValue() );
                }
            }
            catch ( IOException e )
            {
                logger.debug( "Failed to upload checksums for " + file + ": " + e.getMessage(), e );
            }
        }

        private void uploadChecksum( Wagon wagon, File file, String path, String algo, Object checksum )
        {
            try
            {
                if ( checksum instanceof Exception )
                {
                    throw (Exception) checksum;
                }

                String ext = checksumAlgos.get( algo );

                File tmpFile = File.createTempFile( "checksum", ext );
                try
                {
                    FileUtils.fileWrite( tmpFile.getAbsolutePath(), "UTF-8", String.valueOf( checksum ) );
                    wagon.put( tmpFile, path + ext );
                }
                finally
                {
                    tmpFile.delete();
                }
            }
            catch ( Exception e )
            {
                logger.debug( "Failed to upload " + algo + " checksum for " + file + ": " + e.getMessage(), e );
            }
        }

    }

    static interface ExceptionWrapper<T>
    {

        void wrap( T transfer, Exception e, RemoteRepository repository );

    }

    private static final ExceptionWrapper<MetadataTransfer> METADATA = new ExceptionWrapper<MetadataTransfer>()
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

    private static final ExceptionWrapper<ArtifactTransfer> ARTIFACT = new ExceptionWrapper<ArtifactTransfer>()
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

}
