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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;

import org.apache.maven.artifact.repository.metadata.Snapshot;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Reader;
import org.apache.maven.repository.Artifact;
import org.apache.maven.repository.ArtifactRepository;
import org.apache.maven.repository.LocalRepositoryManager;
import org.apache.maven.repository.Metadata;
import org.apache.maven.repository.MetadataDownload;
import org.apache.maven.repository.MetadataNotFoundException;
import org.apache.maven.repository.MetadataTransferException;
import org.apache.maven.repository.RemoteRepository;
import org.apache.maven.repository.RepositoryContext;
import org.apache.maven.repository.RepositoryPolicy;
import org.apache.maven.repository.RepositoryReader;
import org.apache.maven.repository.NoRepositoryReaderException;
import org.apache.maven.repository.RepositoryReaderFactory;
import org.apache.maven.repository.ResolveRequest;
import org.apache.maven.repository.ResolveResult;
import org.apache.maven.repository.UpdateCheck;
import org.apache.maven.repository.UpdateCheckManager;
import org.apache.maven.repository.VersionRequest;
import org.apache.maven.repository.VersionResolutionException;
import org.apache.maven.repository.VersionResult;
import org.apache.maven.repository.WorkspaceReader;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;

/**
 * @author Benjamin Bentmann
 * @plexus.component role="org.apache.maven.repository.RepositorySystem" role-hint="default"
 */
public class DefaultRepositorySystem
{

    private File getFile( RepositoryContext context, Metadata metadata, RemoteRepository repository )
    {
        LocalRepositoryManager lrm = context.getComponentRegistry().getLocalRepositoryManager();
        String path;
        if ( repository != null )
        {
            path = lrm.getPathForRemoteMetadata( metadata, repository );
        }
        else
        {
            path = lrm.getPathForLocalMetadata( metadata );
        }
        return new File( context.getLocalRepository().getBasedir(), path );
    }

    private RepositoryPolicy getPolicy( RepositoryContext context, RemoteRepository repository, boolean releases,
                                        boolean snapshots )
    {
        RepositoryPolicy policy;

        // get effective per-repository policy
        if ( releases && snapshots )
        {
            RepositoryPolicy policy1 = repository.getPolicy( false );
            RepositoryPolicy policy2 = repository.getPolicy( true );
            policy = new RepositoryPolicy( policy1 );
            if ( policy2.isEnabled() )
            {
                policy.setEnabled( true );

                if ( ordinalOfChecksumPolicy( policy2.getChecksumPolicy() ) < ordinalOfChecksumPolicy( policy.getChecksumPolicy() ) )
                {
                    policy.setChecksumPolicy( policy2.getChecksumPolicy() );
                }

                UpdateCheckManager ucm = context.getComponentRegistry().getUpdateCheckManager();
                policy.setChecksumPolicy( ucm.getEffectiveUpdatePolicy( context, policy.getChecksumPolicy(),
                                                                        policy2.getChecksumPolicy() ) );
            }
        }
        else
        {
            policy = new RepositoryPolicy( repository.getPolicy( snapshots ) );
        }

        // superimpose global policy
        if ( StringUtils.isNotEmpty( context.getChecksumPolicy() ) )
        {
            policy.setChecksumPolicy( context.getChecksumPolicy() );
        }
        if ( StringUtils.isNotEmpty( context.getUpdatePolicy() ) )
        {
            policy.setUpdatePolicy( context.getUpdatePolicy() );
        }

        return policy;
    }

    private int ordinalOfChecksumPolicy( String policy )
    {
        if ( RepositoryPolicy.CHECKSUM_POLICY_FAIL.equals( policy ) )
        {
            return 2;
        }
        else if ( RepositoryPolicy.CHECKSUM_POLICY_IGNORE.equals( policy ) )
        {
            return 0;
        }
        else
        {
            return 1;
        }
    }

    private boolean isSnapshot( Artifact artifact )
    {
        String version = artifact.getVersion();
        return version.endsWith( "SNAPSHOT" ) || version.matches( "^(.*)-([0-9]{8}.[0-9]{6})-([0-9]+)$" );
    }

    public VersionResult resolveVersion( VersionRequest request )
        throws VersionResolutionException
    {
        String version = request.getArtifact().getVersion();
        WorkspaceReader workspace = request.getContext().getComponentRegistry().getWorkspaceReader();

        VersionResult result = new VersionResult();

        Metadata metadata;
        boolean releases = false;
        boolean snapshots = false;

        if ( workspace != null && workspace.findVersions( request.getArtifact() ).contains( version ) )
        {
            metadata = null;
            result.setRepository( request.getContext().getWorkspaceRepository() );
        }
        else if ( "RELEASE".equals( version ) || "LATEST".equals( version ) )
        {
            metadata = new Metadata();
            metadata.setGroupId( request.getArtifact().getGroupId() );
            metadata.setArtifactId( request.getArtifact().getArtifactId() );
            metadata.setType( "maven-metadata.xml" );
            releases = true;
            snapshots = "LATEST".equals( version );
        }
        else if ( version.endsWith( "SNAPSHOT" ) )
        {
            metadata = new Metadata();
            metadata.setGroupId( request.getArtifact().getGroupId() );
            metadata.setArtifactId( request.getArtifact().getArtifactId() );
            metadata.setVersion( version );
            metadata.setType( "maven-metadata.xml" );
            snapshots = true;
        }
        else
        {
            metadata = null;
        }

        if ( metadata == null )
        {
            result.setVersion( version );
        }
        else
        {
            RepositoryContext context = request.getContext();

            if ( !context.isOffline() )
            {
                UpdateCheckManager ucm = context.getComponentRegistry().getUpdateCheckManager();

                long localLastUpdated = getFile( context, metadata, null ).lastModified();

                for ( RemoteRepository repository : request.getRemoteRepositories() )
                {
                    RepositoryPolicy policy = getPolicy( context, repository, releases, snapshots );

                    if ( !policy.isEnabled() )
                    {
                        continue;
                    }

                    File metadataFile = getFile( context, metadata, repository );

                    MetadataDownload download =
                        new MetadataDownload( metadata, metadataFile, policy.getChecksumPolicy() );

                    UpdateCheck<Metadata, MetadataTransferException> check =
                        new UpdateCheck<Metadata, MetadataTransferException>();
                    check.setContext( context );
                    check.setLocalLastUpdated( localLastUpdated );
                    check.setItem( metadata );
                    check.setFile( metadataFile );
                    check.setRepository( repository );
                    check.setPolicy( policy.getUpdatePolicy() );
                    ucm.checkMetadata( check );

                    if ( check.isRequired() )
                    {
                        try
                        {
                            RepositoryReader reader = getRepositoryReader( repository, context );
                            try
                            {
                                reader.getMetadata( Arrays.asList( download ) );
                            }
                            finally
                            {
                                reader.close();
                            }
                        }
                        catch ( NoRepositoryReaderException e )
                        {
                            download.setException( new MetadataTransferException( metadata, e ) );
                        }
                        ucm.touchMetadata( check.setException( download.getException() ) );
                    }
                    else
                    {
                        download.setException( check.getException() );
                    }

                    if ( download.getException() instanceof MetadataNotFoundException )
                    {
                        metadataFile.delete();
                    }
                    else if ( download.getException() != null )
                    {
                        // TODO: warn about cached error
                    }
                }
            }

            Versioning versioning = readVersions( context, metadata, null );
            ArtifactRepository repo = context.getLocalRepository();

            for ( RemoteRepository repository : request.getRemoteRepositories() )
            {
                RepositoryPolicy policy = getPolicy( context, repository, releases, snapshots );

                if ( !policy.isEnabled() )
                {
                    continue;
                }

                Versioning v = readVersions( context, metadata, repository );
                if ( mergeVersions( versioning, v ) )
                {
                    repo = repository;
                }
            }

            if ( "RELEASE".equals( version ) )
            {
                result.setVersion( versioning.getRelease() );
            }
            else if ( "LATEST".equals( version ) )
            {
                result.setVersion( versioning.getLatest() );
                if ( StringUtils.isEmpty( versioning.getLatest() ) )
                {
                    result.setVersion( versioning.getRelease() );
                }
            }
            else
            {
                Snapshot snapshot = versioning.getSnapshot();
                if ( snapshot != null )
                {
                    if ( snapshot.getTimestamp() != null && snapshot.getBuildNumber() > 0 )
                    {
                        String qualifier = snapshot.getTimestamp() + "-" + snapshot.getBuildNumber();
                        result.setVersion( version.substring( 0, version.length() - "SNAPSHOT".length() ) + qualifier );
                    }
                    else
                    {
                        result.setVersion( version );
                    }
                }
            }

            if ( StringUtils.isNotEmpty( result.getVersion() ) )
            {
                result.setRepository( repo );
            }
            else
            {
                throw new VersionResolutionException( request.getArtifact() );
            }
        }

        return result;
    }

    private Versioning readVersions( RepositoryContext context, Metadata metadata, RemoteRepository repository )
    {
        Versioning versioning = null;

        File metadataFile = getFile( context, metadata, repository );

        FileInputStream fis = null;
        try
        {
            fis = new FileInputStream( metadataFile );
            org.apache.maven.artifact.repository.metadata.Metadata m = new MetadataXpp3Reader().read( fis );
            versioning = m.getVersioning();
        }
        catch ( FileNotFoundException e )
        {
            return null;
        }
        catch ( Exception e )
        {
            // TODO: notify somebody
        }
        finally
        {
            IOUtil.close( fis );
        }

        return ( versioning != null ) ? versioning : new Versioning();
    }

    private boolean mergeVersions( Versioning target, Versioning source )
    {
        String targetTimestamp = StringUtils.clean( target.getLastUpdated() );
        String sourceTimestamp = StringUtils.clean( source.getLastUpdated() );
        if ( targetTimestamp.compareTo( sourceTimestamp ) >= 0 )
        {
            return false;
        }

        if ( source.getRelease() != null )
        {
            target.setRelease( source.getRelease() );
        }
        if ( source.getLatest() != null )
        {
            target.setLatest( source.getLatest() );
        }
        if ( source.getSnapshot() != null )
        {
            target.setSnapshot( source.getSnapshot() );
        }

        return true;
    }

    public ResolveResult resolveArtifacts( ResolveRequest request )
    {
        return null;
    }

    private RepositoryReader getRepositoryReader( RemoteRepository repository, RepositoryContext context )
        throws NoRepositoryReaderException
    {
        List<? extends RepositoryReaderFactory> factories = context.getComponentRegistry().getReaderFactories();

        for ( RepositoryReaderFactory factory : factories )
        {
            try
            {
                return factory.newInstance( context, repository );
            }
            catch ( NoRepositoryReaderException e )
            {
                // continue and try next factory
            }
        }

        throw new NoRepositoryReaderException( repository );
    }

}
