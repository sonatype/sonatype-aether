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

import org.apache.maven.repository.Metadata;
import org.apache.maven.repository.MetadataTransferException;

/**
 * @author Benjamin Bentmann
 */
public abstract class MetadataTransfer
{

    private Metadata metadata;

    private File file;

    private MetadataTransferException exception;

    public Metadata getMetadata()
    {
        return metadata;
    }

    public MetadataTransfer setMetadata( Metadata metadata )
    {
        this.metadata = metadata;
        return this;
    }

    public File getFile()
    {
        return file;
    }

    public MetadataTransfer setFile( File file )
    {
        this.file = file;
        return this;
    }

    public MetadataTransferException getException()
    {
        return exception;
    }

    public MetadataTransfer setException( MetadataTransferException exception )
    {
        this.exception = exception;
        return this;
    }

}
