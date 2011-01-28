package org.sonatype.aether.util.metadata;

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

import org.sonatype.aether.metadata.Metadata;

/**
 * A basic metadata instance.
 * 
 * @author Benjamin Bentmann
 */
public final class DefaultMetadata
    implements Metadata
{

    private final String groupId;

    private final String artifactId;

    private final String version;

    private final String type;

    private final Nature nature;

    private final File file;

    public DefaultMetadata( String type, Nature nature )
    {
        groupId = artifactId = version = "";
        this.type = ( type != null ) ? type : "";
        if ( nature == null )
        {
            throw new IllegalArgumentException( "metadata nature was not specified" );
        }
        this.nature = nature;
        this.file = null;
    }

    public DefaultMetadata( String groupId, String type, Nature nature )
    {
        this.groupId = ( groupId != null ) ? groupId : "";
        artifactId = version = "";
        this.type = ( type != null ) ? type : "";
        if ( nature == null )
        {
            throw new IllegalArgumentException( "metadata nature was not specified" );
        }
        this.nature = nature;
        this.file = null;
    }

    public DefaultMetadata( String groupId, String artifactId, String type, Nature nature )
    {
        this.groupId = ( groupId != null ) ? groupId : "";
        this.artifactId = ( artifactId != null ) ? artifactId : "";
        version = "";
        this.type = ( type != null ) ? type : "";
        if ( nature == null )
        {
            throw new IllegalArgumentException( "metadata nature was not specified" );
        }
        this.nature = nature;
        this.file = null;
    }

    public DefaultMetadata( String groupId, String artifactId, String version, String type, Nature nature )
    {
        this.groupId = ( groupId != null ) ? groupId : "";
        this.artifactId = ( artifactId != null ) ? artifactId : "";
        this.version = ( version != null ) ? version : "";
        this.type = ( type != null ) ? type : "";
        if ( nature == null )
        {
            throw new IllegalArgumentException( "metadata nature was not specified" );
        }
        this.nature = nature;
        this.file = null;
    }

    public DefaultMetadata( String groupId, String artifactId, String version, String type, Nature nature, File file )
    {
        this.groupId = ( groupId != null ) ? groupId : "";
        this.artifactId = ( artifactId != null ) ? artifactId : "";
        this.version = ( version != null ) ? version : "";
        this.type = ( type != null ) ? type : "";
        if ( nature == null )
        {
            throw new IllegalArgumentException( "metadata nature was not specified" );
        }
        this.nature = nature;
        this.file = file;
    }

    public String getGroupId()
    {
        return groupId;
    }

    public String getArtifactId()
    {
        return artifactId;
    }

    public String getVersion()
    {
        return version;
    }

    public String getType()
    {
        return type;
    }

    public Nature getNature()
    {
        return nature;
    }

    public File getFile()
    {
        return file;
    }

    public Metadata setFile( File file )
    {
        return new DefaultMetadata( groupId, artifactId, version, type, nature, file );
    }

    @Override
    public String toString()
    {
        StringBuilder buffer = new StringBuilder( 128 );
        if ( getGroupId().length() > 0 )
        {
            buffer.append( getGroupId() );
        }
        if ( getArtifactId().length() > 0 )
        {
            buffer.append( ':' ).append( getArtifactId() );
        }
        if ( getVersion().length() > 0 )
        {
            buffer.append( ':' ).append( getVersion() );
        }
        buffer.append( '/' ).append( getType() );
        return buffer.toString();
    }

}
