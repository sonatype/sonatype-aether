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

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
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

    private Iterator<String> checksumAlgoIterator;

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

            SimpleAsyncHttpClient derivedClient = deriveClient( requestUrl( "" ) );

            if ( transfer.isExistenceCheck() )
            {
                futureResponse = derivedClient.head();
            }
            else
            {
                consumer = newConsumer();
                futureResponse =
                    derivedClient.get( consumer );

                downloadChecksum( configuration.getChecksumAlgos().keySet().iterator().next() );
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
            cancelLeftoverChecksumDownloads();
            closeDerivedClients();
            advanceState();
        }
    }

    private void cancelLeftoverChecksumDownloads()
    {
        for ( Entry<String, Future<Response>> entry : checksumDownloads.entrySet() )
        {
            Future<Response> future = entry.getValue();
            try {
                // see if download is done "soon"
                Response response = future.get( 1, TimeUnit.SECONDS );
                handleResponseCode( url, response.getStatusCode(), response.getStatusText() );

                String extension = configuration.getChecksumAlgos().get( entry.getKey() );
                configuration.getFileProcessor().move( extensionFile( extension + ".tmp" ), extensionFile( extension ) );
            }
            catch ( Exception e )
            {
                // we are only cleaning up here, don't propagate
                future.cancel( true );
                File tmpfile = extensionFile( entry.getKey() + ".tmp" );
                tmpfile.delete();
            }
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

        if ( future == null )
        {
            future = downloadChecksum( algorithm );
        }

        Response response = future.get();

        checksumDownloads.remove( algorithm );

        handleResponseCode( url, response.getStatusCode(), response.getStatusText() );

        String extension = configuration.getChecksumAlgos().get( algorithm );
        configuration.getFileProcessor().move( extensionFile( extension + ".tmp" ), extensionFile( extension ) );
    }

    private Future<Response> downloadChecksum( String algorithm )
        throws IOException
    {
        Map<String, String> checksumAlgos = configuration.getChecksumAlgos();
        String extension = checksumAlgos.get( algorithm );

        File targetFile = extensionFile( extension + ".tmp" );
        configuration.getFileProcessor().mkdirs( targetFile.getParentFile() );

        BodyConsumer target = new FileBodyConsumer( new RandomAccessFile( targetFile, "rw" ) );
        SimpleAsyncHttpClient derivedClient = deriveClient( requestUrl( extension ) );

        Future<Response> future = derivedClient.get( target );

        checksumDownloads.put( algorithm, future );

        return future;
    }

    private String requestUrl( String extension )
    {
        return url + extension;
    }
}
