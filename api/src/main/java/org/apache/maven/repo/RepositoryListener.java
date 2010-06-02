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
 * A listener being notified of events from the repository system. The listener may be called from an arbitrary thread.
 * <em>Note:</em> Implementors are strongly advised to inherit from
 * {@link org.apache.maven.repo.util.AbstractRepositoryListener} instead of directly implementing this interface.
 * 
 * @author Benjamin Bentmann
 * @see TransferListener
 */
public interface RepositoryListener
{

    void artifactDescriptorInvalid( RepositoryEvent event );

    void artifactDescriptorMissing( RepositoryEvent event );

    void artifactRelocated( RepositoryEvent event );

    void metadataInvalid( RepositoryEvent event );

    void artifactResolving( RepositoryEvent event );

    void artifactResolved( RepositoryEvent event );

    void metadataResolving( RepositoryEvent event );

    void metadataResolved( RepositoryEvent event );

    void artifactInstalling( RepositoryEvent event );

    void artifactInstalled( RepositoryEvent event );

    void metadataInstalling( RepositoryEvent event );

    void metadataInstalled( RepositoryEvent event );

    void artifactDeploying( RepositoryEvent event );

    void artifactDeployed( RepositoryEvent event );

    void metadataDeploying( RepositoryEvent event );

    void metadataDeployed( RepositoryEvent event );

}
