package org.sonatype.aether.transfer;

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

import org.sonatype.aether.RepositoryException;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.RemoteRepository;

/**
 * @author Benjamin Bentmann
 */
public class ArtifactTransferException
    extends RepositoryException
{

    private final Artifact artifact;

    private final RemoteRepository repository;

    static String getString( String prefix, RemoteRepository repository )
    {
        if ( repository == null )
        {
            return "";
        }
        else
        {
            return prefix + repository.getId() + " (" + repository.getUrl() + ")";
        }
    }

    public ArtifactTransferException( Artifact artifact, RemoteRepository repository, String message )
    {
        super( message );

        this.artifact = artifact;
        this.repository = repository;
    }

    public ArtifactTransferException( Artifact artifact, RemoteRepository repository, Throwable cause )
    {
        super( "Could not transfer artifact " + artifact + getString( " from/to ", repository )
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
