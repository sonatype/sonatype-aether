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
import java.util.concurrent.ExecutionException;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.spi.connector.Transfer;
import org.sonatype.aether.spi.connector.Transfer.State;
import org.sonatype.aether.transfer.ArtifactNotFoundException;
import org.sonatype.aether.transfer.ArtifactTransferException;
import org.sonatype.aether.transfer.MetadataNotFoundException;
import org.sonatype.aether.transfer.MetadataTransferException;
import org.sonatype.aether.transfer.TransferListener;
import org.sonatype.aether.transfer.TransferResource;

import com.ning.http.client.AsyncHttpClient;

public class Task
{

    protected AsyncHttpClient httpClient;

    protected RemoteRepository repository;

    protected TransferListener listener;

    protected TransferResource transferResource;

    protected TransferWrapper transfer;

    protected ConnectorConfiguration configuration;

    public Task( AsyncHttpClient httpClient, RemoteRepository repository, TransferListener listener )
    {
        super();
        this.httpClient = httpClient;
        this.repository = repository;
        this.listener = listener;
    }

    public Task( RemoteRepository repository, TransferListener listener )
    {
        this.repository = repository;
        this.listener = listener;
    }

    protected String normalizeUri( String path )
    {
        String tmpUri = path;
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

    protected String url( RemoteRepository repository, TransferWrapper download )
    {
        String url = repository.getUrl();
        return normalizeUri( url + "/" + download.getRelativePath() );
    }

    protected void advanceState()
    {
        Transfer realTransfer = transfer.getTransfer();
        State state = realTransfer.getState();
        switch ( state )
        {
            case NEW:
                transfer.setState( State.ACTIVE );
                break;
            case ACTIVE:
                transfer.setState( State.DONE );
                break;
        }
    }

    protected void addException( TransferWrapper transfer, Throwable e )
    {
        RemoteRepository repository = configuration.getRepository();
        if ( transfer.getType().equals( TransferWrapper.Type.METADATA ) )
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
        else
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
    }

    protected boolean alreadyFailed()
    {
        return transfer.getException() != null;
    }

    protected void sanityCheck()
    {
        if ( transfer.getFile() == null )
        {
            throw new IllegalArgumentException( "Target file is set to null." );
        }
    }

}
