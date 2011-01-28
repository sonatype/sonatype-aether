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
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sonatype.aether.artifact.Artifact;

/**
 * A skeleton class for artifacts that implements {@link Object#equals(Object)}, {@link Object#hashCode()} and
 * {@link Object#toString()}.
 * 
 * @author Benjamin Bentmann
 */
public abstract class AbstractArtifact
    implements Artifact
{

    private static final String SNAPSHOT = "SNAPSHOT";

    private static final Pattern SNAPSHOT_TIMESTAMP = Pattern.compile( "^(.*-)?([0-9]{8}.[0-9]{6}-[0-9]+)$" );

    protected static boolean isSnapshot( String version )
    {
        return version.endsWith( SNAPSHOT ) || SNAPSHOT_TIMESTAMP.matcher( version ).matches();
    }

    protected static String toBaseVersion( String version )
    {
        String baseVersion;

        if ( version == null )
        {
            baseVersion = version;
        }
        else if ( version.startsWith( "[" ) || version.startsWith( "(" ) )
        {
            baseVersion = version;
        }
        else
        {
            Matcher m = SNAPSHOT_TIMESTAMP.matcher( version );
            if ( m.matches() )
            {
                if ( m.group( 1 ) != null )
                {
                    baseVersion = m.group( 1 ) + SNAPSHOT;
                }
                else
                {
                    baseVersion = SNAPSHOT;
                }
            }
            else
            {
                baseVersion = version;
            }
        }

        return baseVersion;
    }

    public Artifact setVersion( String version )
    {
        if ( getVersion().equals( version ) )
        {
            return this;
        }
        return new DefaultArtifact( getGroupId(), getArtifactId(), getClassifier(), getExtension(), version, getFile(),
                                    getProperties() );
    }

    public Artifact setFile( File file )
    {
        if ( eq( getFile(), file ) )
        {
            return this;
        }
        return new DefaultArtifact( getGroupId(), getArtifactId(), getClassifier(), getExtension(), getVersion(), file,
                                    getProperties() );
    }

    public Artifact setProperties( Map<String, String> properties )
    {
        if ( getProperties().equals( properties ) )
        {
            return this;
        }
        return new DefaultArtifact( getGroupId(), getArtifactId(), getClassifier(), getExtension(), getVersion(),
                                    properties, getFile() );
    }

    @Override
    public String toString()
    {
        StringBuilder buffer = new StringBuilder( 128 );
        buffer.append( getGroupId() );
        buffer.append( ':' ).append( getArtifactId() );
        buffer.append( ':' ).append( getExtension() );
        if ( getClassifier().length() > 0 )
        {
            buffer.append( ':' ).append( getClassifier() );
        }
        buffer.append( ':' ).append( getVersion() );
        return buffer.toString();
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( obj == this )
        {
            return true;
        }
        else if ( !( obj instanceof Artifact ) )
        {
            return false;
        }

        Artifact that = (Artifact) obj;

        return getArtifactId().equals( that.getArtifactId() ) && getGroupId().equals( that.getGroupId() )
            && getVersion().equals( that.getVersion() ) && getExtension().equals( that.getExtension() )
            && getClassifier().equals( that.getClassifier() ) && eq( getFile(), that.getFile() )
            && getProperties().equals( that.getProperties() );
    }

    private static <T> boolean eq( T s1, T s2 )
    {
        return s1 != null ? s1.equals( s2 ) : s2 == null;
    }

    @Override
    public int hashCode()
    {
        int hash = 17;
        hash = hash * 31 + getGroupId().hashCode();
        hash = hash * 31 + getArtifactId().hashCode();
        hash = hash * 31 + getExtension().hashCode();
        hash = hash * 31 + getClassifier().hashCode();
        hash = hash * 31 + getVersion().hashCode();
        hash = hash * 31 + getProperties().hashCode();
        hash = hash * 31 + hash( getFile() );
        return hash;
    }

    private static int hash( Object obj )
    {
        return ( obj != null ) ? obj.hashCode() : 0;
    }

}
