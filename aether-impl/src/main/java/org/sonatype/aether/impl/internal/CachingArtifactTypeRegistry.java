package org.sonatype.aether.impl.internal;

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

import java.util.HashMap;
import java.util.Map;

import org.sonatype.aether.ArtifactType;
import org.sonatype.aether.ArtifactTypeRegistry;
import org.sonatype.aether.RepositorySystemSession;

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
