package demo;

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

import org.apache.maven.repository.internal.DefaultServiceLocator;
import org.apache.maven.repository.internal.MavenRepositorySystemSession;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.sonatype.aether.RepositoryException;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.repository.LocalRepository;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.VersionRangeRequest;
import org.sonatype.aether.resolution.VersionRangeResult;
import org.sonatype.aether.spi.connector.RepositoryConnectorFactory;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.collection.CollectRequest;
import org.sonatype.aether.connector.wagon.WagonProvider;
import org.sonatype.aether.connector.wagon.WagonRepositoryConnectorFactory;
import org.sonatype.aether.deployment.DeployRequest;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.installation.InstallRequest;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.util.artifact.SubArtifact;
import org.sonatype.aether.util.graph.PreorderNodeListGenerator;
import org.sonatype.aether.version.Version;

public class RepoSys
{

    public static void main( String[] args )
        throws Exception
    {
        RepositorySystem repoSystem;

        System.out.println( "============================================================" );
        System.out.println( "IoC managed/wired demo" );
        System.out.println( "============================================================" );

        // System managed by IoC container (Plexus in this case)
        repoSystem = newManagedSystem();
        playTheDanceBaby( repoSystem );

        System.out.println();
        System.out.println( "============================================================" );
        System.out.println( "Manually managed/wired demo" );
        System.out.println( "============================================================" );

        // System manually managed, no IoC container (manually wired up)
        repoSystem = newManualSystem();
        playTheDanceBaby( repoSystem );
    }

    private static void playTheDanceBaby( RepositorySystem repoSystem )
        throws RepositoryException
    {
        RepositorySystemSession session = newSession( repoSystem );

        Dependency dependency =
            new Dependency( new DefaultArtifact( "org.apache.maven:maven-aether-provider:3.0" ), "runtime" );
        RemoteRepository central = new RemoteRepository( "central", "default", "http://repo1.maven.org/maven2/" );

        /*
         * Resolve a version range, thereby finding all available/matchin versions
         */

        VersionRangeRequest rangeRequest = new VersionRangeRequest();
        rangeRequest.setArtifact( new DefaultArtifact( "org.apache.maven:maven-model:[0,)" ) );
        rangeRequest.addRepository( central );

        VersionRangeResult rangeResult = repoSystem.resolveVersionRange( session, rangeRequest );

        System.out.println( "------------------------------------------------------------" );
        System.out.println( "Version results" );
        for ( Version version : rangeResult.getVersions() )
        {
            System.out.println( version );
        }

        /*
         * Collect and resolve all transitive dependencies
         */

        CollectRequest collectRequest = new CollectRequest();
        collectRequest.setRoot( dependency );
        collectRequest.addRepository( central );
        DependencyNode node = repoSystem.collectDependencies( session, collectRequest ).getRoot();

        repoSystem.resolveDependencies( session, node, null );

        System.out.println( "------------------------------------------------------------" );
        System.out.println( "Resolution results" );
        dump( node, "" );

        PreorderNodeListGenerator nlg = new PreorderNodeListGenerator();
        node.accept( nlg );
        System.out.println( nlg.getFiles() );
        System.out.println( nlg.getClassPath() );

        System.out.println( "------------------------------------------------------------" );
        System.out.println( "Deployment into repository" );

        Artifact projectOutput = new DefaultArtifact( "test", "demo", "", "jar", "0.1-SNAPSHOT" );
        projectOutput = projectOutput.setFile( new File( "demo.jar" ) );
        Artifact projectPom = new SubArtifact( projectOutput, "", "pom" );
        projectPom = projectPom.setFile( new File( "pom.xml" ) );

        /*
         * Install artifacts to the local repository
         */

        InstallRequest installRequest = new InstallRequest();
        installRequest.addArtifact( projectOutput ).addArtifact( projectPom );
        repoSystem.install( session, installRequest );

        /*
         * Deploy artifacts to a remote repository
         */

        DeployRequest deployRequest = new DeployRequest();
        deployRequest.addArtifact( projectOutput ).addArtifact( projectPom );
        deployRequest.setRepository( new RemoteRepository( "nexus", "default",
            new File( "target/dist-repo" ).toURI().toString() ) );
        repoSystem.deploy( session, deployRequest );
    }

    private static RepositorySystem newManagedSystem()
        throws Exception
    {
        return new DefaultPlexusContainer().lookup( RepositorySystem.class );
    }

    private static RepositorySystem newManualSystem()
    {
        DefaultServiceLocator locator = new DefaultServiceLocator();
        locator.setServices( WagonProvider.class, new ManualWagonProvider() );
        locator.addService( RepositoryConnectorFactory.class, WagonRepositoryConnectorFactory.class );

        return locator.getService( RepositorySystem.class );
    }

    private static RepositorySystemSession newSession( RepositorySystem system )
    {
        MavenRepositorySystemSession session = new MavenRepositorySystemSession();

        LocalRepository localRepo = new LocalRepository( "target/local-repo" );
        session.setLocalRepositoryManager( system.newLocalRepositoryManager( localRepo ) );

        session.setTransferListener( new ConsoleTransferListener( System.out ) );
        session.setRepositoryListener( new ConsoleRepositoryListener( System.out ) );

        // uncomment to generate dirty trees
        // session.setDependencyGraphTransformer( null );

        return session;
    }

    private static void dump( DependencyNode node, String indent )
    {
        System.out.println( indent + node.getDependency() );
        indent += "  ";
        for ( DependencyNode child : node.getChildren() )
        {
            dump( child, indent );
        }
    }

}
