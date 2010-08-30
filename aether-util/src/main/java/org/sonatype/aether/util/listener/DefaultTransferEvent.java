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

    private byte[] dataBuffer;

    private int dataOffset;

    private int dataLength;

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

    public byte[] getDataBuffer()
    {
        return dataBuffer;
    }

    public DefaultTransferEvent setDataBuffer( byte[] dataBuffer )
    {
        this.dataBuffer = dataBuffer;
        return this;
    }

    public int getDataOffset()
    {
        return dataOffset;
    }

    public DefaultTransferEvent setDataOffset( int dataOffset )
    {
        this.dataOffset = dataOffset;
        return this;
    }

    public int getDataLength()
    {
        return dataLength;
    }

    public DefaultTransferEvent setDataLength( int dataLength )
    {
        this.dataLength = dataLength;
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
