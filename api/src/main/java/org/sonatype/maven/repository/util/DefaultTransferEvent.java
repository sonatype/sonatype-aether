package org.sonatype.maven.repository.util;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.sonatype.maven.repository.TransferEvent;
import org.sonatype.maven.repository.TransferResource;

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

}
