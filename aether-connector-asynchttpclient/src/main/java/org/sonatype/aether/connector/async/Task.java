package org.sonatype.aether.connector.async;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.transfer.TransferEvent;
import org.sonatype.aether.transfer.TransferListener;
import org.sonatype.aether.transfer.TransferResource;
import org.sonatype.aether.util.listener.DefaultTransferEvent;

import com.ning.http.client.AsyncHttpClient;

public class Task
{

    static class LatchGuard
    {
    
        private final CountDownLatch latch;
    
        private final AtomicBoolean done = new AtomicBoolean( false );
    
        public LatchGuard( CountDownLatch latch )
        {
            this.latch = latch;
        }
    
        public void countDown()
        {
            if ( !done.getAndSet( true ) )
            {
                latch.countDown();
            }
        }
    }

    protected AsyncHttpClient httpClient;

    protected RemoteRepository repository;

    protected TransferListener listener;

    public Task( AsyncHttpClient httpClient, RemoteRepository repository, TransferListener listener )
    {
        super();
        this.httpClient = httpClient;
        this.repository = repository;
        this.listener = listener;
    }

    protected String normalizeUri( String path )
    {
        String tmpUri = buildUrl( path );
        // If we get dav request here, switch to http as no need for dav method.
        String dav = "dav";
        String davHttp = "dav:http";
        if ( tmpUri.startsWith( dav ) )
        {
            if ( tmpUri.startsWith( davHttp ) )
            {
                tmpUri = tmpUri.substring( dav.length() + 1 );
            }
            else
            {
                tmpUri = "http" + tmpUri.substring( dav.length() );
            }
        }
        return tmpUri;
    }

    /**
     * Builds a complete URL string from the repository URL and the relative path passed.
     * 
     * @param path the relative path
     * @return the complete URL
     */
    private String buildUrl( String path )
    {
        final String repoUrl = repository.getUrl();
        path = path.replace( ' ', '+' );

        if ( repoUrl.charAt( repoUrl.length() - 1 ) != '/' )
        {
            return repoUrl + '/' + path;
        }
        return repoUrl + path;
    }

    protected boolean isResourceExisting( String url )
        throws IOException, ExecutionException, InterruptedException, TransferException, AuthorizationException
    {
        int statusCode = httpClient.prepareHead( url ).execute().get().getStatusCode();

        switch ( statusCode )
        {
            case HttpURLConnection.HTTP_OK:
                return true;

            case HttpURLConnection.HTTP_FORBIDDEN:
                throw new AuthorizationException( String.format( "Access denied to %s . Status code %s", url,
                                                                 statusCode ) );

            case HttpURLConnection.HTTP_NOT_FOUND:
                return false;

            case HttpURLConnection.HTTP_UNAUTHORIZED:
                throw new AuthorizationException( String.format( "Access denied to %s . Status code %s", url,
                                                                 statusCode ) );

            default:
                throw new TransferException( "Failed to look for file: " + buildUrl( url ) + ". Return code is: "
                    + statusCode );
        }
    }

    protected TransferEvent newEvent( TransferResource resource, Exception e, TransferEvent.RequestType requestType,
                                    TransferEvent.EventType eventType )
    {
        DefaultTransferEvent event = new DefaultTransferEvent();
        event.setResource( resource );
        event.setRequestType( requestType );
        event.setType( eventType );
        event.setException( e );
        return event;
    }

    protected void handleResponseCode( String url, int responseCode, String responseMsg )
        throws AuthorizationException, ResourceDoesNotExistException, TransferException
    {
        if ( responseCode == HttpURLConnection.HTTP_NOT_FOUND )
        {
            throw new ResourceDoesNotExistException(
                String.format( "Unable to locate resource %s. Error code %s", url, responseCode ) );
        }
    
        if ( responseCode == HttpURLConnection.HTTP_FORBIDDEN || responseCode == HttpURLConnection.HTTP_UNAUTHORIZED ||
            responseCode == HttpURLConnection.HTTP_PROXY_AUTH )
        {
            throw new AuthorizationException(
                String.format( "Access denied to %s. Error code %s, %s", url, responseCode, responseMsg ) );
        }
    
        if ( responseCode >= HttpURLConnection.HTTP_MULT_CHOICE )
        {
            throw new TransferException(
                String.format( "Failed to transfer %s. Error code %s, %s", url, responseCode, responseMsg ) );
        }
    }

}
