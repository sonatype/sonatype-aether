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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;

import org.sonatype.aether.ArtifactTransferException;
import org.sonatype.aether.ChecksumFailureException;
import org.sonatype.aether.MetadataTransferException;
import org.sonatype.aether.RemoteRepository;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.TransferCancelledException;
import org.sonatype.aether.spi.connector.ArtifactDownload;
import org.sonatype.aether.spi.connector.ArtifactTransfer;
import org.sonatype.aether.spi.connector.ArtifactUpload;
import org.sonatype.aether.spi.connector.MetadataDownload;
import org.sonatype.aether.spi.connector.MetadataTransfer;
import org.sonatype.aether.spi.connector.MetadataUpload;
import org.sonatype.aether.spi.connector.Transfer.State;
import org.sonatype.aether.util.ChecksumUtils;
import org.sonatype.aether.util.listener.DefaultTransferEvent;

public class ArtifactWorker
    extends FileConnectorWorker
    implements Runnable
{

    private static LinkedHashMap<String, String> checksumAlgos;

    // private ArtifactTransfer transfer;
    private TransferWrapper transfer;

    private RemoteRepository repository;

    private DefaultLayout layout;

    private CountDownLatch latch = null;

    // private RepositorySystemSession session;

    // private TransferListener listener;

    public void setLatch( CountDownLatch latch )
    {
        this.latch = latch;
    }

    static
    {
        checksumAlgos = new LinkedHashMap<String, String>( 4 );
        checksumAlgos.put( "SHA-1", ".sha1" );
        checksumAlgos.put( "MD5", ".md5" );
    }

    private ArtifactWorker( ArtifactTransfer transfer, RemoteRepository repository, Direction direction,
                            RepositorySystemSession session )
    {
        this( session, repository, direction );
        this.transfer = new TransferWrapper( transfer );
    }

    private ArtifactWorker( MetadataTransfer transfer, RemoteRepository repository, Direction direction,
                            RepositorySystemSession session )
    {
        this( session, repository, direction );
        this.transfer = new TransferWrapper( transfer );
    }

    public ArtifactWorker( ArtifactUpload transfer, RemoteRepository repository, RepositorySystemSession session )
    {
        this( transfer, repository, Direction.UPLOAD, session );
    }

    public ArtifactWorker( ArtifactDownload transfer, RemoteRepository repository, RepositorySystemSession session )
    {
        this( transfer, repository, Direction.DOWNLOAD, session );
    }

    public ArtifactWorker( MetadataDownload transfer, RemoteRepository repository, RepositorySystemSession session )
    {
        this( transfer, repository, Direction.DOWNLOAD, session );
    }
    
    public ArtifactWorker( MetadataUpload transfer, RemoteRepository repository, RepositorySystemSession session )
    {
        this( transfer, repository, Direction.UPLOAD, session );
    }

    public ArtifactWorker( RepositorySystemSession session, RemoteRepository repository, Direction direction )
    {
        super( session, repository, direction );
        this.repository = repository;
        this.layout = new DefaultLayout();
    }

    public void run()
    {
        File target = null;
        try
        {
            transfer.setState( State.NEW );
            DefaultTransferEvent event = newEvent( transfer, repository );
            fireInitiated( event );

            File baseDir = new File( new URI( repository.getUrl() ) );
            File localFile = transfer.getFile();
            File repoFile = null;
            switch ( transfer.getType() )
            {
                case ARTIFACT:
                    repoFile = new File( baseDir, layout.getPath( transfer.getArtifact() ) );
                    break;
                case METADATA:
                    repoFile = new File( baseDir, layout.getPath( transfer.getMetadata() ) );
                    break;
            }

            File src = null;

            switch ( direction )
            {
                case UPLOAD:
                    src = localFile;
                    target = repoFile;
                    break;
                case DOWNLOAD:
                    src = repoFile;
                    target = localFile;

                    break;
            }

            transfer.setState( State.ACTIVE );
            event = newEvent( transfer, repository );
            fireStarted( event );
            event = null;

            target.getParentFile().mkdirs();

            FileChannel in = new FileInputStream( src ).getChannel();
            FileChannel out = new FileOutputStream( target ).getChannel();
            copy( in, out );

            Map<String, Object> crcs = ChecksumUtils.calc( src, checksumAlgos.keySet() );
            switch ( direction )
            {
                case UPLOAD:
                    // write checksum files
                    // FIXME use checksumPolicy
                    for ( Entry<String, Object> crc : crcs.entrySet() )
                    {
                        String name = crc.getKey();
                        Object sum = crc.getValue();

                        if ( sum.getClass().isAssignableFrom( Throwable.class ) )
                        {
                            throw (Throwable) sum;
                        }

                        File crcTarget = new File( target.getPath() + checksumAlgos.get( name ) );
                        FileWriter crcWriter = new FileWriter( crcTarget );
                        crcWriter.write( sum.toString() );
                        crcWriter.close();
                    }
                    break;
                case DOWNLOAD:
                    // verify checksum
                    // FIXME use checksumPolicy
                    boolean verified = false;
                    for ( Entry<String, String> entry : checksumAlgos.entrySet() )
                    {
                        try
                        {
                            String sum = ChecksumUtils.read( new File( src.getPath() + entry.getValue() ) );
                            verified = sum.equalsIgnoreCase( crcs.get( entry.getKey() ).toString() );
                            if ( !verified )
                            {
                                throw new ChecksumFailureException( sum, crcs.get( entry.getKey() ).toString() );
                            }
                            break;
                        }
                        catch ( IOException e )
                        {
                            continue;
                        }
                    }

                    if ( !verified )
                    {
                        throw new ChecksumFailureException( "no supported algorithms found" );
                    }

                    break;
            }

        }
        catch ( Throwable t )
        {
            switch ( transfer.getType() )
            {
                case ARTIFACT:
		            transfer.setException( new ArtifactTransferException( transfer.getArtifact(), repository, t ) );
                    break;
                case METADATA:
                    transfer.setException( new MetadataTransferException( transfer.getMetadata(), repository, t ) );
                    break;
            }
        }
        finally
        {
            transfer.setState( State.DONE );
            try
            {
                if ( transfer.getException() == null )
                {
                    fireSucceeded( newEvent( transfer, repository ) );
                }
                else
                {
                    // cleanup
                    if ( target != null )
                        target.delete();
                    if ( direction.equals( Direction.UPLOAD ) )
                    {
                        // FIXME: delete all checksum files
                    }
                    fireFailed( newEvent( transfer, repository ) );

                }
            }
            catch ( TransferCancelledException e )
            {
                // done anyway
            }
            if ( latch != null )
                latch.countDown();
        }

    }

    private long copy( FileChannel in, FileChannel out )
        throws IOException, TransferCancelledException
    {
        long count = 2000000L;
        ByteBuffer buf = ByteBuffer.allocate( (int) count );

        buf.clear();
        int transferred;
        while ( ( transferred = in.read( buf ) ) >= 0 || buf.position() != 0 )
        {

            DefaultTransferEvent event = newEvent( transfer, repository );
            event.setDataBuffer( buf.array() );
            event.setDataLength( buf.position() );
            event.setDataOffset( 0 );
            event.setTransferredBytes( transferred );
            fireProgressed( event );

            buf.flip();
            out.write( buf );
            buf.compact();
        }
        buf.flip();
        while ( buf.hasRemaining() )
        {
            out.write( buf );
        }
        return count;
    }

}
