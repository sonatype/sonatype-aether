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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.repository.Artifact;
import org.apache.maven.repository.InstallRequest;
import org.apache.maven.repository.InstallationException;
import org.apache.maven.repository.LocalRepositoryManager;
import org.apache.maven.repository.MergeableMetadata;
import org.apache.maven.repository.Metadata;
import org.apache.maven.repository.RepositoryException;
import org.apache.maven.repository.RepositorySession;
import org.apache.maven.repository.spi.Installer;
import org.apache.maven.repository.spi.Logger;
import org.apache.maven.repository.spi.NullLogger;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.FileUtils;

/**
 * @author Benjamin Bentmann
 */
@Component( role = Installer.class )
public class DefaultInstaller
    implements Installer
{

    @Requirement
    private Logger logger = NullLogger.INSTANCE;

    public DefaultInstaller setLogger( Logger logger )
    {
        this.logger = ( logger != null ) ? logger : NullLogger.INSTANCE;
        return this;
    }

    public void install( RepositorySession session, InstallRequest request )
        throws InstallationException
    {
        LocalRepositoryManager lrm = session.getLocalRepositoryManager();
        File basedir = lrm.getRepository().getBasedir();

        for ( Artifact artifact : request.getArtifacts() )
        {
            install( artifact, lrm, basedir );
        }

        for ( Metadata metadata : request.getMetadata() )
        {
            install( metadata, lrm, basedir );
        }
    }

    private void install( Artifact artifact, LocalRepositoryManager lrm, File basedir )
        throws InstallationException
    {
        File srcFile = artifact.getFile();
        File dstFile = new File( basedir, lrm.getPathForLocalArtifact( artifact ) );

        boolean copy =
            "pom".equals( artifact.getType() ) || !dstFile.exists() || srcFile.lastModified() != dstFile.lastModified()
                || srcFile.length() != dstFile.length();

        if ( copy )
        {
            if ( !dstFile.getParentFile().exists() )
            {
                dstFile.getParentFile().mkdirs();
            }

            try
            {
                FileUtils.copyFile( srcFile, dstFile );
            }
            catch ( IOException e )
            {
                throw new InstallationException( "Failed to install artifact " + artifact + ": " + e.getMessage(), e );
            }
            dstFile.setLastModified( srcFile.lastModified() );
        }
        else
        {
            logger.debug( "Skipped re-installing " + srcFile + " to " + dstFile + ", seems unchanged" );
        }

        List<MergeableMetadata> versionMetadata = generateVersionMetadata( artifact );
        for ( MergeableMetadata metadata : versionMetadata )
        {
            install( metadata, lrm, basedir );
        }
    }

    private void install( Metadata metadata, LocalRepositoryManager lrm, File basedir )
        throws InstallationException
    {
        File dstFile = new File( basedir, lrm.getPathForLocalMetadata( metadata ) );

        if ( metadata instanceof MergeableMetadata )
        {
            try
            {
                ( (MergeableMetadata) metadata ).merge( dstFile, dstFile );
            }
            catch ( RepositoryException e )
            {
                throw new InstallationException( "Failed to update metadata " + metadata + ": " + e.getMessage(), e );
            }
        }
        else
        {
            try
            {
                FileUtils.copyFile( metadata.getFile(), dstFile );
            }
            catch ( IOException e )
            {
                throw new InstallationException( "Failed to install metadata " + metadata + ": " + e.getMessage(), e );
            }
        }
    }

    private List<MergeableMetadata> generateVersionMetadata( Artifact artifact )
    {
        List<MergeableMetadata> result = new ArrayList<MergeableMetadata>();

        result.add( new VersionsMetadata( artifact ) );
        if ( artifact.isSnapshot() )
        {
            result.add( new LocalSnapshotMetadata( artifact ) );
        }

        return result;
    }

}
