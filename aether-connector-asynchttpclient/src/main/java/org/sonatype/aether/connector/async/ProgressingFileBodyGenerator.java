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
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

import org.sonatype.aether.transfer.TransferCancelledException;

import com.ning.http.client.RandomAccessBody;

class ProgressingFileBodyGenerator
    extends com.ning.http.client.generators.FileBodyGenerator implements ProgressedEventHandler
{

    private TransferEventCatapult catapult;

    private long transferredBytes;

    public ProgressingFileBodyGenerator( File file, TransferEventCatapult listener )
    {
        super( file );
        this.catapult = listener;
    }

    @Override
    public RandomAccessBody createBody()
        throws IOException
    {
        return new ProgressingBody( super.createBody() );
    }

    /* (non-Javadoc)
     * @see org.sonatype.aether.connector.async.ProgressedEventHandler#fireTransferProgressed(java.nio.ByteBuffer)
     */
    public void fireTransferProgressed( final ByteBuffer buffer )
        throws TransferCancelledException
    {
        if ( catapult != null )
        {
            transferredBytes += buffer.remaining();
            catapult.fireProgressed( buffer, transferredBytes );
        }
    }

    /* (non-Javadoc)
     * @see org.sonatype.aether.connector.async.ProgressedEventHandler#getTransferredBytes()
     */
    public long getTransferredBytes()
    {
        return transferredBytes;
    }

    final class ProgressingBody
        implements RandomAccessBody
    {

        final RandomAccessBody delegate;

        private ProgressingWritableByteChannel channel;

        public ProgressingBody( RandomAccessBody delegate )
        {
            this.delegate = delegate;
        }

        public long getContentLength()
        {
            return delegate.getContentLength();
        }

        public long read( ByteBuffer buffer )
            throws IOException
        {
            ByteBuffer eventBuffer = buffer.slice();
            long read = delegate.read( buffer );
            if ( read > 0 )
            {
                try
                {
                    eventBuffer.limit( (int) read );
                    fireTransferProgressed( eventBuffer );
                }
                catch ( TransferCancelledException e )
                {
                    throw (IOException) new IOException( e.getMessage() ).initCause( e );
                }
            }
            return read;
        }

        public long transferTo( long position, long count, WritableByteChannel target )
            throws IOException
        {
            ProgressingWritableByteChannel dst = channel;
            if ( dst == null || dst.delegate != target )
            {
                channel = dst = new ProgressingWritableByteChannel( target );
            }
            return delegate.transferTo( position, Math.min( count, 1024 * 16 ), dst );
        }

        public void close()
            throws IOException
        {
            delegate.close();
        }

        final class ProgressingWritableByteChannel
            implements WritableByteChannel
        {

            final WritableByteChannel delegate;

            public ProgressingWritableByteChannel( WritableByteChannel delegate )
            {
                this.delegate = delegate;
            }

            public boolean isOpen()
            {
                return delegate.isOpen();
            }

            public void close()
                throws IOException
            {
                delegate.close();
            }

            public int write( ByteBuffer src )
                throws IOException
            {
                ByteBuffer event = src.slice();
                int written = delegate.write( src );
                if ( written > 0 )
                {
                    try
                    {
                        event.limit( written );
                        fireTransferProgressed( event );
                    }
                    catch ( TransferCancelledException e )
                    {
                        throw (IOException) new IOException( e.getMessage() ).initCause( e );
                    }
                }
                return written;
            }

        }

    }

}
