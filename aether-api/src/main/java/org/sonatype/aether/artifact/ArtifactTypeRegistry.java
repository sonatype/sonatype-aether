package org.sonatype.aether.artifact;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

/**
 * A registry of known artifact types.
 * 
 * @author Benjamin Bentmann
 */
public interface ArtifactTypeRegistry
{

    /**
     * Gets the artifact type with the specified identifier.
     * 
     * @param typeId The identifier of the type, must not be {@code null}.
     * @return The artifact type or {@code null} if no type with the requested identifier exists.
     */
    ArtifactType get( String typeId );

}
