package org.sonatype.aether.impl;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import java.util.Collection;
import java.util.List;

import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.resolution.ArtifactRequest;
import org.sonatype.aether.resolution.ArtifactResolutionException;
import org.sonatype.aether.resolution.ArtifactResult;

/**
 * @author Benjamin Bentmann
 */
public interface ArtifactResolver
{

    /**
     * Resolves the path for an artifact. The artifact will be downloaded if necessary.
     */
    ArtifactResult resolveArtifact( RepositorySystemSession session, ArtifactRequest request )
        throws ArtifactResolutionException;

    /**
     * Resolves the paths for a collection of artifacts. Artifacts will be downloaded if necessary.
     */
    List<ArtifactResult> resolveArtifacts( RepositorySystemSession session, Collection<? extends ArtifactRequest> requests )
        throws ArtifactResolutionException;

}
