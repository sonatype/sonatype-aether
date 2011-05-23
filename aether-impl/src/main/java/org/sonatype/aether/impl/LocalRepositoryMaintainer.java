package org.sonatype.aether.impl;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

/**
 * Performs housekeeping tasks in response to updates to the local repository. This provides an extension point to
 * integrators to perform things like updating indexes. <em>Note:</em> Implementors are strongly advised to inherit from
 * {@link AbstractLocalRepositoryMaintainer} instead of directly implementing this interface.
 * 
 * @author Benjamin Bentmann
 * @deprecated As of version 1.9, use a global {@link org.sonatype.aether.RepositoryListener} instead.
 */
@Deprecated
public interface LocalRepositoryMaintainer
{

    /**
     * Notifies the maintainer of the addition of an artifact to the local repository by a local build.
     * 
     * @param event The event that holds details about the artifact, must not be {@code null}.
     */
    void artifactInstalled( LocalRepositoryEvent event );

    /**
     * Notifies the maintainer of the addition of an artifact to the local repository by download from a remote
     * repository.
     * 
     * @param event The event that holds details about the artifact, must not be {@code null}.
     */
    void artifactDownloaded( LocalRepositoryEvent event );

}
