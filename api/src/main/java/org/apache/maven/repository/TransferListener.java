package org.apache.maven.repository;

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
 * A listener being notified of artifact/metadata transfers from/to remote repositories. The listener may be called from
 * an arbitrary thread. <em>Note:</em> Implementors are strongly advised to inherit from
 * {@link org.apache.maven.repository.util.AbstractTransferListener} instead of directly implementing this interface.
 * 
 * @author Benjamin Bentmann
 */
public interface TransferListener
{

    /**
     * Notifies the listener about the initiation of a transfer. This event gets fired before any actual network access
     * to the remote repository.
     * 
     * @param transferEvent The event details, must not be {@code null}.
     * @throws TransferCancelledException If the transfer should be aborted.
     */
    void transferInitiated( TransferEvent transferEvent )
        throws TransferCancelledException;

    /**
     * Notifies the listener about the start of a data transfer.
     * 
     * @param transferEvent The event details, must not be {@code null}.
     * @throws TransferCancelledException If the transfer should be aborted.
     */
    void transferStarted( TransferEvent transferEvent )
        throws TransferCancelledException;

    /**
     * Notifies the listener about some progress in the data transfer.
     * 
     * @param transferEvent The event details, must not be {@code null}.
     * @throws TransferCancelledException If the transfer should be aborted.
     */
    void transferProgressed( TransferEvent transferEvent )
        throws TransferCancelledException;

    /**
     * Notifies the listener that a checksum validation failed. {@link TransferEvent#getException()} will be of type
     * {@link ChecksumFailureException} and can be used to query further details about the expected/actual checksums.
     * 
     * @param transferEvent The event details, must not be {@code null}.
     * @throws TransferCancelledException If the transfer should be aborted.
     */
    void transferCorrupted( TransferEvent transferEvent )
        throws TransferCancelledException;

    /**
     * Notifies the listener about the successful completion of a transfer.
     * 
     * @param transferEvent The event details, must not be {@code null}.
     */
    void transferSucceeded( TransferEvent transferEvent );

    /**
     * Notifies the listener about the unsuccessful termination of a transfer.
     * 
     * @param transferEvent The event details, must not be {@code null}.
     */
    void transferFailed( TransferEvent transferEvent );

}
