package org.sonatype.aether.spi.connector;

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

import java.io.File;

import org.sonatype.aether.metadata.Metadata;
import org.sonatype.aether.transfer.MetadataTransferException;

/**
 * A download/upload of metadata.
 * 
 * @author Benjamin Bentmann
 */
public abstract class MetadataTransfer
    extends Transfer
{

    private Metadata metadata;

    private File file;

    private MetadataTransferException exception;

    /**
     * Gets the metadata being transferred.
     * 
     * @return The metadata being transferred or {@code null} if not set.
     */
    public Metadata getMetadata()
    {
        return metadata;
    }

    /**
     * Sets the metadata to transfer.
     * 
     * @param metadata The metadata, may be {@code null}.
     * @return This transfer for chaining, never {@code null}.
     */
    public MetadataTransfer setMetadata( Metadata metadata )
    {
        this.metadata = metadata;
        return this;
    }

    /**
     * Gets the local file the metadata is downloaded to or uploaded from.
     * 
     * @return The local file or {@code null} if not set.
     */
    public File getFile()
    {
        return file;
    }

    /**
     * Sets the local file the metadata is downloaded to or uploaded from.
     * 
     * @param file The local file, may be {@code null}.
     * @return This transfer for chaining, never {@code null}.
     */
    public MetadataTransfer setFile( File file )
    {
        this.file = file;
        return this;
    }

    /**
     * Gets the exception that occurred during the transfer (if any).
     * 
     * @return The exception or {@code null} if the transfer was successful.
     */
    public MetadataTransferException getException()
    {
        return exception;
    }

    /**
     * Sets the exception that occurred during the transfer.
     * 
     * @param exception The exception, may be {@code null} to denote a successful transfer.
     * @return This transfer for chaining, never {@code null}.
     */
    public MetadataTransfer setException( MetadataTransferException exception )
    {
        this.exception = exception;
        return this;
    }

}
