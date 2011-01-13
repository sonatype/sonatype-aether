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

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

import org.sonatype.aether.connector.async.AsyncRepositoryConnector.ExceptionWrapper;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.spi.connector.Transfer;
import org.sonatype.aether.spi.log.Logger;
import org.sonatype.aether.transfer.TransferListener;
import org.sonatype.aether.transfer.TransferResource;
import org.sonatype.aether.transfer.TransferEvent.EventType;
import org.sonatype.aether.transfer.TransferEvent.RequestType;
import org.sonatype.aether.util.ChecksumUtils;
import org.sonatype.aether.util.listener.DefaultTransferResource;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;

class PutTask<T extends Transfer>
    extends Task
    implements Runnable
{

    private final T upload;

    private final ExceptionWrapper<T> wrapper;

    private final String path;

    private final File file;

    private volatile Exception exception;

    private final LatchGuard latch;

    private Map<String, String> checksumAlgos;

    private Logger logger;

    public PutTask( String path, File file, CountDownLatch latch, T upload, ExceptionWrapper<T> wrapper,
                    AsyncHttpClient httpClient, RemoteRepository repository, TransferListener listener,
                    Map<String, String> checksumAlgos, Logger logger )
    {
        super( httpClient, repository, listener );
        this.path = path;
        this.file = file;
        this.upload = upload;
        this.wrapper = wrapper;
        this.latch = new LatchGuard( latch );
        this.checksumAlgos = checksumAlgos;
        this.logger = logger;
    }

    public Exception getException()
    {
        return exception;
    }

    public void run()
    {
        upload.setState( Transfer.State.ACTIVE );
        final DefaultTransferResource transferResource =
            new DefaultTransferResource( repository.getUrl(), path, file );

        try
        {
            final String uri = normalizeUri( path );

            final CompletionHandler completionHandler =
                new PutCompletionHandler( transferResource, httpClient, logger, RequestType.PUT, transferResource, uri );

            if ( listener != null )

            {
                completionHandler.addTransferListener( listener );
                listener.transferInitiated(
                    newEvent( transferResource, null, RequestType.PUT, EventType.INITIATED ) );
            }

            if ( file == null )
            {
                throw new IllegalArgumentException( "no source file specified for upload" );
            }

            transferResource.setContentLength( file.length() );

            httpClient.preparePut( uri ).setBody(
                new ProgressingFileBodyGenerator( file, completionHandler ) ).execute( completionHandler );
        }
        catch ( Exception e )
        {
            try
            {
                if ( listener != null )
                {
                    listener.transferFailed( newEvent( transferResource, e, RequestType.PUT, EventType.FAILED ) );
                }
                exception = e;
            }
            finally
            {
                latch.countDown();
            }
        }
    }

    public void flush()
    {
        wrapper.wrap( upload, exception, repository );
        upload.setState( Transfer.State.DONE );
    }

    public void flush( Exception exception )
    {
        Exception e = this.exception;
        wrapper.wrap( upload, ( e != null ) ? e : exception, repository );
        upload.setState( Transfer.State.DONE );
    }

    private void uploadChecksums( File file, String path )
    {
        try
        {
            Map<String, Object> checksums = ChecksumUtils.calc( file, checksumAlgos.keySet() );
            for ( Map.Entry<String, Object> entry : checksums.entrySet() )
            {
                uploadChecksum( file, path, entry.getKey(), entry.getValue() );
            }
        }
        catch ( IOException e )
        {
            logger.debug( "Failed to upload checksums for " + file + ": " + e.getMessage(), e );
        }
    }

    private void uploadChecksum( File file, String path, String algo, Object checksum )
    {
        try
        {
            if ( checksum instanceof Exception )
            {
                throw (Exception) checksum;
            }

            String ext = checksumAlgos.get( algo );
            path = path + ext;

            uploadChecksum( path, String.valueOf( checksum ) );
        }
        catch ( Exception e )
        {
            logger.debug( "Failed to upload " + algo + " checksum for " + file + ": " + e.getMessage(), e );
        }
    }

    private void uploadChecksum( String path, String checksum )
        throws InterruptedException, ExecutionException, IOException, TransferException
    {
        // Here we go blocking as this is a simple request.
        Response response = httpClient.preparePut( path ).setBody( checksum ).execute().get();

        if ( response == null || response.getStatusCode() >= HttpURLConnection.HTTP_BAD_REQUEST )
        {
            throw new TransferException(
                String.format( "Checksum failed for %s with status code %s", path, response == null
                    ? HttpURLConnection.HTTP_INTERNAL_ERROR
                    : response.getStatusCode() ) );
        }
    }

    /**
     * @author Benjamin Hanzelmann
     *
     */
    private final class PutCompletionHandler
        extends CompletionHandler
    {
        /**
         * 
         */
        private final DefaultTransferResource transferResource;
    
        /**
         * 
         */
        private final String uri;
    
        /**
         * @param transferResource
         * @param httpClient
         * @param logger
         * @param requestType
         * @param transferResource2
         * @param uri
         */
        private PutCompletionHandler( TransferResource transferResource, AsyncHttpClient httpClient, Logger logger,
                                      RequestType requestType, DefaultTransferResource transferResource2, String uri )
        {
            super( transferResource, httpClient, logger, requestType );
            this.transferResource = transferResource2;
            this.uri = uri;
        }
    
        @Override
        public void onThrowable( Throwable t )
        {
            try
            {
                super.onThrowable( t );
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
                    listener.transferFailed(
                        newEvent( transferResource, exception, RequestType.PUT, EventType.FAILED ) );
                }
            }
            finally
            {
                latch.countDown();
            }
        }
    
        @Override
        public Response onCompleted( Response r )
            throws Exception
        {
            try
            {
                Response response = super.onCompleted( r );
                handleResponseCode( uri, response.getStatusCode(), response.getStatusText() );
    
                super.httpClient.getConfig().executorService().execute( new Runnable()
                {
                    public void run()
                    {
                        try
                        {
                            uploadChecksums( file, uri );
                        }
                        catch ( Exception ex )
                        {
                            exception = ex;
                        }
                        finally
                        {
                            latch.countDown();
                        }
                    }
                } );
    
                return r;
            }
    
            catch ( Exception ex )
            {
                exception = ex;
                throw ex;
            }
    
        }
    }

}