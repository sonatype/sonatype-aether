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

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.artifact.ArtifactType;

/**
 * A simple artifact.
 * 
 * @author Benjamin Bentmann
 */
public final class DefaultArtifact
    extends AbstractArtifact
{

    private final String groupId;

    private final String artifactId;

    private final String version;

    private final String classifier;

    private final String extension;

    private final File file;

    private final Map<String, String> properties;

    private String baseVersion;

    public DefaultArtifact( String coords )
    {
        this( coords, Collections.<String, String> emptyMap() );
    }

    public DefaultArtifact( String coords, Map<String, String> properties )
    {
        Pattern p = Pattern.compile( "([^: ]+):([^: ]+):([^: ]+)(:([^: ]*)(:([^: ]+))?)?" );
        Matcher m = p.matcher( coords );
        if ( !m.matches() )
        {
            throw new IllegalArgumentException( "Bad artifact coordinates"
                + ", expected format is <groupId>:<artifactId>:<version>[:<extension>[:<classifier>]]" );
        }
        groupId = m.group( 1 );
        artifactId = m.group( 2 );
        version = m.group( 3 );
        extension = get( m.group( 5 ), "jar" );
        classifier = get( m.group( 7 ), "" );
        file = null;
        if ( properties != null && !properties.isEmpty() )
        {
            this.properties = new HashMap<String, String>( properties );
        }
        else
        {
            this.properties = Collections.emptyMap();
        }
    }

    private static String get( String value, String defaultValue )
    {
        return ( value == null || value.length() <= 0 ) ? defaultValue : value;
    }

    public DefaultArtifact( String groupId, String artifactId, String extension, String version )
    {
        this( groupId, artifactId, "", extension, version );
    }

    public DefaultArtifact( String groupId, String artifactId, String classifier, String extension, String version )
    {
        this.groupId = emptify( groupId );
        this.artifactId = emptify( artifactId );
        this.classifier = emptify( classifier );
        this.extension = emptify( extension );
        this.version = emptify( version );
        this.file = null;
        this.properties = Collections.emptyMap();
    }

    public DefaultArtifact( String groupId, String artifactId, String classifier, String extension, String version,
                            ArtifactType type )
    {
        this( groupId, artifactId, classifier, extension, version, null, type );
    }

    public DefaultArtifact( String groupId, String artifactId, String classifier, String extension, String version,
                            Map<String, String> properties, ArtifactType type )
    {
        this.groupId = emptify( groupId );
        this.artifactId = emptify( artifactId );
        if ( classifier != null || type == null )
        {
            this.classifier = emptify( classifier );
        }
        else
        {
            this.classifier = emptify( type.getClassifier() );
        }
        if ( extension != null || type == null )
        {
            this.extension = emptify( extension );
        }
        else
        {
            this.extension = emptify( type.getExtension() );
        }
        this.version = emptify( version );
        this.file = null;
        this.properties = merge( properties, ( type != null ) ? type.getProperties() : null );
    }

    private static Map<String, String> merge( Map<String, String> dominant, Map<String, String> recessive )
    {
        Map<String, String> properties;

        if ( ( dominant == null || dominant.isEmpty() ) && ( recessive == null || recessive.isEmpty() ) )
        {
            properties = Collections.emptyMap();
        }
        else
        {
            properties = new HashMap<String, String>();
            if ( recessive != null )
            {
                properties.putAll( recessive );
            }
            if ( dominant != null )
            {
                properties.putAll( dominant );
            }
        }

        return properties;
    }

    public DefaultArtifact( String groupId, String artifactId, String classifier, String extension, String version,
                            Map<String, String> properties, File file )
    {
        this.groupId = emptify( groupId );
        this.artifactId = emptify( artifactId );
        this.classifier = emptify( classifier );
        this.extension = emptify( extension );
        this.version = emptify( version );
        this.file = file;
        if ( properties != null && !properties.isEmpty() )
        {
            this.properties = new HashMap<String, String>( properties );
        }
        else
        {
            this.properties = Collections.emptyMap();
        }
    }

    DefaultArtifact( String groupId, String artifactId, String classifier, String extension, String version, File file,
                     Map<String, String> properties )
    {
        // NOTE: This constructor assumes immutability of the provided properties, for internal use only
        this.groupId = emptify( groupId );
        this.artifactId = emptify( artifactId );
        this.classifier = emptify( classifier );
        this.extension = emptify( extension );
        this.version = emptify( version );
        this.file = file;
        this.properties = properties;
    }

    private static String emptify( String str )
    {
        return ( str == null ) ? "" : str;
    }

    public String getGroupId()
    {
        return groupId;
    }

    public String getArtifactId()
    {
        return artifactId;
    }

    public String getBaseVersion()
    {
        if ( baseVersion == null )
        {
            baseVersion = toBaseVersion( getVersion() );
        }
        return baseVersion;
    }

    public String getVersion()
    {
        return version;
    }

    public Artifact setVersion( String version )
    {
        if ( this.version.equals( version ) )
        {
            return this;
        }
        return new DefaultArtifact( groupId, artifactId, classifier, extension, version, file, properties );
    }

    public boolean isSnapshot()
    {
        return isSnapshot( getVersion() );
    }

    public String getClassifier()
    {
        return classifier;
    }

    public String getExtension()
    {
        return extension;
    }

    public File getFile()
    {
        return file;
    }

    public Artifact setFile( File file )
    {
        if ( ( this.file == null ) ? file == null : this.file.equals( file ) )
        {
            return this;
        }
        return new DefaultArtifact( groupId, artifactId, classifier, extension, version, file, properties );
    }

    public String getProperty( String key, String defaultValue )
    {
        String value = properties.get( key );
        return ( value != null ) ? value : defaultValue;
    }

    public Map<String, String> getProperties()
    {
        return Collections.unmodifiableMap( properties );
    }

}
