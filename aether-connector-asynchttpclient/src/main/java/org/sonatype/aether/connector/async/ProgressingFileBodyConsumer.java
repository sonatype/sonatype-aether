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

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

import org.sonatype.aether.transfer.TransferCancelledException;

import com.ning.http.client.consumers.FileBodyConsumer;

/**
 * @author Benjamin Hanzelmann
 *
 */
public class ProgressingFileBodyConsumer
    extends FileBodyConsumer
    implements Progressor
{

    private long transferredBytes;

    private TransferEventCatapult catapult;

    public ProgressingFileBodyConsumer( RandomAccessFile file, TransferEventCatapult catapult )
    {
        super( file );
        this.catapult = catapult;
    }

    public void fireTransferProgressed( ByteBuffer buffer )
        throws TransferCancelledException
    {
        transferredBytes += buffer.remaining();
        catapult.fireProgressed( buffer, transferredBytes );
    }

    public long getTransferredBytes()
    {
        return transferredBytes;
    }

    @Override
    public void consume( ByteBuffer byteBuffer )
        throws IOException
    {
        ByteBuffer eventBuffer = byteBuffer.slice();

        super.consume( byteBuffer );

        try
        {
            fireTransferProgressed( eventBuffer );
        }
        catch ( TransferCancelledException e )
        {
            IOException ex = new IOException( e.getMessage() );
            e.initCause( e );
            throw ex;
        }
    }

}
