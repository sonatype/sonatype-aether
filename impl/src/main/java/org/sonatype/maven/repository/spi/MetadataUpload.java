package org.sonatype.maven.repository.spi;

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

import org.sonatype.maven.repository.Metadata;
import org.sonatype.maven.repository.MetadataTransferException;

/**
 * An upload of metadata to a remote repository.
 * 
 * @author Benjamin Bentmann
 */
public class MetadataUpload
    extends MetadataTransfer
{

    /**
     * Creates a new uninitialized upload.
     */
    public MetadataUpload()
    {
        // enables default constructor
    }

    /**
     * Creates a new upload with the specified properties.
     * 
     * @param metadata The metadata to upload, may be {@code null}.
     * @param file The local file to upload the metadata from, may be {@code null}.
     */
    public MetadataUpload( Metadata metadata, File file )
    {
        setMetadata( metadata );
        setFile( file );
    }

    @Override
    public MetadataUpload setMetadata( Metadata metadata )
    {
        super.setMetadata( metadata );
        return this;
    }

    @Override
    public MetadataUpload setFile( File file )
    {
        super.setFile( file );
        return this;
    }

    @Override
    public MetadataUpload setException( MetadataTransferException exception )
    {
        super.setException( exception );
        return this;
    }

}
