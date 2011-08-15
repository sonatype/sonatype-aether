package org.sonatype.aether.util.artifact;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import java.util.HashMap;
import java.util.Map;

import org.sonatype.aether.artifact.ArtifactType;
import org.sonatype.aether.artifact.ArtifactTypeRegistry;

/**
 * A simple artifact type registry.
 * 
 * @author Benjamin Bentmann
 */
public class DefaultArtifactTypeRegistry
    implements ArtifactTypeRegistry
{

    private final Map<String, ArtifactType> types;

    /**
     * Creates a new artifact type registry with initally no registered artifact types. Use {@link #add(ArtifactType)}
     * to populate the registry.
     */
    public DefaultArtifactTypeRegistry()
    {
        types = new HashMap<String, ArtifactType>();
    }

    /**
     * Adds the specified artifact type to the registry.
     * 
     * @param type The artifact type to add, must not be {@code null}.
     * @return This registry for chaining, never {@code null}.
     */
    public DefaultArtifactTypeRegistry add( ArtifactType type )
    {
        types.put( type.getId(), type );
        return this;
    }

    public ArtifactType get( String typeId )
    {
        ArtifactType type = types.get( typeId );

        return type;
    }

    @Override
    public String toString()
    {
        return types.toString();
    }

}
