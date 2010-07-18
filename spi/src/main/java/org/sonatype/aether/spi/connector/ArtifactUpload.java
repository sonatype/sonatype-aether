package org.sonatype.aether.spi.connector;

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

import org.sonatype.aether.Artifact;
import org.sonatype.aether.ArtifactTransferException;

/**
 * An upload of an artifact to a remote repository.
 * 
 * @author Benjamin Bentmann
 */
public class ArtifactUpload
    extends ArtifactTransfer
{

    /**
     * Creates a new uninitialized upload.
     */
    public ArtifactUpload()
    {
        // enables default constructor
    }

    /**
     * Creates a new upload with the specified properties.
     * 
     * @param artifact The artifact to upload, may be {@code null}.
     * @param file The local file to upload the artifact from, may be {@code null}.
     */
    public ArtifactUpload( Artifact artifact, File file )
    {
        setArtifact( artifact );
        setFile( file );
    }

    @Override
    public ArtifactUpload setArtifact( Artifact artifact )
    {
        super.setArtifact( artifact );
        return this;
    }

    @Override
    public ArtifactUpload setFile( File file )
    {
        super.setFile( file );
        return this;
    }

    @Override
    public ArtifactUpload setException( ArtifactTransferException exception )
    {
        super.setException( exception );
        return this;
    }

}
