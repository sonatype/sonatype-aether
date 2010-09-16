package org.sonatype.aether.util.listener;

import java.nio.ByteBuffer;

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

/**
 * A TransferEvent using a {@link ByteBuffer} as content.
 * 
 * @author Benjamin Hanzelmann
 */
public class ByteBufferTransferEvent
    extends DefaultTransferEvent
{

    private ByteBuffer buffer;

    private int position;

    public ByteBufferTransferEvent()
    {
        super();
    }

    public ByteBufferTransferEvent( ByteBuffer buffer )
    {
        setByteBuffer( buffer );
    }

    /**
     * Set the ByteBuffer to use as content. If the buffer has a backing array, it is used in {@link #getDataBuffer()}.
     * Otherwise, the content is lazily read from the ByteBuffer. {@link #getDataLength()} and {@link #getDataOffset()}
     * are set according to the position and limit of the buffer.
     * 
     * @param buffer the ByteBuffer to use as the content of this event.
     * @return this event.
     */
    public ByteBufferTransferEvent setByteBuffer( ByteBuffer buffer )
    {
        if ( buffer.hasArray() )
        {
            this.buffer = null;
            setDataBuffer( buffer.array() );
            setDataOffset( buffer.arrayOffset() );
            setDataLength( buffer.remaining() );
        }
        else
        {
            this.buffer = buffer;
            setDataLength( buffer.remaining() );
            this.position = buffer.position();
        }

        return this;
    }

    @Override
    public byte[] getDataBuffer()
    {
        if ( buffer == null )
        {
            return super.getDataBuffer();
        }

        byte[] content = new byte[getDataLength()];
        buffer.get( content );
        buffer.position( position );
        return content;
    }


}
