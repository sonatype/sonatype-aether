package org.sonatype.aether.util.artifact;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *******************************************************************************/

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

    /**
     * Creates a new artifact with the specified coordinates.
     * 
     * @param coords The artifact coordinates in the format
     *            {@code <groupId>:<artifactId>[:<extension>[:<classifier>]]:<version>}, must not be {@code null}.
     */
    public DefaultArtifact( String coords )
    {
        this( coords, Collections.<String, String> emptyMap() );
    }

    /**
     * Creates a new artifact with the specified coordinates and properties.
     * 
     * @param coords The artifact coordinates in the format
     *            {@code <groupId>:<artifactId>[:<extension>[:<classifier>]]:<version>}, must not be {@code null}.
     * @param properties The artifact properties, may be {@code null}.
     */
    public DefaultArtifact( String coords, Map<String, String> properties )
    {
        Pattern p = Pattern.compile( "([^: ]+):([^: ]+)(:([^: ]*)(:([^: ]+))?)?:([^: ]+)" );
        Matcher m = p.matcher( coords );
        if ( !m.matches() )
        {
            throw new IllegalArgumentException( "Bad artifact coordinates"
                + ", expected format is <groupId>:<artifactId>[:<extension>[:<classifier>]]:<version>" );
        }
        groupId = m.group( 1 );
        artifactId = m.group( 2 );
        extension = get( m.group( 4 ), "jar" );
        classifier = get( m.group( 6 ), "" );
        version = m.group( 7 );
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

    /**
     * Creates a new artifact with the specified coordinates.
     * 
     * @param groupId The group identifier of the artifact, may be {@code null}.
     * @param artifactId The artifact identifier of the artifact, may be {@code null}.
     * @param extension The file extension of the artifact, may be {@code null}.
     * @param version The version of the artifact, may be {@code null}.
     */
    public DefaultArtifact( String groupId, String artifactId, String extension, String version )
    {
        this( groupId, artifactId, "", extension, version );
    }

    /**
     * Creates a new artifact with the specified coordinates.
     * 
     * @param groupId The group identifier of the artifact, may be {@code null}.
     * @param artifactId The artifact identifier of the artifact, may be {@code null}.
     * @param classifier The classifier of the artifact, may be {@code null}.
     * @param extension The file extension of the artifact, may be {@code null}.
     * @param version The version of the artifact, may be {@code null}.
     */
    public DefaultArtifact( String groupId, String artifactId, String classifier, String extension, String version )
    {
        this( groupId, artifactId, classifier, extension, version, null, (File) null );
    }

    /**
     * Creates a new artifact with the specified coordinates. The optional artifact type provided to this constructor
     * will be used to determine the artifact's classifier and file extension if the corresponding arguments for this
     * constructor are {@code null}.
     * 
     * @param groupId The group identifier of the artifact, may be {@code null}.
     * @param artifactId The artifact identifier of the artifact, may be {@code null}.
     * @param classifier The classifier of the artifact, may be {@code null}.
     * @param extension The file extension of the artifact, may be {@code null}.
     * @param version The version of the artifact, may be {@code null}.
     * @param type The artifact type from which to query classifier, file extension and properties, may be {@code null}.
     */
    public DefaultArtifact( String groupId, String artifactId, String classifier, String extension, String version,
                            ArtifactType type )
    {
        this( groupId, artifactId, classifier, extension, version, null, type );
    }

    /**
     * Creates a new artifact with the specified coordinates and properties. The optional artifact type provided to this
     * constructor will be used to determine the artifact's classifier and file extension if the corresponding arguments
     * for this constructor are {@code null}. If the artifact type specifies properties, those will get merged with the
     * properties passed directly into the constructor, with the latter properties taking precedence.
     * 
     * @param groupId The group identifier of the artifact, may be {@code null}.
     * @param artifactId The artifact identifier of the artifact, may be {@code null}.
     * @param classifier The classifier of the artifact, may be {@code null}.
     * @param extension The file extension of the artifact, may be {@code null}.
     * @param version The version of the artifact, may be {@code null}.
     * @param properties The properties of the artifact, may be {@code null}.
     * @param type The artifact type from which to query classifier, file extension and properties, may be {@code null}.
     */
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

    /**
     * Creates a new artifact with the specified coordinates, properties and file.
     * 
     * @param groupId The group identifier of the artifact, may be {@code null}.
     * @param artifactId The artifact identifier of the artifact, may be {@code null}.
     * @param classifier The classifier of the artifact, may be {@code null}.
     * @param extension The file extension of the artifact, may be {@code null}.
     * @param version The version of the artifact, may be {@code null}.
     * @param properties The properties of the artifact, may be {@code null}.
     * @param file The resolved file of the artifact, may be {@code null}.
     */
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
        if ( this.version.equals( version ) || ( version == null && this.version.length() <= 0 ) )
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

    public Artifact setProperties( Map<String, String> properties )
    {
        if ( this.properties.equals( properties ) || ( properties == null && this.properties.isEmpty() ) )
        {
            return this;
        }
        return new DefaultArtifact( groupId, artifactId, classifier, extension, version, properties, file );
    }

}
