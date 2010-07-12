package org.sonatype.maven.repository;

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

/**
 * @author Benjamin Bentmann
 */
public class ArtifactTransferException
    extends RepositoryException
{

    private final Artifact artifact;

    private final RemoteRepository repository;

    public ArtifactTransferException( Artifact artifact, RemoteRepository repository, String message )
    {
        super( message );

        this.artifact = artifact;
        this.repository = repository;
    }

    public ArtifactTransferException( Artifact artifact, RemoteRepository repository, Throwable cause )
    {
        super( "Could not transfer artifact " + artifact + ( repository != null ? " from " + repository : "" )
            + getMessage( ": ", cause ), cause );

        this.artifact = artifact;
        this.repository = repository;
    }

    public Artifact getArtifact()
    {
        return artifact;
    }

    public RemoteRepository getRepository()
    {
        return repository;
    }

}
