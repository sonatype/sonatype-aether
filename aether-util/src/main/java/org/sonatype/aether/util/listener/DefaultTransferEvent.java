package org.sonatype.aether.util.listener;

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

import java.nio.ByteBuffer;

import org.sonatype.aether.transfer.TransferEvent;
import org.sonatype.aether.transfer.TransferResource;

/**
 * @author Benjamin Bentmann
 */
public class DefaultTransferEvent
    implements TransferEvent
{

    private EventType type;

    private RequestType requestType;

    private TransferResource resource;

    private ByteBuffer dataBuffer;

    private long transferredBytes;

    private Exception exception;

    public EventType getType()
    {
        return type;
    }

    public DefaultTransferEvent setType( EventType type )
    {
        this.type = type;
        return this;
    }

    public RequestType getRequestType()
    {
        return requestType;
    }

    public DefaultTransferEvent setRequestType( RequestType requestType )
    {
        this.requestType = requestType;
        return this;
    }

    public TransferResource getResource()
    {
        return resource;
    }

    public DefaultTransferEvent setResource( TransferResource resource )
    {
        this.resource = resource;
        return this;
    }

    public long getTransferredBytes()
    {
        return transferredBytes;
    }

    public DefaultTransferEvent setTransferredBytes( long transferredBytes )
    {
        this.transferredBytes = transferredBytes;
        return this;
    }

    public int getDataLength()
    {
        return ( dataBuffer != null ) ? dataBuffer.remaining() : 0;
    }

    public ByteBuffer getDataBuffer()
    {
        return dataBuffer != null ? dataBuffer.asReadOnlyBuffer() : null;
    }

    /**
     * Wraps the given <code>byte[]</code>-array into a {@link ByteBuffer} as the content for this event.
     * 
     * @param buffer The array to use, must not be {@code null}.
     * @param offset the starting point of valid bytes in the array.
     * @param length the number of valid bytes.
     * @return This event for chaining, never {@code null}.
     */
    public DefaultTransferEvent setDataBuffer( byte[] buffer, int offset, int length )
    {
        return setDataBuffer( ByteBuffer.wrap( buffer, offset, length ) );
    }

    public DefaultTransferEvent setDataBuffer( ByteBuffer dataBuffer )
    {
        this.dataBuffer = dataBuffer;
        return this;
    }

    public Exception getException()
    {
        return exception;
    }

    public DefaultTransferEvent setException( Exception exception )
    {
        this.exception = exception;
        return this;
    }

    @Override
    public String toString()
    {
        return getRequestType() + " " + getType() + " " + getResource();
    }

}
