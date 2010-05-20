package org.apache.maven.repository;

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

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Benjamin Bentmann
 */
public class DefaultArtifact
    implements Artifact, Cloneable
{

    private static final String SNAPSHOT = "SNAPSHOT";

    private static final Pattern SNAPSHOT_TIMESTAMP = Pattern.compile( "^(.*-)([0-9]{8}.[0-9]{6}-[0-9]+)$" );

    private String groupId;

    private String artifactId;

    private String baseVersion;

    private String version;

    private String classifier;

    private String type;

    private File file;

    private Map<String, Object> properties = new HashMap<String, Object>();

    public DefaultArtifact()
    {
        // enables default constructor
        groupId = artifactId = version = classifier = type = "";
    }

    public DefaultArtifact( String groupId, String artifactId, String classifier, String type, String version )
    {
        setGroupId( groupId );
        setArtifactId( artifactId );
        setClassifier( classifier );
        setType( type );
        setVersion( version );
    }

    public DefaultArtifact( String groupId, String artifactId, String classifier, String type, String version,
                            ArtifactStereotype stereotype )
    {
        setGroupId( groupId );
        setArtifactId( artifactId );
        setClassifier( ( classifier != null || stereotype == null ) ? classifier : stereotype.getClassifier() );
        setType( ( type != null || stereotype == null ) ? type : stereotype.getType() );
        setVersion( version );
        properties.putAll( stereotype.getProperties() );
    }

    public DefaultArtifact( Artifact artifact )
    {
        setGroupId( artifact.getGroupId() );
        setArtifactId( artifact.getArtifactId() );
        setClassifier( artifact.getClassifier() );
        setType( artifact.getType() );
        setVersion( artifact.getVersion() );
        setFile( artifact.getFile() );
        properties.putAll( artifact.getProperties() );
    }

    public String getGroupId()
    {
        return groupId;
    }

    public DefaultArtifact setGroupId( String groupId )
    {
        this.groupId = ( groupId != null ) ? groupId : "";
        return this;
    }

    public String getArtifactId()
    {
        return artifactId;
    }

    public DefaultArtifact setArtifactId( String artifactId )
    {
        this.artifactId = ( artifactId != null ) ? artifactId : "";
        return this;
    }

    public String getBaseVersion()
    {
        if ( baseVersion == null )
        {
            String version = getVersion();
            if ( version.startsWith( "[" ) || version.startsWith( "(" ) )
            {
                baseVersion = "";
            }
            else
            {
                Matcher m = SNAPSHOT_TIMESTAMP.matcher( version );
                if ( m.matches() )
                {
                    this.baseVersion = m.group( 1 ) + SNAPSHOT;
                }
                else
                {
                    this.baseVersion = version;
                }
            }
        }
        return baseVersion;
    }

    public String getVersion()
    {
        return version;
    }

    public DefaultArtifact setVersion( String version )
    {
        this.version = ( version != null ) ? version : "";
        return this;
    }

    public boolean isSnapshot()
    {
        String bv = getBaseVersion();
        return bv != null && bv.endsWith( SNAPSHOT );
    }

    public String getClassifier()
    {
        return classifier;
    }

    public DefaultArtifact setClassifier( String classifier )
    {
        this.classifier = ( classifier != null ) ? classifier : "";
        return this;
    }

    public String getType()
    {
        return type;
    }

    public DefaultArtifact setType( String type )
    {
        this.type = ( type != null ) ? type : "";
        return this;
    }

    public File getFile()
    {
        return file;
    }

    public DefaultArtifact setFile( File file )
    {
        this.file = file;
        return this;
    }

    public <T> T getProperty( String key, Class<T> type, T defaultValue )
    {
        Object value = properties.get( key );
        return type.isInstance( value ) ? type.cast( value ) : defaultValue;
    }

    public Map<String, Object> getProperties()
    {
        return properties;
    }

    public DefaultArtifact setProperty( String key, Object value )
    {
        if ( value == null )
        {
            properties.remove( key );
        }
        else
        {
            properties.put( key, value );
        }
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder buffer = new StringBuilder( 128 );
        buffer.append( getGroupId() );
        buffer.append( ':' ).append( getArtifactId() );
        buffer.append( ':' ).append( getType() );
        if ( getClassifier().length() > 0 )
        {
            buffer.append( ':' ).append( getClassifier() );
        }
        buffer.append( ':' ).append( getVersion() );
        return buffer.toString();
    }

    @Override
    public DefaultArtifact clone()
    {
        try
        {
            DefaultArtifact clone = (DefaultArtifact) super.clone();

            clone.properties = new HashMap<String, Object>( clone.properties );

            return clone;
        }
        catch ( CloneNotSupportedException e )
        {
            throw new UnsupportedOperationException( e );
        }
    }

}
