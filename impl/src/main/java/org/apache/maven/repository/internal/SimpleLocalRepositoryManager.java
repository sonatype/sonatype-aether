package org.apache.maven.repository.internal;

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
import org.apache.maven.repository.LocalArtifactQuery;
import org.apache.maven.repository.LocalRepository;
import org.apache.maven.repository.LocalRepositoryManager;
import org.apache.maven.repository.Metadata;
import org.apache.maven.repository.RemoteRepository;

/**
 * A local repository manager that realizes the classical Maven 2.0 local repository.
 * 
 * @author Benjamin Bentmann
 */
public class SimpleLocalRepositoryManager
    implements LocalRepositoryManager
{

    private final LocalRepository repository;

    public SimpleLocalRepositoryManager( File basedir )
    {
        this( basedir, "default" );
    }

    SimpleLocalRepositoryManager( File basedir, String type )
    {
        if ( basedir == null )
        {
            throw new IllegalArgumentException( "base directory has not been specified" );
        }
        repository = new LocalRepository( basedir, type );
    }

    public LocalRepository getRepository()
    {
        return repository;
    }

    public String getPathForLocalArtifact( Artifact artifact )
    {
        StringBuilder path = new StringBuilder( 128 );

        path.append( artifact.getGroupId().replace( '.', '/' ) ).append( '/' );

        path.append( artifact.getArtifactId() ).append( '/' );

        path.append( artifact.getBaseVersion() ).append( '/' );

        path.append( artifact.getArtifactId() ).append( '-' ).append( artifact.getVersion() );

        if ( artifact.getClassifier().length() > 0 )
        {
            path.append( '-' ).append( artifact.getClassifier() );
        }

        path.append( '.' ).append( artifact.getType() );

        return path.toString();
    }

    public String getPathForRemoteArtifact( Artifact artifact, RemoteRepository repository )
    {
        return getPathForLocalArtifact( artifact );
    }

    public String getPathForLocalMetadata( Metadata metadata )
    {
        return getPath( metadata, "local" );
    }

    public String getPathForRemoteMetadata( Metadata metadata, RemoteRepository repository )
    {
        return getPath( metadata, repository.getId() );
    }

    private String getPath( Metadata metadata, String repositoryId )
    {
        StringBuilder path = new StringBuilder( 128 );

        if ( metadata.getGroupId().length() > 0 )
        {
            path.append( metadata.getGroupId().replace( '.', '/' ) ).append( '/' );

            if ( metadata.getArtifactId().length() > 0 )
            {
                path.append( metadata.getArtifactId() ).append( '/' );

                if ( metadata.getVersion().length() > 0 )
                {
                    path.append( metadata.getVersion() ).append( '/' );
                }
            }
        }

        path.append( insertRepositoryId( metadata.getType(), repositoryId ) );

        return path.toString();
    }

    private String insertRepositoryId( String filename, String repositoryId )
    {
        String result;
        int idx = filename.indexOf( '.' );
        if ( idx < 0 )
        {
            result = filename + '-' + repositoryId;
        }
        else
        {
            result = filename.substring( 0, idx ) + '-' + repositoryId + filename.substring( idx );
        }
        return result;
    }

    public void find( LocalArtifactQuery query )
    {
        String path = getPathForLocalArtifact( query.getArtifact() );
        File file = new File( getRepository().getBasedir(), path );
        if ( file.isFile() )
        {
            query.setFile( file );
            query.setAvailable( true );
        }
    }

    public void addLocalArtifact( Artifact artifact )
    {
        // noop
    }

    public void addRemoteArtifact( Artifact artifact, RemoteRepository repository )
    {
        // noop
    }

}
