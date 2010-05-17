package org.apache.maven.repository.util;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.util.HashMap;
import java.util.Map;

import org.apache.maven.repository.ArtifactStereotype;
import org.apache.maven.repository.ArtifactStereotypeManager;

/**
 * @author Benjamin Bentmann
 */
public class DefaultArtifactStereotypeManager
    implements ArtifactStereotypeManager
{

    private final Map<String, ArtifactStereotype> stereotypes = new HashMap<String, ArtifactStereotype>();

    public DefaultArtifactStereotypeManager addStereotype( ArtifactStereotype stereotype )
    {
        stereotypes.put( stereotype.getId(), stereotype );
        return this;
    }

    public ArtifactStereotype get( String stereotypeId )
    {
        ArtifactStereotype stereotype = stereotypes.get( stereotypeId );

        if ( stereotype == null )
        {
            stereotype = new DefaultArtifactStereotype( stereotypeId );
        }

        return stereotype;
    }

}
