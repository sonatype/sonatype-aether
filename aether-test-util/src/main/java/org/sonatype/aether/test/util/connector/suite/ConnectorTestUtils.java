package org.sonatype.aether.test.util.connector.suite;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.metadata.Metadata;
import org.sonatype.aether.repository.RepositoryPolicy;
import org.sonatype.aether.spi.connector.ArtifactDownload;
import org.sonatype.aether.spi.connector.ArtifactTransfer;
import org.sonatype.aether.spi.connector.ArtifactUpload;
import org.sonatype.aether.spi.connector.MetadataDownload;
import org.sonatype.aether.spi.connector.MetadataUpload;
import org.sonatype.aether.spi.connector.Transfer;
import org.sonatype.aether.test.util.TestFileUtils;
import org.sonatype.aether.test.util.impl.StubArtifact;
import org.sonatype.aether.test.util.impl.StubMetadata;

/**
 * @author Benjamin Hanzelmann
 */
public class ConnectorTestUtils
{

    /**
     * Creates transfer objects according to the given class. If the file parameter is {@code null}, a new temporary
     * file will be created for downloads. Uploads will just use the parameter as it is.
     */
    public static <T extends Transfer> List<T> createTransfers( Class<T> cls, int count, File file )
    {
        ArrayList<T> ret = new ArrayList<T>();
        Object item;
        if ( ArtifactTransfer.class.isAssignableFrom( cls ) )
        {
            item = new StubArtifact( "testGroup", "testArtifact", "sources", "jar", "will be replaced" );
        }
        else
        {
            item =
                new StubMetadata( "testGroup", "testArtifact", "will be replaced", "jar",
                                  Metadata.Nature.RELEASE_OR_SNAPSHOT, file );
        }

        for ( int i = 0; i < count; i++ )
        {
            String context = null;
            String checksumPolicy = RepositoryPolicy.CHECKSUM_POLICY_IGNORE;

            Object obj = null;
            if ( cls.isAssignableFrom( ArtifactUpload.class ) )
            {
                Artifact artifact = ( (Artifact) item ).setVersion( ( i + 1 ) + "-test" );
                obj = new ArtifactUpload( artifact, file );
            }
            else if ( cls.isAssignableFrom( ArtifactDownload.class ) )
            {
                try
                {
                    Artifact artifact = ( (Artifact) item ).setVersion( ( i + 1 ) + "-test" );
                    obj = new ArtifactDownload( artifact, context, safeFile( file ), checksumPolicy );
                }
                catch ( IOException e )
                {
                    throw new RuntimeException( e.getMessage(), e );
                }
            }
            else if ( cls.isAssignableFrom( MetadataUpload.class ) )
            {
                Metadata metadata = ( (StubMetadata) item ).setVersion( ( i + 1 ) + "-test" );
                obj = new MetadataUpload( metadata, file );
            }
            else if ( cls.isAssignableFrom( MetadataDownload.class ) )
            {
                try
                {
                    Metadata metadata = ( (StubMetadata) item ).setVersion( ( i + 1 ) + "-test" );
                    obj = new MetadataDownload( metadata, context, safeFile( file ), checksumPolicy );
                }
                catch ( IOException e )
                {
                    throw new RuntimeException( e.getMessage(), e );
                }
            }

            ret.add( cls.cast( obj ) );
        }

        return ret;
    }

    private static File safeFile( File file )
        throws IOException
    {
        return file == null ? TestFileUtils.createTempFile( "" ) : file;
    }

}
