package org.sonatype.maven.repository.util;

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

import org.sonatype.maven.repository.ArtifactType;

/**
 * @author Benjamin Bentmann
 */
public class DefaultArtifactType
    implements ArtifactType
{

    private final String id;

    private final String extension;

    private final String classifier;

    private final Map<String, String> properties;

    public DefaultArtifactType( String id )
    {
        this( id, id, "", "none", false, false );
    }

    public DefaultArtifactType( String id, String extension, String classifier, String language )
    {
        this( id, extension, classifier, language, true, false );
    }

    public DefaultArtifactType( String id, String extension, String classifier, String language,
                                boolean constitutesBuildPath, boolean includesDependencies )
    {
        if ( id == null || id.length() < 0 )
        {
            throw new IllegalArgumentException( "no type id specified" );
        }
        this.id = id;
        this.extension = ( extension != null && extension.length() > 0 ) ? extension : id;
        this.classifier = ( classifier != null ) ? classifier : "";
        Map<String, String> props = new HashMap<String, String>();
        props.put( "type", id );
        props.put( "language", ( language != null && language.length() > 0 ) ? language : "none" );
        props.put( "includesDependencies", Boolean.toString( includesDependencies ) );
        props.put( "constitutesBuildPath", Boolean.toString( constitutesBuildPath ) );
        properties = props;
    }

    public String getId()
    {
        return id;
    }

    public String getExtension()
    {
        return extension;
    }

    public String getClassifier()
    {
        return classifier;
    }

    public Map<String, String> getProperties()
    {
        return properties;
    }

}
