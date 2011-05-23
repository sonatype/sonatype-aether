package org.sonatype.aether.impl.internal;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import java.util.HashMap;
import java.util.Map;

import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.ArtifactType;
import org.sonatype.aether.artifact.ArtifactTypeRegistry;

/**
 * A short-lived artifact type registry that caches results from a presumedly slower type registry.
 * 
 * @author Benjamin Bentmann
 */
class CachingArtifactTypeRegistry
    implements ArtifactTypeRegistry
{

    private final ArtifactTypeRegistry delegate;

    private final Map<String, ArtifactType> types;

    public static ArtifactTypeRegistry newInstance( RepositorySystemSession session )
    {
        return newInstance( session.getArtifactTypeRegistry() );
    }

    public static ArtifactTypeRegistry newInstance( ArtifactTypeRegistry delegate )
    {
        return ( delegate != null ) ? new CachingArtifactTypeRegistry( delegate ) : null;
    }

    private CachingArtifactTypeRegistry( ArtifactTypeRegistry delegate )
    {
        this.delegate = delegate;
        types = new HashMap<String, ArtifactType>();
    }

    public ArtifactType get( String typeId )
    {
        ArtifactType type = types.get( typeId );

        if ( type == null )
        {
            type = delegate.get( typeId );
            types.put( typeId, type );
        }

        return type;
    }

}
