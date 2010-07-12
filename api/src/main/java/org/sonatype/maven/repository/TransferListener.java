package org.sonatype.maven.repository;

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
 * A listener being notified of artifact/metadata transfers from/to remote repositories. The listener may be called from
 * an arbitrary thread. <em>Note:</em> Implementors are strongly advised to inherit from
 * {@link org.sonatype.maven.repository.util.AbstractTransferListener} instead of directly implementing this interface.
 * 
 * @author Benjamin Bentmann
 */
public interface TransferListener
{

    /**
     * Notifies the listener about the initiation of a transfer. This event gets fired before any actual network access
     * to the remote repository.
     * 
     * @param event The event details, must not be {@code null}.
     * @throws TransferCancelledException If the transfer should be aborted.
     */
    void transferInitiated( TransferEvent event )
        throws TransferCancelledException;

    /**
     * Notifies the listener about the start of a data transfer, i.e. the successful connection to the remote
     * repository.
     * 
     * @param event The event details, must not be {@code null}.
     * @throws TransferCancelledException If the transfer should be aborted.
     */
    void transferStarted( TransferEvent event )
        throws TransferCancelledException;

    /**
     * Notifies the listener about some progress in the data transfer.
     * 
     * @param event The event details, must not be {@code null}.
     * @throws TransferCancelledException If the transfer should be aborted.
     */
    void transferProgressed( TransferEvent event )
        throws TransferCancelledException;

    /**
     * Notifies the listener that a checksum validation failed. {@link TransferEvent#getException()} will be of type
     * {@link ChecksumFailureException} and can be used to query further details about the expected/actual checksums.
     * 
     * @param event The event details, must not be {@code null}.
     * @throws TransferCancelledException If the transfer should be aborted.
     */
    void transferCorrupted( TransferEvent event )
        throws TransferCancelledException;

    /**
     * Notifies the listener about the successful completion of a transfer.
     * 
     * @param event The event details, must not be {@code null}.
     */
    void transferSucceeded( TransferEvent event );

    /**
     * Notifies the listener about the unsuccessful termination of a transfer.
     * 
     * @param event The event details, must not be {@code null}.
     */
    void transferFailed( TransferEvent event );

}
