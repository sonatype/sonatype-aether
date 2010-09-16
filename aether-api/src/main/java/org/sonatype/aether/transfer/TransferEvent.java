package org.sonatype.aether.transfer;

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
     * @return The total number of bytes that have been transferred.
     */
    long getTransferredBytes();

    /**
     * The byte buffer holding the transferred bytes since the last event. Note that the buffer can be bigger than the
     * actually valid bytes, so be sure to query {@link #getDataOffset()} and {@link #getDataLength()}. A listener must
     * assume this buffer to be owned by the event source and must not change any byte in this buffer. Also, the buffer
     * is only valid for the duration of the event, i.e. the next event might reuse the same buffer (with updated
     * contents).
     * 
     * @return The (read-only) byte buffer or {@code null} if not applicable to the event, i.e. if the event type is not
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
