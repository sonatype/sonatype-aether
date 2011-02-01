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
