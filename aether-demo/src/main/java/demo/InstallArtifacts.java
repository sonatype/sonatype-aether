package demo;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import java.io.File;

import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.installation.InstallRequest;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.util.artifact.SubArtifact;

import demo.util.Booter;

/**
 * Installs a JAR and its POM to the local repository.
 */
public class InstallArtifacts
{

    public static void main( String[] args )
        throws Exception
    {
        System.out.println( "------------------------------------------------------------" );
        System.out.println( InstallArtifacts.class.getSimpleName() );

        RepositorySystem system = Booter.newRepositorySystem();

        RepositorySystemSession session = Booter.newRepositorySystemSession( system );

        Artifact jarArtifact = new DefaultArtifact( "test", "demo", "", "jar", "0.1-SNAPSHOT" );
        jarArtifact = jarArtifact.setFile( new File( "demo.jar" ) );

        Artifact pomArtifact = new SubArtifact( jarArtifact, "", "pom" );
        pomArtifact = pomArtifact.setFile( new File( "pom.xml" ) );

        InstallRequest installRequest = new InstallRequest();
        installRequest.addArtifact( jarArtifact ).addArtifact( pomArtifact );

        system.install( session, installRequest );
    }

}
