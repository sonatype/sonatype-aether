package org.apache.maven.repository.spi;

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

import org.apache.maven.repository.Artifact;
import org.apache.maven.repository.ArtifactTransferException;

/**
 * @author Benjamin Bentmann
 */
public class ArtifactDownload
{

    private Artifact artifact;

    private File file;

    private boolean existenceCheck;

    private String checksumPolicy;

    private ArtifactTransferException exception;

    public ArtifactDownload()
    {
        // enables default constructor
    }

    public ArtifactDownload( Artifact artifact, File file, String checksumPolicy )
    {
        setArtifact( artifact );
        setFile( file );
        setChecksumPolicy( checksumPolicy );
    }

    public Artifact getArtifact()
    {
        return artifact;
    }

    public ArtifactDownload setArtifact( Artifact artifact )
    {
        this.artifact = artifact;
        return this;
    }

    public File getFile()
    {
        return file;
    }

    public ArtifactDownload setFile( File file )
    {
        this.file = file;
        return this;
    }

    public boolean isExistenceCheck()
    {
        return existenceCheck;
    }

    public ArtifactDownload setExistenceCheck( boolean existenceCheck )
    {
        this.existenceCheck = existenceCheck;
        return this;
    }

    public String getChecksumPolicy()
    {
        return checksumPolicy;
    }

    public ArtifactDownload setChecksumPolicy( String checksumPolicy )
    {
        this.checksumPolicy = checksumPolicy;
        return this;
    }

    public ArtifactTransferException getException()
    {
        return exception;
    }

    public ArtifactDownload setException( ArtifactTransferException exception )
    {
        this.exception = exception;
        return this;
    }

}
