package demo.aether;

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
import java.util.List;

import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.collection.DependencyCollectionException;
import org.sonatype.aether.deployment.DeploymentException;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.installation.InstallationException;
import org.sonatype.aether.resolution.ArtifactResolutionException;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.util.artifact.SubArtifact;

public class AetherDemo
{
    public void resolve() 
        throws DependencyCollectionException, ArtifactResolutionException
    {
        Aether aether = new Aether( "http://localhost:8081/nexus/content/groups/public", "/Users/jvanzyl/aether-repo" );
                
        AetherResult result = aether.resolve( "com.mycompany.app", "super-app", "1.0" );

        // Get the root of the resolved tree of artifacts
        //
        DependencyNode root = result.getRoot();

        // Get the list of files for the artifacts resolved
        //
        List<File> artifacts = result.getResolvedFiles();
        
        // Get the classpath of the artifacts resolved
        //
        String classpath = result.getResolvedClassPath();        
    }
    
    public void installAndDeploy() 
        throws InstallationException, DeploymentException
    {
        Aether aether = new Aether( "http://localhost:8081/nexus/content/groups/public", "/Users/jvanzyl/aether-repo" );
        
        Artifact artifact = new DefaultArtifact( "com.mycompany.super", "super-core", "jar", "0.1-SNAPSHOT" );
        artifact = artifact.setFile( new File( "jar-from-whatever-process.jar" ) );
        Artifact pom = new SubArtifact( artifact, null, "pom" );
        pom = pom.setFile( new File( "pom-from-whatever-process.xml" ) );
          
        // Install into the local repository specified
        //
        aether.install( artifact, pom );
        
        // Deploy to a remote reposistory
        //
        aether.deploy( artifact, pom, "http://localhost:8081/nexus/content/repositories/snapshots/" );
    }
}
