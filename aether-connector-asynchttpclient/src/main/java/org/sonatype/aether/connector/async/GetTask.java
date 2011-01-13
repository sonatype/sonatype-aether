package org.sonatype.aether.connector.async;

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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.connector.async.AsyncRepositoryConnector.ExceptionWrapper;
import org.sonatype.aether.connector.async.AsyncRepositoryConnector.FileLockCompanion;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.repository.RepositoryPolicy;
import org.sonatype.aether.spi.connector.ArtifactDownload;
import org.sonatype.aether.spi.connector.ArtifactTransfer;
import org.sonatype.aether.spi.connector.MetadataDownload;
import org.sonatype.aether.spi.connector.MetadataTransfer;
import org.sonatype.aether.spi.connector.Transfer;
import org.sonatype.aether.spi.io.FileProcessor;
import org.sonatype.aether.spi.log.Logger;
import org.sonatype.aether.transfer.ChecksumFailureException;
import org.sonatype.aether.transfer.TransferEvent.EventType;
import org.sonatype.aether.transfer.TransferEvent.RequestType;
import org.sonatype.aether.transfer.TransferListener;
import org.sonatype.aether.transfer.TransferResource;
import org.sonatype.aether.util.ChecksumUtils;
import org.sonatype.aether.util.listener.DefaultTransferResource;

import com.ning.http.client.AsyncHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.AsyncHttpClientConfig.Builder;
import com.ning.http.client.FluentCaseInsensitiveStringsMap;
import com.ning.http.client.HttpResponseBodyPart;
import com.ning.http.client.HttpResponseHeaders;
import com.ning.http.client.Request;
import com.ning.http.client.RequestBuilder;
import com.ning.http.client.Response;
import com.ning.http.client.providers.netty.NettyAsyncHttpProvider;

/**
 * @author Benjamin Hanzelmann
 *
 */
class GetTask<T extends Transfer>
    extends Task
    implements Runnable
{

    private final T download;

    private final String path;

    private final File file;

    private final String checksumPolicy;

    private final LatchGuard latch;

    private volatile Exception exception;

    private final ExceptionWrapper<T> wrapper;

    private final AtomicBoolean deleteFile = new AtomicBoolean( true );

    private final boolean allowResumable;

    private AsyncHttpClient client;

    private final FileProcessor fileProcessor;

    private RepositorySystemSession session;

    private Logger logger;

    private Map<String, String> checksumAlgos;

    private boolean disableResumeSupport;

    private int maxIOExceptionRetry;

    private boolean useCache;

    private final static ConcurrentHashMap<RandomAccessFile, Boolean> activeDownloadFiles =
        new ConcurrentHashMap<RandomAccessFile, Boolean>();

    public GetTask( String path, File file, String checksumPolicy, CountDownLatch latch, T download,
                    ExceptionWrapper<T> wrapper, boolean allowResumable, AsyncHttpClient client,
                    RemoteRepository repository, FileProcessor fileProcessor, RepositorySystemSession session,
                    Logger logger, TransferListener listener, Map<String, String> checksumAlgos,
                    boolean disableResumeSupport, int maxIOExceptionRetry, boolean useCache )
    {
        super( client, repository, listener );
        this.path = path;
        this.file = file;
        this.checksumPolicy = checksumPolicy;
        this.allowResumable = allowResumable;
        this.latch = new LatchGuard( latch );
        this.download = download;
        this.wrapper = wrapper;
        this.client = client;
        this.fileProcessor = fileProcessor;
        this.session = session;
        this.logger = logger;
        this.checksumAlgos = checksumAlgos;
        this.disableResumeSupport = disableResumeSupport;
        this.maxIOExceptionRetry = maxIOExceptionRetry;
        this.useCache = useCache;
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
        final String uri = normalizeUri( path );
        final TransferResource transferResource = new DefaultTransferResource( repository.getUrl(), path, file );
        final boolean ignoreChecksum = RepositoryPolicy.CHECKSUM_POLICY_IGNORE.equals( checksumPolicy );
        CompletionHandler completionHandler = null;

        final FileLockCompanion fileLockCompanion = createOrGetTmpFile( file, allowResumable );

        try
        {
            long length = 0;
            if ( fileLockCompanion.getFile() != null )
            {
                fileProcessor.mkdirs( fileLockCompanion.getFile().getParentFile() );
            }

            // Position the file to the end in case we are resuming an aborted download.
            final RandomAccessFile resumableFile =
                fileLockCompanion.getFile() == null ? null : new RandomAccessFile( fileLockCompanion.getFile(), "rw" );
            if ( resumableFile != null )
            {
                length = resumableFile.length();
            }

            FluentCaseInsensitiveStringsMap headers = new FluentCaseInsensitiveStringsMap();
            if ( !useCache )
            {
                headers.add( "Pragma", "no-cache" );
            }
            headers.add( "Accept", "text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2" );

            Request request = null;
            final AtomicInteger maxRequestTry = new AtomicInteger();
            final AtomicBoolean closeOnComplete = new AtomicBoolean( false );

            /**
             * If length > 0, it means we are resuming a interrupted download. If that's the case, we can't re-use the
             * current httpClient because compression is enabled, and supporting compression per request is not
             * supported in ahc and may never has it could have a performance impact.
             */
            if ( length > 0 )
            {
                Builder configBuilder = new AsyncHttpClientConfig.Builder( client.getConfig() );
                // AsyncHttpClientConfig config = createConfig( session, repository, false );
                configBuilder.setCompressionEnabled( false );
                AsyncHttpClientConfig config = configBuilder.build();
                client = new AsyncHttpClient( new NettyAsyncHttpProvider( config ) );
                request = client.prepareGet( uri ).setRangeOffset( length ).setHeaders( headers ).build();
                closeOnComplete.set( true );
            }
            else
            {
                request = client.prepareGet( uri ).setHeaders( headers ).build();
            }

            final Request activeRequest = request;
            completionHandler =
                new GetCompletionHandler( transferResource, client, logger, RequestType.GET, resumableFile,
                                          maxRequestTry, client, ignoreChecksum, uri, transferResource,
                                          fileLockCompanion, activeRequest, closeOnComplete );

            try
            {
                if ( file == null )
                {
                    if ( !isResourceExisting( uri ) )
                    {
                        throw new ResourceDoesNotExistException( "Could not find " + uri + " in " + repository.getUrl() );
                    }
                    latch.countDown();
                }
                else
                {
                    if ( listener != null )
                    {
                        completionHandler.addTransferListener( listener );
                        listener.transferInitiated( newEvent( transferResource, null, RequestType.GET,
                                                              EventType.INITIATED ) );
                    }

                    client.executeRequest( request, completionHandler );
                }
            }
            catch ( Exception ex )
            {
                try
                {
                    if ( resumableFile != null )
                    {
                        resumableFile.close();
                    }
                }
                catch ( IOException ex2 )
                {
                }
                deleteFile( fileLockCompanion );
                exception = ex;
                latch.countDown();
            }
        }
        catch ( Throwable t )
        {
            deleteFile( fileLockCompanion );
            try
            {
                if ( Exception.class.isAssignableFrom( t.getClass() ) )
                {
                    exception = Exception.class.cast( t );
                }
                else
                {
                    exception = new Exception( t );
                }
                if ( listener != null )
                {
                    listener.transferFailed( newEvent( transferResource, exception, RequestType.GET, EventType.FAILED ) );
                }
            }
            finally
            {
                latch.countDown();
            }
        }
    }

    private void deleteFile( FileLockCompanion fileLockCompanion )
    {
        if ( fileLockCompanion.getFile() != null && deleteFile.get() )
        {
            releaseLock( fileLockCompanion );
            activeDownloadFiles.remove( fileLockCompanion.getFile() );
            fileLockCompanion.getFile().delete();
        }
    }

    private boolean verifyChecksum( File file, String path, String actual, String ext )
        throws ChecksumFailureException
    {
        File tmp = getTmpFile( file.getPath() + ext );
        try
        {
            try
            {
                Response response = client.prepareGet( path + ext ).execute().get();

                if ( response.getStatusCode() == HttpURLConnection.HTTP_NOT_FOUND )
                {
                    return false;
                }

                OutputStream fs = new BufferedOutputStream( new FileOutputStream( tmp ) );
                try
                {
                    InputStream is = response.getResponseBodyAsStream();
                    final byte[] buffer = new byte[4 * 1024];
                    int n = 0;
                    while ( -1 != ( n = is.read( buffer ) ) )
                    {
                        fs.write( buffer, 0, n );
                    }
                    fs.flush();
                }
                finally
                {
                    fs.close();
                }

            }
            catch ( Exception ex )
            {
                throw new ChecksumFailureException( ex );
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

    private void rename( File from, File to )
        throws IOException
    {
        fileProcessor.move( from, to );
    }

    private String stripExtension( File tmpFile )
    {
        return tmpFile.getPath().substring( 0, tmpFile.getPath().lastIndexOf( "." ) );
    }

    /**
     * Create a {@link FileLockCompanion} containing a reference to a temporary {@link File} used when downloading a
     * remote file. If a local and incomplete version of a file is available, use that file and resume bytes
     * downloading. To prevent multiple process trying to resume the same file, a {@link FileLock} companion to the
     * tmeporary file is created and used to prevent concurrency issue.
     * 
     * @param path The path to download to
     * @param allowResumable If {@code true}, allow resumable download. Otherwise disallow resume.
     * @return a configured
     */
    private FileLockCompanion createOrGetTmpFile( String path, boolean allowResumable )
    {
        if ( !disableResumeSupport && allowResumable )
        {
            File f = new File( path );
            File parentFile = f.getParentFile();
            if ( parentFile.isDirectory() )
            {

                FilenameFilter ahcExtension = new FilenameFilter()
                {
                    public boolean accept( File dir, String name )
                    {
                        if ( name.indexOf( "." ) > 0 && name.lastIndexOf( "." ) == name.indexOf( ".ahc" ) )
                        {
                            return true;
                        }
                        return false;
                    }
                };

                for ( File tmpFile : parentFile.listFiles( ahcExtension ) )
                {

                    if ( tmpFile.length() > 0 )
                    {
                        // cut off .ahc
                        String realPath = stripExtension( tmpFile );

                        if ( realPath.equals( path ) )
                        {
                            File newFile = tmpFile;
                            synchronized ( activeDownloadFiles )
                            {
                                FileLockCompanion fileLockCompanion;
                                fileLockCompanion = lockFile( tmpFile );
                                logger.debug( String.format( "Found an incomplete download for file %s.", path ) );

                                if ( fileLockCompanion.getLock() == null )
                                {
                                    /**
                                     * Lock failed so we need to regenerate a new tmp file.
                                     */
                                    newFile = getTmpFile( path );
                                    fileLockCompanion = lockFile( newFile );
                                }
                                return fileLockCompanion;
                            }
                        }
                    }
                }
            }
        }
        return new FileLockCompanion( getTmpFile( path ), null );
    }

    private FileLockCompanion createOrGetTmpFile( File file, boolean allowResumable )
    {
        if ( file != null )
        {
            return createOrGetTmpFile( file.getPath(), allowResumable );
        }
        else
        {
            return new FileLockCompanion( null, null );
        }
    }

    private File getTmpFile( String path )
    {
        File file;
        do
        {
            file = new File( path + ".ahc" + UUID.randomUUID().toString().replace( "-", "" ).substring( 0, 16 ) );
        }
        while ( file.exists() );
        return file;
    }

    /**
     * Create a temporary file used to lock ({@link FileLock}) an associated incomplete file {@link File}. The
     * {@link FileLock}'s name is derived from the original file, appending ".lock" at the end. Usually this method gets
     * executed when a download fail to complete because the JVM goes down. In that case we resume the incomplete
     * download and to prevent multiple process to work on the same file, we use a dedicated {@link FileLock}.
     * 
     * @param tmpFile a file on which we want to create a temporary lock file.
     * @return a {@link FileLockCompanion} contains the {@link File} and a {@link FileLock} if it was possible to lock
     *         the file.
     */
    private FileLockCompanion lockFile( File tmpFile )
    {
        try
        {
            // On Unix tmpLock.getChannel().tryLock may not fail inside the same process, so we must keep track
            // of current resumable file.
            if ( activeDownloadFiles.containsKey( tmpFile ) )
            {
                return new FileLockCompanion( tmpFile, null );
            }

            RandomAccessFile tmpLock = new RandomAccessFile( tmpFile.getPath() + ".lock", "rw" );
            FileLock lock = tmpLock.getChannel().tryLock( 0, 1, false );

            if ( lock != null )
            {
                activeDownloadFiles.put( tmpLock, Boolean.TRUE );
            }
            else if ( lock == null )
            {
                try
                {
                    tmpLock.close();
                }
                catch ( IOException ex )
                {

                }
            }

            return new FileLockCompanion( tmpFile, lock, tmpFile.getPath() + ".lock" );
        }
        catch ( OverlappingFileLockException ex )
        {
            return new FileLockCompanion( tmpFile, null );
        }
        catch ( IOException ex )
        {
            return new FileLockCompanion( tmpFile, null );
        }
    }

    private void releaseLock( FileLockCompanion fileLockCompanion )
    {
        try
        {
            if ( fileLockCompanion.getLock() != null )
            {
                try
                {
                    fileLockCompanion.getLock().channel().close();
                    fileLockCompanion.getLock().release();
                }
                finally
                {
                    if ( fileLockCompanion.getLockedPathFile() != null )
                    {
                        new File( fileLockCompanion.getLockedPathFile() ).delete();
                    }
                }
            }
        }
        catch ( IOException e )
        {
            // Ignore.
        }
    }

    /**
     * @author Benjamin Hanzelmann
     */
    private final class GetCompletionHandler
        extends CompletionHandler
    {
        /**
             * 
             */
        private final RandomAccessFile resumableFile;

        /**
             * 
             */
        private final AtomicInteger maxRequestTry;

        /**
             * 
             */
        private final AsyncHttpClient activeHttpClient;

        /**
             * 
             */
        private final boolean ignoreChecksum;

        /**
             * 
             */
        private final String uri;

        /**
             * 
             */
        private final TransferResource transferResource;

        /**
             * 
             */
        private final FileLockCompanion fileLockCompanion;

        /**
             * 
             */
        private final Request activeRequest;

        /**
             * 
             */
        private final AtomicBoolean closeOnComplete;

        private final AtomicBoolean seekEndOnFile = new AtomicBoolean( false );

        private final AtomicBoolean handleTmpFile = new AtomicBoolean( true );

        /**
         * @param transferResource
         * @param httpClient
         * @param logger
         * @param requestType
         * @param resumableFile
         * @param maxRequestTry
         * @param activeHttpClient
         * @param ignoreChecksum
         * @param uri
         * @param transferResource2
         * @param fileLockCompanion
         * @param activeRequest
         * @param closeOnComplete
         */
        private GetCompletionHandler( TransferResource transferResource, AsyncHttpClient httpClient, Logger logger,
                                      RequestType requestType, RandomAccessFile resumableFile,
                                      AtomicInteger maxRequestTry, AsyncHttpClient activeHttpClient,
                                      boolean ignoreChecksum, String uri, TransferResource transferResource2,
                                      FileLockCompanion fileLockCompanion, Request activeRequest,
                                      AtomicBoolean closeOnComplete )
        {
            super( transferResource, httpClient, logger, requestType );
            this.resumableFile = resumableFile;
            this.maxRequestTry = maxRequestTry;
            this.activeHttpClient = activeHttpClient;
            this.ignoreChecksum = ignoreChecksum;
            this.uri = uri;
            this.transferResource = transferResource2;
            this.fileLockCompanion = fileLockCompanion;
            this.activeRequest = activeRequest;
            this.closeOnComplete = closeOnComplete;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public STATE onHeadersReceived( final HttpResponseHeaders headers )
            throws Exception
        {

            FluentCaseInsensitiveStringsMap h = headers.getHeaders();
            String rangeByteValue = h.getFirstValue( "Content-Range" );
            // Make sure the server acceptance of the range requests headers
            if ( rangeByteValue != null && rangeByteValue.compareToIgnoreCase( "none" ) != 0 )
            {
                seekEndOnFile.set( true );
            }
            return super.onHeadersReceived( headers );
        }

        @Override
        public void onThrowable( Throwable t )
        {
            try
            {
                /**
                 * If an IOException occurs, let's try to resume the request based on how much bytes has been so far
                 * downloaded. Fail after IOException.
                 */
                if ( maxRequestTry.get() < maxIOExceptionRetry && IOException.class.isAssignableFrom( t.getClass() ) )
                {
                    maxRequestTry.incrementAndGet();
                    Request newRequest =
                        new RequestBuilder( activeRequest ).setRangeOffset( resumableFile.length() ).build();
                    activeHttpClient.executeRequest( newRequest, this );
                    deleteFile.set( false );
                    return;
                }

                if ( closeOnComplete.get() )
                {
                    activeHttpClient.close();
                }

                super.onThrowable( t );
                if ( Exception.class.isAssignableFrom( t.getClass() ) )
                {
                    exception = Exception.class.cast( t );
                }
                else
                {
                    exception = new Exception( t );
                }
                fireTransferFailed();
            }
            catch ( Throwable ex )
            {
                logger.debug( "Unexpected exception", ex );
            }
            finally
            {
                if ( resumableFile != null )
                {
                    try
                    {
                        resumableFile.close();
                    }
                    catch ( IOException ex )
                    {
                    }
                }
                deleteFile( fileLockCompanion );

                latch.countDown();
                removeListeners();
            }
        }

        private void removeListeners()
        {
            removeTransferListener( listener );
        }

        @Override
        public STATE onBodyPartReceived( final HttpResponseBodyPart content )
            throws Exception
        {
            if ( status() != null && ( status().getStatusCode() == 200 || status().getStatusCode() == 206 ) )
            {
                byte[] bytes = content.getBodyPartBytes();
                try
                {
                    // If the content-range header was present, save the bytes at the end of the file
                    // as we are resuming an existing download.
                    if ( seekEndOnFile.get() )
                    {
                        resumableFile.seek( fileLockCompanion.getFile().length() );

                        // No need to seek again.
                        seekEndOnFile.set( false );
                    }
                    resumableFile.write( bytes );
                }
                catch ( IOException ex )
                {
                    return AsyncHandler.STATE.ABORT;
                }
            }
            return super.onBodyPartReceived( content );
        }

        @Override
        public Response onCompleted( Response r )
            throws Exception
        {
            try
            {
                deleteFile.set( true );
                try
                {
                    resumableFile.close();
                }
                catch ( IOException ex )
                {
                }

                final Response response = super.onCompleted( r );

                handleResponseCode( uri, response.getStatusCode(), response.getStatusText() );

                if ( !ignoreChecksum )
                {
                    activeHttpClient.getConfig().executorService().execute( new Runnable()
                    {
                        public void run()
                        {
                            try
                            {
                                try
                                {
                                    Map<String, Object> checksums =
                                        ChecksumUtils.calc( fileLockCompanion.getFile(), checksumAlgos.keySet() );
                                    if ( !verifyChecksum( file, uri, (String) checksums.get( "SHA-1" ), ".sha1" )
                                        && !verifyChecksum( file, uri, (String) checksums.get( "MD5" ), ".md5" ) )
                                    {
                                        throw new ChecksumFailureException( "Checksum validation failed"
                                            + ", no checksums available from the repository" );
                                    }
                                }
                                catch ( ChecksumFailureException e )
                                {
                                    if ( RepositoryPolicy.CHECKSUM_POLICY_FAIL.equals( checksumPolicy ) )
                                    {
                                        throw e;
                                    }
                                    if ( listener != null )
                                    {
                                        listener.transferCorrupted( newEvent( transferResource, e, RequestType.GET,
                                                                              EventType.CORRUPTED ) );
                                    }
                                }
                            }
                            catch ( Exception ex )
                            {
                                exception = ex;
                            }
                            finally
                            {
                                if ( exception == null )
                                {
                                    try
                                    {
                                        rename( fileLockCompanion.getFile(), file );
                                        releaseLock( fileLockCompanion );
                                    }
                                    catch ( IOException e )
                                    {
                                        exception = e;
                                    }
                                }
                                else
                                {
                                    deleteFile( fileLockCompanion );
                                }

                                latch.countDown();
                                if ( closeOnComplete.get() )
                                {
                                    activeHttpClient.close();
                                }
                            }
                        }
                    } );
                }
                else
                {

                    rename( fileLockCompanion.getFile(), file );
                    releaseLock( fileLockCompanion );
                    handleTmpFile.set( false );

                    // asyncHttpClient.close may takes time before all connections get closed.
                    // We unlatch first.
                    latch.countDown();
                    if ( closeOnComplete.get() )
                    {
                        activeHttpClient.close();
                    }
                }
                removeListeners();

                return response;
            }
            catch ( Exception ex )
            {
                exception = ex;
                throw ex;
            }
            finally
            {
                try
                {
                    if ( handleTmpFile.get() && fileLockCompanion.getFile() != null )
                    {
                        if ( exception != null )
                        {
                            deleteFile( fileLockCompanion );
                        }
                        else if ( ignoreChecksum )
                        {
                            rename( fileLockCompanion.getFile(), file );
                            releaseLock( fileLockCompanion );
                        }
                    }
                }
                catch ( IOException ex )
                {
                    exception = ex;
                }
            }
        }
    }

    public static GetTask<MetadataTransfer> metadataTask( String resource, MetadataDownload download,
                                                          CountDownLatch latch,
                                              ConnectorConfiguration configuration )
    {
        return new GetTask<MetadataTransfer>( resource, download.getFile(), download.getChecksumPolicy(), latch,
                                              download, AsyncRepositoryConnector.METADATA_EXCEPTION_WRAPPER, false,
                                              configuration.getHttpClient(), configuration.getRepository(),
                                              configuration.getFileProcessor(), configuration.getSession(),
                                              configuration.getLogger(), configuration.getListener(),
                                              configuration.getChecksumAlgos(), configuration.isDisableResumeSupport(),
                                              configuration.getMaxIOExceptionRetry(),
                                              configuration.isUseCache() );
    }

    public static GetTask<ArtifactTransfer> artifactTask( String resource, ArtifactDownload download,
                                                          CountDownLatch latch,
                                              ConnectorConfiguration configuration )
    {
        return new GetTask<ArtifactTransfer>( resource, download.isExistenceCheck() ? null : download.getFile(),
                                              download.getChecksumPolicy(), latch, download,
                                              AsyncRepositoryConnector.ARTIFACT_EXCEPTION_WRAPPER, true,
                                              configuration.getHttpClient(), configuration.getRepository(),
                                              configuration.getFileProcessor(), configuration.getSession(),
                                              configuration.getLogger(), configuration.getListener(),
                                              configuration.getChecksumAlgos(), configuration.isDisableResumeSupport(),
                                              configuration.getMaxIOExceptionRetry(),
                                              configuration.isUseCache() );
    }
}
