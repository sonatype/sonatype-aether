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
import java.security.MessageDigest;
import java.util.SortedSet;
import java.util.TreeSet;

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
        return getPath( metadata, getRepositoryKey( repository ) );
    }

    String getRepositoryKey( RemoteRepository repository )
    {
        String key;

        if ( repository.isRepositoryManager() )
        {
            // repository serving dynamic contents, take mirrored repositories into account for key

            StringBuilder buffer = new StringBuilder( 128 );

            buffer.append( repository.getId() );

            if ( !repository.getMirroredRepositories().isEmpty() )
            {
                buffer.append( '-' );

                SortedSet<String> subKeys = new TreeSet<String>();
                for ( RemoteRepository mirroredRepo : repository.getMirroredRepositories() )
                {
                    subKeys.add( mirroredRepo.getId() );
                }

                try
                {
                    MessageDigest digest = MessageDigest.getInstance( "SHA-1" );
                    for ( String subKey : subKeys )
                    {
                        digest.update( subKey.getBytes( "UTF-8" ) );
                    }
                    byte[] bytes = digest.digest();

                    for ( int i = 0; i < bytes.length; i++ )
                    {
                        int b = bytes[i] & 0xFF;

                        if ( b < 0x10 )
                        {
                            buffer.append( '0' );
                        }

                        buffer.append( Integer.toHexString( b ) );
                    }
                }
                catch ( Exception e )
                {
                    long hash = 13;
                    for ( String subKey : subKeys )
                    {
                        hash = hash * 31 + subKey.hashCode();
                    }
                    buffer.append( hash );
                }
            }

            key = buffer.toString();
        }
        else
        {
            // repository serving static contents, its id is sufficient as key

            key = repository.getId();
        }

        return key;
    }

    private String getPath( Metadata metadata, String repositoryKey )
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

        path.append( insertRepositoryKey( metadata.getType(), repositoryKey ) );

        return path.toString();
    }

    private String insertRepositoryKey( String filename, String repositoryKey )
    {
        String result;
        int idx = filename.indexOf( '.' );
        if ( idx < 0 )
        {
            result = filename + '-' + repositoryKey;
        }
        else
        {
            result = filename.substring( 0, idx ) + '-' + repositoryKey + filename.substring( idx );
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
