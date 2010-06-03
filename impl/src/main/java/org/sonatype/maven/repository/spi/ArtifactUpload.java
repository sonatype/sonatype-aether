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
public class ArtifactUpload
    extends ArtifactTransfer
{

    public ArtifactUpload()
    {
        // enables default constructor
    }

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
