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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;

import org.sonatype.aether.Artifact;
import org.sonatype.aether.LocalArtifactRequest;
import org.sonatype.aether.LocalArtifactResult;
import org.sonatype.aether.RemoteRepository;
import org.sonatype.aether.spi.log.Logger;

/**
 * A local repository manager that builds upon the classical Maven 2.0 local repository structure but additionally keeps
 * track of from what repositories a cached artifact was resolved. Resolution of locally cached artifacts will be
 * rejected in case the current resolution request does not match the known source repositories of an artifact, thereby
 * emulating physically separated artifact caches per remote repository.
 * 
 * @author Benjamin Bentmann
 */
public class EnhancedLocalRepositoryManager
    extends SimpleLocalRepositoryManager
{

    private static final String LOCAL_REPO_ID = "";

    private TrackingFileManager trackingFileManager;

    public EnhancedLocalRepositoryManager( File basedir )
    {
        super( basedir, "enhanced" );
        trackingFileManager = new TrackingFileManager();
    }

    @Override
    public EnhancedLocalRepositoryManager setLogger( Logger logger )
    {
        super.setLogger( logger );
        trackingFileManager.setLogger( logger );
        return this;
    }

    @Override
    public LocalArtifactResult find( LocalArtifactRequest request )
    {
        String path = getPathForLocalArtifact( request.getArtifact() );
        File file = new File( getRepository().getBasedir(), path );

        LocalArtifactResult result = new LocalArtifactResult( request );

        if ( file.isFile() )
        {
            result.setFile( file );

            Properties props = readRepos( file );

            if ( props.get( getKey( file, LOCAL_REPO_ID ) ) != null )
            {
                result.setAvailable( true );
            }
            else
            {
                String context = request.getContext();
                for ( RemoteRepository repository : request.getRepositories() )
                {
                    if ( props.get( getKey( file, getRepositoryKey( repository, context ) ) ) != null )
                    {
                        result.setAvailable( true );
                        break;
                    }
                }
                if ( !result.isAvailable() && !isTracked( props, file ) )
                {
                    /*
                     * NOTE: The artifact is present but not tracked at all, for inter-op with Maven 2.x, assume the
                     * artifact was locally built.
                     */
                    result.setAvailable( true );
                }
            }
        }

        return result;
    }

    @Override
    public void addLocalArtifact( Artifact artifact )
    {
        addArtifact( artifact, Collections.singleton( LOCAL_REPO_ID ) );
    }

    @Override
    public void addRemoteArtifact( Artifact artifact, RemoteRepository repository, Collection<String> contexts )
    {
        addArtifact( artifact, getRepositoryKeys( repository, contexts ) );
    }

    private Collection<String> getRepositoryKeys( RemoteRepository repository, Collection<String> contexts )
    {
        Collection<String> keys = new HashSet<String>();

        if ( contexts != null )
        {
            for ( String context : contexts )
            {
                keys.add( getRepositoryKey( repository, context ) );
            }
        }

        return keys;
    }

    private void addArtifact( Artifact artifact, Collection<String> repositories )
    {
        String path = getPathForLocalArtifact( artifact );
        File file = new File( getRepository().getBasedir(), path );
        addRepo( file, repositories );
    }

    private Properties readRepos( File artifactFile )
    {
        File trackingFile = getTrackingFile( artifactFile );

        Properties props = trackingFileManager.read( trackingFile );
        return ( props != null ) ? props : new Properties();
    }

    private void addRepo( File artifactFile, Collection<String> repositories )
    {
        Map<String, String> updates = new HashMap<String, String>();
        for ( String repository : repositories )
        {
            updates.put( getKey( artifactFile, repository ), "" );
        }

        File trackingFile = getTrackingFile( artifactFile );

        trackingFileManager.update( trackingFile, updates );
    }

    private File getTrackingFile( File artifactFile )
    {
        return new File( artifactFile.getParentFile(), "_maven.repositories" );
    }

    private String getKey( File file, String repository )
    {
        return file.getName() + '>' + repository;
    }

    private boolean isTracked( Properties props, File file )
    {
        if ( props != null )
        {
            String keyPrefix = file.getName() + '>';
            for ( Object key : props.keySet() )
            {
                if ( key.toString().startsWith( keyPrefix ) )
                {
                    return true;
                }
            }
        }
        return false;
    }

}
