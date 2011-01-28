package org.sonatype.aether.impl;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *******************************************************************************/

import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.resolution.ArtifactDescriptorException;
import org.sonatype.aether.resolution.ArtifactDescriptorRequest;
import org.sonatype.aether.resolution.ArtifactDescriptorResult;

/**
 * @author Benjamin Bentmann
 */
public interface ArtifactDescriptorReader
{

    /**
     * Gets information about an artifact like its direct dependencies.
     * 
     * @param session The repository session, must not be {@code null}.
     * @param request The descriptor request, must not be {@code null}
     * @return The descriptor result, never {@code null}.
     * @throws ArtifactDescriptorException If the artifact descriptor could not be read.
     */
    ArtifactDescriptorResult readArtifactDescriptor( RepositorySystemSession session, ArtifactDescriptorRequest request )
        throws ArtifactDescriptorException;

}
