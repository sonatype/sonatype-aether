package org.sonatype.aether.impl;

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
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.WriterFactory;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.aether.MergeableMetadata;
import org.sonatype.aether.RepositoryException;
import org.sonatype.aether.impl.metadata.Metadata;
import org.sonatype.aether.impl.metadata.Versioning;
import org.sonatype.aether.impl.metadata.io.xpp3.MetadataXpp3Reader;
import org.sonatype.aether.impl.metadata.io.xpp3.MetadataXpp3Writer;

/**
 * @author Benjamin Bentmann
 */
abstract class MavenMetadata
    implements MergeableMetadata
{

    private final File file;

    protected final Metadata metadata;

    protected MavenMetadata( Metadata metadata, File file )
    {
        this.metadata = metadata;
        this.file = file;
    }

    public String getType()
    {
        return "maven-metadata.xml";
    }

    public File getFile()
    {
        return file;
    }

    public void merge( File existing, File result )
        throws RepositoryException
    {
        Metadata recessive = read( existing );

        merge( recessive );

        write( result, metadata );
    }

    protected void merge( Metadata recessive )
    {
        Versioning versioning = recessive.getVersioning();
        if ( versioning != null )
        {
            versioning.setLastUpdated( null );
        }

        Metadata dominant = metadata;

        versioning = dominant.getVersioning();
        if ( versioning != null )
        {
            versioning.updateTimestamp();
        }

        dominant.merge( recessive );
    }

    private Metadata read( File metadataFile )
        throws RepositoryException
    {
        if ( metadataFile.length() <= 0 )
        {
            return new Metadata();
        }

        Reader reader = null;
        try
        {
            reader = ReaderFactory.newXmlReader( metadataFile );
            return new MetadataXpp3Reader().read( reader, false );
        }
        catch ( IOException e )
        {
            throw new RepositoryException( "Could not read metadata " + metadataFile + ": " + e.getMessage(), e );
        }
        catch ( XmlPullParserException e )
        {
            throw new RepositoryException( "Could not parse metadata " + metadataFile + ": " + e.getMessage(), e );
        }
        finally
        {
            IOUtil.close( reader );
        }
    }

    private void write( File metadataFile, Metadata metadata )
        throws RepositoryException
    {
        Writer writer = null;
        try
        {
            metadataFile.getParentFile().mkdirs();
            writer = WriterFactory.newXmlWriter( metadataFile );
            new MetadataXpp3Writer().write( writer, metadata );
        }
        catch ( IOException e )
        {
            throw new RepositoryException( "Could not write metadata " + metadataFile + ": " + e.getMessage(), e );
        }
        finally
        {
            IOUtil.close( writer );
        }
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
