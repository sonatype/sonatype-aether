package org.sonatype.aether.connector.file;

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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.codehaus.plexus.util.IOUtil;
import org.sonatype.aether.ArtifactTransferException;
import org.sonatype.aether.ChecksumFailureException;
import org.sonatype.aether.RemoteRepository;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.TransferCancelledException;
import org.sonatype.aether.TransferEvent;
import org.sonatype.aether.TransferListener;
import org.sonatype.aether.TransferResource;
import org.sonatype.aether.TransferEvent.EventType;
import org.sonatype.aether.TransferEvent.RequestType;
import org.sonatype.aether.spi.connector.ArtifactDownload;
import org.sonatype.aether.spi.connector.ArtifactTransfer;
import org.sonatype.aether.spi.connector.ArtifactUpload;
import org.sonatype.aether.spi.connector.Transfer.State;
import org.sonatype.aether.util.ChecksumUtils;
import org.sonatype.aether.util.listener.DefaultTransferEvent;
import org.sonatype.aether.util.listener.DefaultTransferResource;

public class ArtifactWorker
    extends FileConnectorWorker
    implements Runnable
{

    private static LinkedHashMap<String, String> checksumAlgos;

//    private ArtifactTransfer transfer;
    private TransferWrapper transfer;

    private RemoteRepository repository;

    private DefaultLayout layout;

//    private RepositorySystemSession session;

//    private TransferListener listener;

    static
    {
        checksumAlgos = new LinkedHashMap<String, String>( 4 );
        checksumAlgos.put( "SHA-1", ".sha1" );
        checksumAlgos.put( "MD5", ".md5" );
    }

    private ArtifactWorker( ArtifactTransfer transfer, RemoteRepository repository, Direction direction,
                            RepositorySystemSession session )
    {
        super( session, repository, direction );
        this.transfer = new TransferWrapper( transfer );
        // this.direction = direction;
        this.repository = repository;
//        this.session = session;

//        this.listener = session.getTransferListener();

        this.layout = new DefaultLayout();
    }

    public ArtifactWorker( ArtifactUpload transfer, RemoteRepository repository, RepositorySystemSession session )
    {
        this( transfer, repository, Direction.UPLOAD, session );
    }

    public ArtifactWorker( ArtifactDownload transfer, RemoteRepository repository, RepositorySystemSession session )
    {
        this( transfer, repository, Direction.DOWNLOAD, session );
    }

    public void run()
    {
        File target = null;
        File checksumFile = null;
        try
        {
            transfer.setState( State.NEW );
            DefaultTransferEvent event = newEvent( transfer, repository );
            fireInitiated( event );

            File baseDir = new File( repository.getUrl() );
            File localFile = transfer.getFile();
            File repoFile = new File( baseDir, layout.getPath( transfer.getArtifact() ) );

            File src = null;
            String checksumAlgo = null;

            switch ( direction )
            {
                case UPLOAD:
                    src = localFile;
                    target = repoFile;
                    checksumAlgo = "SHA-1";
                    break;
                case DOWNLOAD:
                    src = repoFile;
                    target = localFile;

                    for ( Map.Entry<String, String> entry : checksumAlgos.entrySet() )
                    {
                        checksumFile = new File( repoFile.getPath() + entry.getValue() );
                        if ( checksumFile.exists() )
                        {
                            checksumAlgo = entry.getKey();
                        }
                        break;
                    }
                    if ( checksumAlgo == null )
                    {
                        throw new ChecksumFailureException( "No supported checksum algorithm found." );
                    }
                    break;
            }

            DigestInputStream dis;
            try
            {
                dis = new DigestInputStream( new FileInputStream( src ), MessageDigest.getInstance( checksumAlgo ) );
            }
            catch ( NoSuchAlgorithmException e )
            {
                throw new ChecksumFailureException( "No supported checksum algorithm found." );
            }

            transfer.setState( State.ACTIVE );
            event = newEvent( transfer, repository );
            fireStarted( event );

            IOUtil.copy( dis, new FileOutputStream( target ) );

            // write target checksum file
            String checksumExtension = checksumAlgos.get( checksumAlgo );
            checksumFile = new File( target.getPath() + checksumExtension );
            String actualChecksum = new String( dis.getMessageDigest().digest() );
            IOUtil.copy( actualChecksum, new FileWriter( checksumFile ) );

            // verify checksum
            String expectedChecksum = ChecksumUtils.read( new File( src.getPath() + checksumExtension ) );
            if ( !actualChecksum.equals( expectedChecksum ) )
            {
                throw new ChecksumFailureException( expectedChecksum, actualChecksum );
            }

        }
        catch ( Throwable t )
        {
            transfer.setException( new ArtifactTransferException( transfer.getArtifact(), repository, t ) );
        }
        finally
        {
            transfer.setState( State.DONE );
            try
            {
                if ( transfer.getException() != null )
                {
                    fireSucceeded( newEvent( transfer, repository ) );
                }
                else
                {
                    // cleanup
                    if ( target != null )
                        target.delete();
                    if ( checksumFile != null )
                        checksumFile.delete();
                    fireFailed( newEvent( transfer, repository ) );

                }
            }
            catch ( TransferCancelledException e )
            {
                // done anyway
            }
        }

    }

}
