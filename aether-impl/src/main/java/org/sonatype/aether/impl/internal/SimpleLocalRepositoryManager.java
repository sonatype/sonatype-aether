package org.sonatype.aether.impl.internal;

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

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.SortedSet;
import java.util.TreeSet;

import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.metadata.Metadata;
import org.sonatype.aether.repository.LocalArtifactRegistration;
import org.sonatype.aether.repository.LocalArtifactRequest;
import org.sonatype.aether.repository.LocalArtifactResult;
import org.sonatype.aether.repository.LocalRepository;
import org.sonatype.aether.repository.LocalRepositoryManager;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.spi.log.Logger;

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
        this( basedir, "simple" );
    }

    public SimpleLocalRepositoryManager( String basedir )
    {
        this( ( basedir != null ) ? new File( basedir ) : null, "simple" );
    }

    SimpleLocalRepositoryManager( File basedir, String type )
    {
        if ( basedir == null )
        {
            throw new IllegalArgumentException( "base directory has not been specified" );
        }
        repository = new LocalRepository( basedir.getAbsoluteFile(), type );
    }

    public SimpleLocalRepositoryManager setLogger( Logger logger )
    {
        return this;
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

        path.append( '.' ).append( artifact.getExtension() );

        return path.toString();
    }

    public String getPathForRemoteArtifact( Artifact artifact, RemoteRepository repository, String context )
    {
        return getPathForLocalArtifact( artifact );
    }

    public String getPathForLocalMetadata( Metadata metadata )
    {
        return getPath( metadata, "local" );
    }

    public String getPathForRemoteMetadata( Metadata metadata, RemoteRepository repository, String context )
    {
        return getPath( metadata, getRepositoryKey( repository, context ) );
    }

    String getRepositoryKey( RemoteRepository repository, String context )
    {
        String key;

        if ( repository.isRepositoryManager() )
        {
            // repository serves dynamic contents, take request parameters into account for key

            StringBuilder buffer = new StringBuilder( 128 );

            buffer.append( repository.getId() );

            buffer.append( '-' );

            SortedSet<String> subKeys = new TreeSet<String>();
            for ( RemoteRepository mirroredRepo : repository.getMirroredRepositories() )
            {
                subKeys.add( mirroredRepo.getId() );
            }

            SafeDigest digest = new SafeDigest();
            digest.update( context );
            for ( String subKey : subKeys )
            {
                digest.update( subKey );
            }
            buffer.append( digest.digest() );

            key = buffer.toString();
        }
        else
        {
            // repository serves static contents, its id is sufficient as key

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

    public LocalArtifactResult find( RepositorySystemSession session, LocalArtifactRequest request )
    {
        String path = getPathForLocalArtifact( request.getArtifact() );
        File file = new File( getRepository().getBasedir(), path );

        LocalArtifactResult result = new LocalArtifactResult( request );
        if ( file.isFile() )
        {
            result.setFile( file );
            result.setAvailable( true );
        }

        return result;
    }

    public void add( RepositorySystemSession session, LocalArtifactRegistration request )
    {
        // noop
    }

    @Override
    public String toString()
    {
        return String.valueOf( getRepository() );
    }

    static class SafeDigest
    {

        private MessageDigest digest;

        private long hash;

        public SafeDigest()
        {
            try
            {
                digest = MessageDigest.getInstance( "SHA-1" );
            }
            catch ( NoSuchAlgorithmException e )
            {
                digest = null;
                hash = 13;
            }
        }

        public void update( String data )
        {
            if ( data == null )
            {
                return;
            }
            if ( digest != null )
            {
                try
                {
                    digest.update( data.getBytes( "UTF-8" ) );
                }
                catch ( UnsupportedEncodingException e )
                {
                    // broken JVM
                }
            }
            else
            {
                hash = hash * 31 + data.hashCode();
            }
        }

        public String digest()
        {
            if ( digest != null )
            {
                StringBuilder buffer = new StringBuilder( "64" );

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

                return buffer.toString();
            }
            else
            {
                return Long.toHexString( hash );
            }
        }
    }

}
