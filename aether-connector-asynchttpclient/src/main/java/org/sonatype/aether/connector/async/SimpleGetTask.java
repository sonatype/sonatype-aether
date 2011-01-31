package org.sonatype.aether.connector.async;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.sonatype.aether.repository.RepositoryPolicy;
import org.sonatype.aether.spi.connector.ArtifactDownload;
import org.sonatype.aether.spi.connector.MetadataDownload;
import org.sonatype.aether.transfer.ChecksumFailureException;
import org.sonatype.aether.transfer.TransferCancelledException;
import org.sonatype.aether.util.ChecksumUtils;
import org.sonatype.aether.util.listener.DefaultTransferResource;

import com.ning.http.client.BodyConsumer;
import com.ning.http.client.Response;
import com.ning.http.client.SimpleAsyncHttpClient;
import com.ning.http.client.consumers.FileBodyConsumer;

/**
 * @author Benjamin Hanzelmann
 */
public class SimpleGetTask
    extends Task
{

    private final String path;

    private final String url;

    private Future<Response> futureResponse;

    private Map<String, Future<Response>> checksumDownloads = new LinkedHashMap<String, Future<Response>>( 4 );

    private TransferEventCatapult catapult;

    private ProgressingFileBodyConsumer consumer;

    public SimpleGetTask( ArtifactDownload download, ConnectorConfiguration configuration )
    {
        this( configuration, new TransferWrapper( download ) );
    }

    public SimpleGetTask( MetadataDownload download, ConnectorConfiguration configuration )
    {
        this( configuration, new TransferWrapper( download ) );
    }

    private SimpleGetTask( ConnectorConfiguration configuration, TransferWrapper download )
    {
        super( configuration.getRepository(), configuration.getListener() );
        this.path = download.getRelativePath();
        this.configuration = configuration;
        this.transfer = download;
        this.url = url( configuration.getRepository(), download );
        transferResource = new DefaultTransferResource( repository.getUrl(), path, download.getFile() );
        this.catapult = TransferEventCatapult.newDownloadCatapult( listener, transferResource );
    }

    public void run()
    {
        try
        {
            catapult.fireInitiated();
            advanceState();

            sanityCheck();

            if ( transfer.isExistenceCheck() )
            {
                futureResponse = configuration.getHttpClient().derive().setUrl( requestUrl( "" ) ).build().head();
            }
            else
            {
                consumer = newConsumer();
                futureResponse =
                    configuration.getHttpClient().derive().setUrl( requestUrl( "" ) ).build().get( consumer );

                for ( String algo : configuration.getChecksumAlgos().keySet() )
                {
                    checksumDownloads.put( algo, downloadChecksum( algo ) );
                }
            }
        }
        catch ( Exception e )
        {
            addException( transfer, e );
            catapult.fireFailed( transfer.getException() );
        }
    }

    public void flush()
    {
        if ( alreadyFailed() )
        {
            return;
        }
        try
        {
            processResponse();
        }
        catch ( InterruptedException e )
        {
            addException( transfer, e );
            Thread.currentThread().interrupt();
            catapult.fireFailed( transfer.getException() );
        }
        catch ( Throwable e )
        {
            addException( transfer, e );
            catapult.fireFailed( transfer.getException() );
        }
        finally
        {
            try
            {
                flushFutures( checksumDownloads.values() );
            }
            catch ( Exception e )
            {
                // we tried...
            }
            advanceState();
        }
    }

    private void flushFutures( Collection<Future<Response>> values )
        throws InterruptedException, ExecutionException
    {
        for (Future<Response> future : values) {
            future.get();
        }
        
    }

    private ProgressingFileBodyConsumer newConsumer()
        throws IOException
    {
        File file = tmpFile();
        configuration.getFileProcessor().mkdirs( file.getParentFile() );
        RandomAccessFile raf = new RandomAccessFile( file, "rw" );
        return new ProgressingFileBodyConsumer( raf, catapult );
    }

    private File tmpFile()
        throws IOException
    {

        return extensionFile( ".tmp" );
    }

    private File extensionFile( String extension )
    {
        return new File( transfer.getFile().getAbsolutePath() + extension );
    }

    public void processResponse()
        throws InterruptedException, ExecutionException, AuthorizationException, ResourceDoesNotExistException,
        TransferException, IOException, ChecksumFailureException, TransferCancelledException
    {
        Response response = futureResponse.get();

        handleResponseCode( url, response.getStatusCode(), response.getStatusText() );

        if ( !transfer.isExistenceCheck() )
        {
            verifyChecksum();

            configuration.getFileProcessor().move( tmpFile(), transfer.getFile() );
        }

        long transferredBytes = consumer == null ? 0 : consumer.getTransferredBytes();

        catapult.fireSucceeded( transferredBytes );
    }

    private void verifyChecksum()
        throws ChecksumFailureException, IOException, TransferCancelledException
    {
        if ( RepositoryPolicy.CHECKSUM_POLICY_IGNORE.equals( transfer.getChecksumPolicy() ) )
        {
            return;
        }
        Map<String, String> checksumAlgos = configuration.getChecksumAlgos();
        Map<String, Object> crcs = ChecksumUtils.calc( tmpFile(), checksumAlgos.keySet() );

        boolean verified = false;
        try
        {
            for ( String algorithm : checksumAlgos.keySet() )
            {
                String sum;
                try
                {
                    retrieveChecksum( algorithm );
                    sum = readChecksum( algorithm );
                }
                catch ( Exception e )
                {
                    // skip verify - try next algorithm
                    continue;
                }

                verified = sum.equalsIgnoreCase( crcs.get( algorithm ).toString() );
                if ( !verified )
                {
                    throw new ChecksumFailureException( crcs.get( algorithm ).toString(), sum );
                }
                break;
            }

            // all algorithms checked
            if ( !verified )
            {
                throw new ChecksumFailureException( "no supported algorithms found" );
            }
        }
        catch ( ChecksumFailureException e )
        {
            if ( RepositoryPolicy.CHECKSUM_POLICY_FAIL.equals( transfer.getChecksumPolicy() ) )
            {
                throw e;
            }

            catapult.fireCorrupted( e );
        }
    }

    private String readChecksum( String algorithm )
        throws IOException
    {
        Map<String, String> algos = configuration.getChecksumAlgos();
        String ext = algos.get( algorithm );
        File checksumFile = extensionFile( ext );
        return ChecksumUtils.read( checksumFile );
    }

    private void retrieveChecksum( String algorithm )
        throws Exception
    {
        Future<Response> future = checksumDownloads.get( algorithm );
        Response response = future.get();
        handleResponseCode( url, response.getStatusCode(), response.getStatusText() );
    }

    private Future<Response> downloadChecksum( String algorithm )
        throws IOException
    {
        Map<String, String> checksumAlgos = configuration.getChecksumAlgos();
        String extension = checksumAlgos.get( algorithm );

        File targetFile = extensionFile( extension );
        configuration.getFileProcessor().mkdirs( targetFile.getParentFile() );

        BodyConsumer target = new FileBodyConsumer( new RandomAccessFile( targetFile, "rw" ) );
        SimpleAsyncHttpClient httpClient = configuration.getHttpClient();
        Future<Response> future = httpClient.derive().setUrl( requestUrl( extension ) ).build().get( target );
        return future;
    }

    private String requestUrl( String extension )
    {
        return url + extension;
    }
}
