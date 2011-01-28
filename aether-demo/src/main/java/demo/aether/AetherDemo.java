package demo.aether;

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

@SuppressWarnings( "unused" )
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
