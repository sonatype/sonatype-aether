package org.sonatype.aether.connector.async;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.sonatype.aether.spi.connector.ArtifactUpload;
import org.sonatype.aether.spi.connector.MetadataUpload;
import org.sonatype.aether.util.ChecksumUtils;
import org.sonatype.aether.util.listener.DefaultTransferResource;

import com.ning.http.client.Response;
import com.ning.http.client.generators.InputStreamBodyGenerator;

/**
 * @author Benjamin Hanzelmann
 */
public class SimplePutTask
    extends Task
{

    private String path;

    private String url;

    private Future<Response> futureResponse;

    private List<FutureBody> futures = new LinkedList<FutureBody>();

    private TransferEventCatapult catapult;

    public SimplePutTask( ArtifactUpload upload, ConnectorConfiguration configuration )
    {
        this( configuration, new TransferWrapper( upload ) );
    }

    public SimplePutTask( MetadataUpload upload, ConnectorConfiguration configuration )
    {
        this( configuration, new TransferWrapper( upload ) );
    }

    private SimplePutTask( ConnectorConfiguration configuration, TransferWrapper upload )
    {
        super( configuration.getRepository(), configuration.getListener() );
        this.path = upload.getRelativePath();
        this.configuration = configuration;
        this.transfer = upload;
        this.url = url( configuration.getRepository(), upload );
        transferResource = new DefaultTransferResource( repository.getUrl(), path, upload.getFile() );
        this.catapult = TransferEventCatapult.newUploadCatapult( listener, transferResource );
    }

    public void run()
    {
        try
        {
            catapult.fireInitiated();
            advanceState();

            sanityCheck();

            ProgressingFileBodyGenerator bodyGenerator = newGenerator();

            futureResponse = configuration.getHttpClient().put( requestUrl( "" ), bodyGenerator );
            futures.add( new FutureBody( futureResponse, bodyGenerator ) );

            generateAndUploadChecksums();
        }
        catch ( Exception e )
        {
            addException( transfer, e );
            catapult.fireFailed( transfer.getException() );
        }
    }

    private void generateAndUploadChecksums()
        throws IOException, Exception
    {
        Map<String, Object> checksums =
            ChecksumUtils.calc( transfer.getFile(), configuration.getChecksumAlgos().keySet() );
        uploadChecksums( checksums );
    }

    private void uploadChecksums( Map<String, Object> checksums )
        throws Exception
    {
        for ( String key : checksums.keySet() )
        {
            Object value = checksums.get( key );
            if ( value instanceof Exception )
            {
                throw (Exception) value;
            }
            String sum = String.valueOf( value );
            uploadChecksum( key, sum );
        }
    }

    private void uploadChecksum( String algo, String sum )
        throws IOException
    {
        String extension = configuration.getChecksumAlgos().get( algo );
        byte[] bytes = sum.getBytes( "us-ascii" );

        InputStreamBodyGenerator generator = new InputStreamBodyGenerator( new ByteArrayInputStream( bytes ) );
        Future<Response> future = configuration.getHttpClient().put( requestUrl( extension ), generator );

        futures.add( new FutureBody( future, null ) );
    }

    private String requestUrl( String extension )
    {
        return url + extension;
    }

    private ProgressingFileBodyGenerator newGenerator()
    {
        return new ProgressingFileBodyGenerator( transfer.getFile(), catapult );
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
            Thread.currentThread().interrupt();
            addException( transfer, e );
            catapult.fireFailed( e );
        }
        catch ( Exception e )
        {
            addException( transfer, e );
            catapult.fireFailed( e );
        }
        finally
        {
            advanceState();
        }
    }

    private void processResponse()
        throws InterruptedException, ExecutionException, AuthorizationException, ResourceDoesNotExistException,
        TransferException
    {
        for ( FutureBody futureBody : futures )
        {
            Response response = futureBody.future.get();
            handleResponseCode( url, response.getStatusCode(), response.getStatusText() );

            if ( futureBody.progressEventHandler != null )
            {
                catapult.fireSucceeded( futureBody.progressEventHandler.getTransferredBytes() );
            }
        }
    }

    private static class FutureBody
    {
        Future<Response> future;

        Progressor progressEventHandler;

        public FutureBody( Future<Response> future, Progressor progressedEventHandler )
        {
            super();
            this.future = future;
            this.progressEventHandler = progressedEventHandler;
        }
    }

}
