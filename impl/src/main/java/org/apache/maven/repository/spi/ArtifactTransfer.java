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
public abstract class ArtifactTransfer
    extends Transfer
{

    private Artifact artifact;

    private File file;

    private ArtifactTransferException exception;

    public Artifact getArtifact()
    {
        return artifact;
    }

    public ArtifactTransfer setArtifact( Artifact artifact )
    {
        this.artifact = artifact;
        return this;
    }

    public File getFile()
    {
        return file;
    }

    public ArtifactTransfer setFile( File file )
    {
        this.file = file;
        return this;
    }

    public ArtifactTransferException getException()
    {
        return exception;
    }

    public ArtifactTransfer setException( ArtifactTransferException exception )
    {
        this.exception = exception;
        return this;
    }

}
