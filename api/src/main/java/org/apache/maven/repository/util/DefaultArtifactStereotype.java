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

/**
 * @author Benjamin Bentmann
 */
public class DefaultArtifactStereotype
    implements ArtifactStereotype
{

    private final String id;

    private final String type;

    private final String classifier;

    private final Map<String, Object> properties;

    public DefaultArtifactStereotype( String id )
    {
        this( id, id, "", "none", false, false );
    }

    public DefaultArtifactStereotype( String id, String type, String classifier, String language )
    {
        this( id, type, classifier, language, true, false );
    }

    public DefaultArtifactStereotype( String id, String type, String classifier, String language,
                                      boolean constitutesBuildPath, boolean includesDependencies )
    {
        if ( id == null || id.length() < 0 )
        {
            throw new IllegalArgumentException( "no stereotype id specified" );
        }
        this.id = id;
        this.type = ( type != null && type.length() > 0 ) ? type : id;
        this.classifier = ( classifier != null ) ? classifier : "";
        Map<String, Object> props = new HashMap<String, Object>();
        props.put( "stereotype", id );
        props.put( "language", ( language != null && language.length() > 0 ) ? language : "none" );
        props.put( "includesDependencies", Boolean.valueOf( includesDependencies ) );
        props.put( "constitutesBuildPath", Boolean.valueOf( constitutesBuildPath ) );
        properties = props;
    }

    public String getId()
    {
        return id;
    }

    public String getType()
    {
        return type;
    }

    public String getClassifier()
    {
        return classifier;
    }

    public Map<String, Object> getProperties()
    {
        return properties;
    }

}
