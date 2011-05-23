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
import org.sonatype.aether.resolution.MetadataRequest;
import org.sonatype.aether.resolution.MetadataResult;

/**
 * @author Benjamin Bentmann
 */
public interface MetadataResolver
{

    /**
     * Resolves the paths for a collection of metadata. Metadata will be downloaded if necessary.
     */
    List<MetadataResult> resolveMetadata( RepositorySystemSession session, Collection<? extends MetadataRequest> requests );

}
