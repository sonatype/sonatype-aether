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

import java.util.Collection;

/**
 * A connector for a remote repository. The connector is responsible for downloading/uploading of artifacts and metadata
 * from/to a remote repository. Besides performing the actual transfer and recording any exception encountered in the
 * provided upload/download objects, a connector must also use
 * {@link Transfer#setState(org.sonatype.aether.spi.connector.Transfer.State)} to update the state of a transfer during
 * its processing. Furthermore, the connector must notify any {@link org.sonatype.aether.transfer.TransferListener
 * TransferListener} configured on its associated {@link org.sonatype.aether.RepositorySystemSession
 * RepositorySystemSession}. If applicable, a connector should obey connect/request timeouts and other relevant settings
 * from the configuration properties of the repository system session. While a connector itself can use multiple threads
 * internally to performs the transfers, clients must not call a connector concurrently, i.e. connectors are generally
 * not thread-safe.
 * 
 * @author Benjamin Bentmann
 * @see org.sonatype.aether.RepositorySystemSession#getConfigProperties()
 */
public interface RepositoryConnector
{

    /**
     * Performs the specified downloads. Any error encountered during a transfer can later be queried via
     * {@link ArtifactDownload#getException()} and {@link MetadataDownload#getException()}, respectively. The connector
     * may performs the transfers concurrently and in any order.
     * 
     * @param artifactDownloads The artifact downloads to perform, may be {@code null} or empty.
     * @param metadataDownloads The metadata downloads to perform, may be {@code null} or empty.
     */
    void get( Collection<? extends ArtifactDownload> artifactDownloads,
              Collection<? extends MetadataDownload> metadataDownloads );

    /**
     * Performs the specified uploads. Any error encountered during a transfer can later be queried via
     * {@link ArtifactDownload#getException()} and {@link MetadataDownload#getException()}, respectively. The connector
     * may performs the transfers concurrently and in any order.
     * 
     * @param artifactUploads The artifact uploads to perform, may be {@code null} or empty.
     * @param metadataUploads The metadata uploads to perform, may be {@code null} or empty.
     */
    void put( Collection<? extends ArtifactUpload> artifactUploads, Collection<? extends MetadataUpload> metadataUploads );

    /**
     * Closes this connector and frees any network resources associated with it. Once closed, a connector must not be
     * used for further transfers. Closing an already closed connector has no effect.
     */
    void close();

}
