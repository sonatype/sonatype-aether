package org.sonatype.aether.test.impl;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *******************************************************************************/

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.metadata.Metadata;
import org.sonatype.aether.repository.LocalArtifactRegistration;
import org.sonatype.aether.repository.LocalArtifactRequest;
import org.sonatype.aether.repository.LocalArtifactResult;
import org.sonatype.aether.repository.LocalRepository;
import org.sonatype.aether.repository.LocalRepositoryManager;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.test.util.TestFileUtils;

public class TestLocalRepositoryManager
    implements LocalRepositoryManager
{

    private LocalRepository localRepository;

    private Set<Artifact> registration = new HashSet<Artifact>();

    public TestLocalRepositoryManager()
        throws IOException
    {
        super();
        localRepository = new LocalRepository( TestFileUtils.createTempDir( "test-local-repository" ) );
    }

    public LocalRepository getRepository()
    {
        return localRepository;
    }

    public String getPathForLocalArtifact( Artifact artifact )
    {
        String artifactId = artifact.getArtifactId();
        String groupId = artifact.getGroupId();
        String extension = artifact.getExtension();
        String version = artifact.getVersion();
        String classifier = artifact.getClassifier();

        String path =
            String.format( "%s/%s/%s/%s-%s-%s%s.%s", groupId, artifactId, version, groupId, artifactId, version,
                           classifier, extension );
        return path;
    }

    public String getPathForRemoteArtifact( Artifact artifact, RemoteRepository repository, String context )
    {
        return getPathForLocalArtifact( artifact );
    }

    public String getPathForLocalMetadata( Metadata metadata )
    {
        String artifactId = metadata.getArtifactId();
        String groupId = metadata.getGroupId();
        String version = metadata.getVersion();
        return String.format( "%s/%s/%s/%s-%s-%s.xml", groupId, artifactId, version, groupId, artifactId, version );
    }

    public String getPathForRemoteMetadata( Metadata metadata, RemoteRepository repository, String context )
    {
        return getPathForLocalMetadata( metadata );
    }

    public LocalArtifactResult find( RepositorySystemSession session, LocalArtifactRequest request )
    {
        Artifact artifact = request.getArtifact();

        LocalArtifactResult result = new LocalArtifactResult( request );

        result.setAvailable( registration.contains( artifact ) );
        return result;
    }

    public void add( RepositorySystemSession session, LocalArtifactRegistration request )
    {
        registration.add( request.getArtifact() );
    }

}
