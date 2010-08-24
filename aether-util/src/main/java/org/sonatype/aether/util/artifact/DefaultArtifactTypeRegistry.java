package org.sonatype.aether.util.artifact;

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
