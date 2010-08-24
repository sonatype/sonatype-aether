package org.sonatype.aether;

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

import org.sonatype.aether.transfer.TransferListener;

/**
 * A listener being notified of events from the repository system. The listener may be called from an arbitrary thread.
 * <em>Note:</em> Implementors are strongly advised to inherit from
 * {@link org.sonatype.aether.util.listener.AbstractRepositoryListener} instead of directly implementing this interface.
 * 
 * @author Benjamin Bentmann
 * @see TransferListener
 */
public interface RepositoryListener
{

    void artifactDescriptorInvalid( RepositoryEvent event );

    void artifactDescriptorMissing( RepositoryEvent event );

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
