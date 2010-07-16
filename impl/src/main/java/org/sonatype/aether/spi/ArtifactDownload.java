package org.sonatype.aether.spi;

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
import java.util.Collections;
import java.util.List;

import org.sonatype.aether.Artifact;
import org.sonatype.aether.ArtifactTransferException;
import org.sonatype.aether.RemoteRepository;

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

    private List<RemoteRepository> repositories = Collections.emptyList();

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
        setRequestContext( context );
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
    public String getRequestContext()
    {
        return context;
    }

    /**
     * Sets the context of this transfer.
     * 
     * @param context The context id, may be {@code null}.
     * @return This transfer for chaining, never {@code null}.
     */
    public ArtifactDownload setRequestContext( String context )
    {
        this.context = ( context != null ) ? context : "";
        return this;
    }

    /**
     * Gets the remote repositories that are being aggregated by the physically contacted remote repository (i.e. a
     * repository manager).
     * 
     * @return The remote repositories being aggregated, never {@code null}.
     */
    public List<RemoteRepository> getRepositories()
    {
        return repositories;
    }

    /**
     * Sets the remote repositories that are being aggregated by the physically contacted remote repository (i.e. a
     * repository manager).
     * 
     * @param repositories The remote repositories being aggregated, may be {@code null}.
     * @return This transfer for chaining, never {@code null}.
     */
    public ArtifactDownload setRepositories( List<RemoteRepository> repositories )
    {
        if ( repositories == null )
        {
            this.repositories = Collections.emptyList();
        }
        else
        {
            this.repositories = repositories;
        }
        return this;
    }

    @Override
    public ArtifactDownload setException( ArtifactTransferException exception )
    {
        super.setException( exception );
        return this;
    }

}
