package org.sonatype.aether.test.util.connector.suite;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.metadata.Metadata;
import org.sonatype.aether.metadata.Metadata.Nature;
import org.sonatype.aether.repository.RepositoryPolicy;
import org.sonatype.aether.spi.connector.ArtifactDownload;
import org.sonatype.aether.spi.connector.ArtifactUpload;
import org.sonatype.aether.spi.connector.MetadataDownload;
import org.sonatype.aether.spi.connector.MetadataUpload;
import org.sonatype.aether.spi.connector.Transfer;
import org.sonatype.aether.test.util.FileUtil;
import org.sonatype.aether.test.util.impl.StubArtifact;
import org.sonatype.aether.test.util.impl.StubMetadata;

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

/**
 * @author Benjamin Hanzelmann
 */
public class ConnectorTestUtil
{

    public static <T extends Transfer> List<T> createTransfers( Class<T> cls, int count, File file )
    {
        ArrayList<T> ret = new ArrayList<T>();
    
        for ( int i = 0; i < count; i++ )
        {
            Artifact artifact =
                new StubArtifact( "testGroup", "testArtifact", "sources", "jar", ( i + 1 ) + "-test" );
            Metadata metadata =
                new StubMetadata( "testGroup", "testArtifact", ( i + 1 ) + "test", "jar",
                                     Metadata.Nature.RELEASE_OR_SNAPSHOT, file );
            String context = null;
            String checksumPolicy = RepositoryPolicy.CHECKSUM_POLICY_IGNORE;
    
            Object obj = null;
            if ( cls.isAssignableFrom( ArtifactUpload.class ) )
            {
                obj = new ArtifactUpload( artifact, file );
            }
            else if ( cls.isAssignableFrom( ArtifactDownload.class ) )
            {
                try
                {
                    obj =
                        new ArtifactDownload( artifact, context, file == null ? FileUtil.createTempFile( "" ) : file,
                                              checksumPolicy );
                }
                catch ( IOException e )
                {
                    throw new RuntimeException( e.getMessage(), e );
                }
            }
            else if ( cls.isAssignableFrom( MetadataUpload.class ) )
            {
                obj = new MetadataUpload( metadata, file );
            }
            else if ( cls.isAssignableFrom( MetadataDownload.class ) )
            {
                try
                {
                    obj =
                        new MetadataDownload( metadata, context, file == null ? FileUtil.createTempFile( "" ) : file,
                                              checksumPolicy );
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

}
