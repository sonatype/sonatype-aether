package org.sonatype.aether.impl.internal;

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

import static junit.framework.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.sonatype.aether.Artifact;
import org.sonatype.aether.ArtifactTransferException;
import org.sonatype.aether.Metadata;
import org.sonatype.aether.MetadataTransferException;
import org.sonatype.aether.spi.connector.ArtifactDownload;
import org.sonatype.aether.spi.connector.ArtifactUpload;
import org.sonatype.aether.spi.connector.MetadataDownload;
import org.sonatype.aether.spi.connector.MetadataUpload;
import org.sonatype.aether.spi.connector.RepositoryConnector;
import org.sonatype.aether.test.util.FileUtil;

/**
 * A repository connector doing nothing but recording all get/put-requests.
 * 
 * @author Benjamin Hanzelmann
 */
class RecordingRepositoryConnector
    implements RepositoryConnector
{
    private Artifact[] expectGet;

    private Artifact[] expectPut;

    private Metadata[] expectGetMD;

    private Metadata[] expectPutMD;

    private List<Artifact> actualGet = new ArrayList<Artifact>();

    private List<Metadata> actualGetMD = new ArrayList<Metadata>();

    private List<Artifact> actualPut = new ArrayList<Artifact>();

    private List<Metadata> actualPutMD = new ArrayList<Metadata>();

    public RecordingRepositoryConnector( Artifact[] expectGet, Artifact[] expectPut, Metadata[] expectGetMD,
                                         Metadata[] expectPutMD )
    {
        super();
        this.expectGet = expectGet;
        this.expectPut = expectPut;
        this.expectGetMD = expectGetMD;
        this.expectPutMD = expectPutMD;
    }

    public void get( Collection<? extends ArtifactDownload> artifactDownloads,
                     Collection<? extends MetadataDownload> metadataDownloads )
    {
        try
        {
            if ( artifactDownloads != null )
            {
                for ( ArtifactDownload artifactDownload : artifactDownloads )
                {
                    Artifact artifact = artifactDownload.getArtifact();
                    this.actualGet.add( artifact );
                    artifactDownload.setFile( FileUtil.createTempFile( artifact.toString() ) );
                }
                for ( MetadataDownload metadataDownload : metadataDownloads )
                {
                    Metadata metadata = metadataDownload.getMetadata();
                    this.actualGetMD.add( metadata );
                    metadataDownload.setFile( FileUtil.createTempFile( metadata.toString() ) );
                }
            }
        }
        catch ( IOException e )
        {
            throw new IllegalStateException( "Cannot create temporary file", e );
        }

    }

    public void put( Collection<? extends ArtifactUpload> artifactUploads,
                     Collection<? extends MetadataUpload> metadataUploads )
    {
        for ( ArtifactUpload artifactUpload : artifactUploads )
        {
            // mimic "real" connector
            if ( artifactUpload.getArtifact().getFile() == null )
            {
                artifactUpload.setException( new ArtifactTransferException( artifactUpload.getArtifact(), null,
                                                                            "no file" ) );
            }
            this.actualPut.add( artifactUpload.getArtifact() );
        }
        for ( MetadataUpload metadataUpload : metadataUploads )
        {
            // mimic "real" connector
            if ( metadataUpload.getMetadata().getFile() == null )
            {
                metadataUpload.setException( new MetadataTransferException( metadataUpload.getMetadata(), null,
                                                                            "no file" ) );
            }
            this.actualPutMD.add( metadataUpload.getMetadata() );
        }

    }

    public void close()
    {
    }

    public void assertSeenExpected()
    {
        assertSeenExpected( actualGet, expectGet );
        assertSeenExpected( actualGetMD, expectGetMD );
        assertSeenExpected( actualPut, expectPut );
        assertSeenExpected( actualPutMD, expectPutMD );
    }

    private void assertSeenExpected( List<? extends Object> actual, Object[] expected )
    {
        if ( expected == null )
        {
            expected = new Object[0];
        }

        assertTrue( "different number of expected and actual elements", actual.size() == expected.length );
        int idx = 0;
        for ( Object actualObject : actual )
        {
            assertEquals( "seen object differs", expected[idx++], actualObject );
        }
    }

    public List<Artifact> getActualArtifactGetRequests()
    {
        return actualGet;
    }

    public List<Metadata> getActualMetadataGetRequests()
    {
        return actualGetMD;
    }

    public List<Artifact> getActualArtifactPutRequests()
    {
        return actualPut;
    }

    public List<Metadata> getActualMetadataPutRequests()
    {
        return actualPutMD;
    }

}