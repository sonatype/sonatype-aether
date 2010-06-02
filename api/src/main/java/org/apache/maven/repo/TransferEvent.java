package org.apache.maven.repo;

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

/**
 * An event fired to a transfer listener during an artifact/metadata transfer.
 * 
 * @author Benjamin Bentmann
 */
public interface TransferEvent
{

    /**
     * The type of the event.
     */
    enum EventType
    {
        INITIATED, STARTED, PROGRESSED, CORRUPTED, SUCCEEDED, FAILED
    }

    /**
     * The type of the request/transfer.
     */
    enum RequestType
    {
        GET, PUT,
    }

    /**
     * Gets the type of the event.
     * 
     * @return The type of the event, never {@code null}.
     */
    EventType getType();

    /**
     * Gets the type of the request/transfer.
     * 
     * @return The type of the request/transfer, never {@code null}.
     */
    RequestType getRequestType();

    /**
     * Gets the resource that is being transferred.
     * 
     * @return The resource being transferred, never {@code null}.
     */
    TransferResource getResource();

    /**
     * Gets the total number of bytes that have been transferred so far during the request.
     * 
     * @return The total number of bytes that have been transferrred.
     */
    long getTransferredBytes();

    /**
     * The byte buffer holding the transferred bytes since the last event. Note that the buffer can be bigger than the
     * actually valid bytes, so be sure to query {@link #getDataOffset()} and {@link #getDataLength()}.
     * 
     * @return The byte buffer or {@code null} if not applicable to the event, i.e. if the event type is not
     *         {@link EventType#PROGRESSED}.
     */
    byte[] getDataBuffer();

    /**
     * The offset into the byte buffer where the first valid byte resides.
     * 
     * @return The offset into the byte buffer.
     */
    int getDataOffset();

    /**
     * The number of valid bytes in the byte buffer.
     * 
     * @return The number of valid bytes.
     */
    int getDataLength();

    /**
     * Gets the error that occurred during the transfer.
     * 
     * @return The error that occurred or {@code null} if none.
     */
    Exception getException();

}
