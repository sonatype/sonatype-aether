package org.sonatype.aether.test.impl;

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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.sonatype.aether.Artifact;
import org.sonatype.aether.LocalArtifactRequest;
import org.sonatype.aether.LocalArtifactResult;
import org.sonatype.aether.LocalRepository;
import org.sonatype.aether.LocalRepositoryManager;
import org.sonatype.aether.Metadata;
import org.sonatype.aether.RemoteRepository;

public class TestLocalRepositoryManager
    implements LocalRepositoryManager
{

    private LocalRepository localRepository = new LocalRepository( "target/test-local-repository" );
    
    private Set<Artifact> registration = new HashSet<Artifact>();

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
        
        String path = String.format("%s/%s/%s/%s-%s-%s%s.%s", groupId, artifactId, version, groupId, artifactId, version, classifier, extension);
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
        return String.format("%s/%s/%s/%s-%s-%s.pom", groupId, artifactId, version, groupId, artifactId, version);
    }

    public String getPathForRemoteMetadata( Metadata metadata, RemoteRepository repository, String context )
    {
        return getPathForLocalMetadata( metadata );
    }

    public LocalArtifactResult find( LocalArtifactRequest request )
    {
        Artifact artifact = request.getArtifact();
        
        LocalArtifactResult result = new LocalArtifactResult( request );
        
        result.setAvailable( registration.contains(artifact) );
        return result;
    }

    public void addLocalArtifact( Artifact artifact )
    {
        registration.add( artifact );
    }

    public void addRemoteArtifact( Artifact artifact, RemoteRepository repository, Collection<String> contexts )
    {
        registration.add( artifact );
    }

}
