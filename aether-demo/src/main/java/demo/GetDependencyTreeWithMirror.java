package demo;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.collection.CollectRequest;
import org.sonatype.aether.collection.CollectResult;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.util.repository.DefaultMirrorSelector;

import demo.util.Booter;
import demo.util.ConsoleDependencyGraphDumper;

/**
 * Collects the transitive dependencies of an artifact, and demonstrate
 * mirror resolution.
 */
public class GetDependencyTreeWithMirror
{

    public static void main( String[] args )
        throws Exception
    {
        System.out.println( "------------------------------------------------------------" );
        System.out.println( GetDependencyTreeWithMirror.class.getSimpleName() );

        RepositorySystem system = Booter.newRepositorySystem();

        RepositorySystemSession session = Booter.newRepositorySystemSession( system );

        Artifact artifact = new DefaultArtifact( "org.apache.maven:maven-aether-provider:3.0.2" );

        RemoteRepository central = new RemoteRepository( "central", "default", "http://repo1.maven.org/maven2/" );
        DefaultMirrorSelector dms = (DefaultMirrorSelector) session.getMirrorSelector();
        dms.add( "mirror", "http://repo1.maven.org/maven2/", "default", true, "central", "*" );

        CollectRequest collectRequest = new CollectRequest();
        collectRequest.setRoot( new Dependency( artifact, "" ) );
        collectRequest.addRepository( central );

        CollectResult collectResult = system.collectDependencies( session, collectRequest );

        collectResult.getRoot().accept( new ConsoleDependencyGraphDumper() );
    }

}
