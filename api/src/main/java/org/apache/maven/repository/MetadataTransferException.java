package org.apache.maven.repository;

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

/**
 * @author Benjamin Bentmann
 */
public class MetadataTransferException
    extends RepositoryException
{

    private final Metadata metadata;

    public MetadataTransferException( Metadata metadata, String message )
    {
        super( message );

        this.metadata = metadata;
    }

    public MetadataTransferException( Metadata metadata, Throwable cause )
    {
        super( "Could not transfer metadata " + metadata + ( cause != null ? ": " + getMessage( cause ) : "" ), cause );

        this.metadata = metadata;
    }

    public Metadata getMetadata()
    {
        return metadata;
    }

}
