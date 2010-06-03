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
 * @author Benjamin Bentmann
 */
public class ArtifactDownload
    extends ArtifactTransfer
{

    private boolean existenceCheck;

    private String checksumPolicy;

    private String context;

    public ArtifactDownload()
    {
        // enables default constructor
    }

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

    public String getContext()
    {
        return context;
    }

    public ArtifactDownload setContext( String context )
    {
        this.context = context;
        return this;
    }

    @Override
    public ArtifactDownload setException( ArtifactTransferException exception )
    {
        super.setException( exception );
        return this;
    }

}
