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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import org.sonatype.aether.spi.connector.ArtifactUpload;
import org.sonatype.aether.spi.connector.MetadataUpload;
import org.sonatype.aether.util.ChecksumUtils;
import org.sonatype.aether.util.listener.DefaultTransferResource;

import com.ning.http.client.Request;
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
            Request request = newRequest();

            futureResponse = configuration.getHttpClient().put( request, bodyGenerator );
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
        Request request = newRequest( url, extension );
        byte[] bytes = sum.getBytes( "us-ascii" );

        InputStreamBodyGenerator generator = new InputStreamBodyGenerator( new ByteArrayInputStream( bytes ) );
        Future<Response> future = configuration.getHttpClient().put( request, generator );

        futures.add( new FutureBody( future, null ) );
    }

    private Request newRequest()
    {
        return newRequest( url, "" );
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

    private static class FutureBody
    {
        Future<Response> future;

        ProgressedEventHandler progressEventHandler;

        public FutureBody( Future<Response> future, ProgressedEventHandler progressedEventHandler )
        {
            super();
            this.future = future;
            this.progressEventHandler = progressedEventHandler;
        }
    }

}
