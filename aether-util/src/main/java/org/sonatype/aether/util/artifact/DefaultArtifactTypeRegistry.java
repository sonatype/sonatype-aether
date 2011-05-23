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

    private final Map<String, ArtifactType> stereotypes = new HashMap<String, ArtifactType>();

    /**
     * Adds the specified artifact type to the registry.
     * 
     * @param stereotype The artifact type to add, must not be {@code null}.
     * @return This registry for chaining, never {@code null}.
     */
    public DefaultArtifactTypeRegistry add( ArtifactType stereotype )
    {
        stereotypes.put( stereotype.getId(), stereotype );
        return this;
    }

    public ArtifactType get( String stereotypeId )
    {
        ArtifactType stereotype = stereotypes.get( stereotypeId );

        return stereotype;
    }

}
