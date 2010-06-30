package org.sonatype.maven.repository;

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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
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

    private final SnapshotHandler snapshotHandler = DefaultSnapshotHandler.INSTANCE;

    private String baseVersion;

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
        if ( type != null )
        {
            properties = new HashMap<String, String>( type.getProperties() );
        }
        else
        {
            properties = Collections.emptyMap();
        }
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
            baseVersion = snapshotHandler.toBaseVersion( getVersion() );
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
        return snapshotHandler.isSnapshot( getVersion() );
    }

    public SnapshotHandler getSnapshotHandler()
    {
        return snapshotHandler;
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
