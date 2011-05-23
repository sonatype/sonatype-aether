package org.sonatype.aether.repository;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

/**
 * A repository hosting artifacts.
 * 
 * @author Benjamin Bentmann
 */
public interface ArtifactRepository
{

    /**
     * Gets the type of the repository, for example "default".
     * 
     * @return The (case-sensitive) type of the repository, never {@code null}.
     */
    String getContentType();

    /**
     * Gets the identifier of this repository.
     * 
     * @return The (case-sensitive) identifier, never {@code null}.
     */
    String getId();

}
