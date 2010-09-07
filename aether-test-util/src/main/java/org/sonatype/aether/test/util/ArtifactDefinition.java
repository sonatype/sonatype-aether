package org.sonatype.aether.test.util;

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

class ArtifactDefinition
{
    private String groupId;

    private String artifactId;

    private String extension;

    private String version;

    private String scope = "";

    private String definition;

    private String id = null;

    private String reference = null;

    private boolean optional = false;

    public ArtifactDefinition( String def )
    {
        super();

        this.definition = def.trim();

        if ( definition.startsWith( "(" ) )
        {
            int idx = definition.indexOf( ')' );
            this.id = definition.substring( 1, idx );
            this.definition = definition.substring( idx + 1 );
        }
        else if ( definition.startsWith( "^" ) )
        {
            this.reference = definition.substring( 1 );
            return;
        }

        String[] split = definition.split( ":" );
        if ( split.length < 4 )
        {
            throw new IllegalArgumentException( "Need definition like 'gid:aid:ext:ver[:scope]', but was: "
                + definition );
        }
        groupId = split[0];
        artifactId = split[1];
        extension = split[2];
        version = split[3];
        if ( split.length > 4 )
        {
            scope = split[4];
        }
        if ( split.length > 5 && "optional".equalsIgnoreCase( split[5] ) )
        {
            optional = true;
        }
    }

    public String getGroupId()
    {
        return groupId;
    }

    public String getArtifactId()
    {
        return artifactId;
    }

    public String getExtension()
    {
        return extension;
    }

    public String getVersion()
    {
        return version;
    }

    public String getScope()
    {
        return scope;
    }

    @Override
    public String toString()
    {
        return definition;
    }

    public String getId()
    {
        return id;
    }

    public String getReference()
    {
        return reference;
    }

    public boolean isReference()
    {
        return reference != null;
    }

    public boolean hasId()
    {
        return id != null;
    }

    public boolean isOptional()
    {
        return optional;
    }
}