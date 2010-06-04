package org.sonatype.maven.repository.spi;

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

import org.sonatype.maven.repository.Artifact;
import org.sonatype.maven.repository.ArtifactTransferException;

/**
 * A download of an artifact from a remote repository.
 * 
 * @author Benjamin Bentmann
 */
public class ArtifactDownload
    extends ArtifactTransfer
{

    private boolean existenceCheck;

    private String checksumPolicy = "";

    private String context = "";

    /**
     * Creates a new uninitialized download.
     */
    public ArtifactDownload()
    {
        // enables default constructor
    }

    /**
     * Creates a new download with the specified properties.
     * 
     * @param artifact The artifact to download, may be {@code null}.
     * @param context The context in which this download is performed, may be {@code null}.
     * @param file The local file to download the artifact to, may be {@code null}.
     * @param checksumPolicy The checksum policy, may be {@code null}.
     */
    public ArtifactDownload( Artifact artifact, String context, File file, String checksumPolicy )
    {
        setArtifact( artifact );
        setContext( context );
        setFile( file );
        setChecksumPolicy( checksumPolicy );
    }

    @Override
    public ArtifactDownload setArtifact( Artifact artifact )
    {
        super.setArtifact( artifact );
        return this;
    }

    @Override
    public ArtifactDownload setFile( File file )
    {
        super.setFile( file );
        return this;
    }

    /**
     * Indicates whether this transfer shall only verify the existence of the artifact in the remote repository rather
     * than actually downloading the file.
     * 
     * @return {@code true} if only the artifact existence shall be verified, {@code false} to actually download the
     *         artifact.
     */
    public boolean isExistenceCheck()
    {
        return existenceCheck;
    }

    /**
     * Controls whether this transfer shall only verify the existence of the artifact in the remote repository rather
     * than actually downloading the file.
     * 
     * @param existenceCheck {@code true} if only the artifact existence shall be verified, {@code false} to actually
     *            download the artifact.
     * @return This transfer for chaining, never {@code null}.
     */
    public ArtifactDownload setExistenceCheck( boolean existenceCheck )
    {
        this.existenceCheck = existenceCheck;
        return this;
    }

    /**
     * Gets the checksum policy for this transfer.
     * 
     * @return The checksum policy, never {@code null}.
     */
    public String getChecksumPolicy()
    {
        return checksumPolicy;
    }

    /**
     * Sets the checksum policy for this transfer.
     * 
     * @param checksumPolicy The checksum policy, may be {@code null}.
     * @return This transfer for chaining, never {@code null}.
     */
    public ArtifactDownload setChecksumPolicy( String checksumPolicy )
    {
        this.checksumPolicy = ( checksumPolicy != null ) ? checksumPolicy : "";
        return this;
    }

    /**
     * Gets the context of this transfer.
     * 
     * @return The context id, never {@code null}.
     */
    public String getContext()
    {
        return context;
    }

    /**
     * Sets the context of this transfer.
     * 
     * @param context The context id, may be {@code null}.
     * @return This transfer for chaining, never {@code null}.
     */
    public ArtifactDownload setContext( String context )
    {
        this.context = ( context != null ) ? context : "";
        return this;
    }

    @Override
    public ArtifactDownload setException( ArtifactTransferException exception )
    {
        super.setException( exception );
        return this;
    }

}
