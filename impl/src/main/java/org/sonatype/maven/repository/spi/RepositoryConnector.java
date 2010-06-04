package org.sonatype.maven.repository.spi;

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

import java.util.Collection;

/**
 * A connector for a remote repository. The connector is responsible for downloading/uploading of artifacts and metadata
 * from/to a remote repository.
 * 
 * @author Benjamin Bentmann
 */
public interface RepositoryConnector
{

    /**
     * Performs the specified downloads. Any error encountered during a transfer can be queried via
     * {@link ArtifactDownload#getException()} and {@link MetadataDownload#getException()}, respectively.
     * 
     * @param artifactDownloads The artifact downloads to perform, may be {@code null} or empty.
     * @param metadataDownloads The metadata downloads to perform, may be {@code null} or empty.
     */
    void get( Collection<? extends ArtifactDownload> artifactDownloads,
              Collection<? extends MetadataDownload> metadataDownloads );

    /**
     * Performs the specified uploads. Any error encountered during a transfer can be queried via
     * {@link ArtifactDownload#getException()} and {@link MetadataDownload#getException()}, respectively.
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
