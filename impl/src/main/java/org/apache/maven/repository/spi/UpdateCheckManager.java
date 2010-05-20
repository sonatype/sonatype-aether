package org.apache.maven.repository.spi;

import org.apache.maven.repository.Artifact;
import org.apache.maven.repository.ArtifactTransferException;
import org.apache.maven.repository.Metadata;
import org.apache.maven.repository.MetadataTransferException;
import org.apache.maven.repository.RepositorySession;

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
 * @author Benjamin Bentmann
 */
public interface UpdateCheckManager
{

    String getEffectiveUpdatePolicy( RepositorySession session, String policy1, String policy2 );

    void checkArtifact( RepositorySession session, UpdateCheck<Artifact, ArtifactTransferException> check );

    void touchArtifact( RepositorySession session, UpdateCheck<Artifact, ArtifactTransferException> check );

    void checkMetadata( RepositorySession session, UpdateCheck<Metadata, MetadataTransferException> check );

    void touchMetadata( RepositorySession session, UpdateCheck<Metadata, MetadataTransferException> check );

}
