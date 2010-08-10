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

import org.apache.maven.repository.internal.DefaultArtifactDescriptorReader;
import org.apache.maven.repository.internal.DefaultVersionRangeResolver;
import org.apache.maven.repository.internal.DefaultVersionResolver;
import org.apache.maven.repository.internal.SnapshotMetadataGeneratorFactory;
import org.apache.maven.repository.internal.VersionsMetadataGeneratorFactory;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.sonatype.aether.Artifact;
import org.sonatype.aether.ArtifactResolutionException;
import org.sonatype.aether.CollectRequest;
import org.sonatype.aether.DefaultArtifact;
import org.sonatype.aether.Dependency;
import org.sonatype.aether.DependencyCollectionException;
import org.sonatype.aether.DependencyNode;
import org.sonatype.aether.DeployRequest;
import org.sonatype.aether.DeploymentException;
import org.sonatype.aether.InstallRequest;
import org.sonatype.aether.InstallationException;
import org.sonatype.aether.LocalRepository;
import org.sonatype.aether.RemoteRepository;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.spi.connector.RepositoryConnectorFactory;
import org.sonatype.aether.connector.wagon.WagonProvider;
import org.sonatype.aether.connector.wagon.WagonRepositoryConnectorFactory;
import org.sonatype.aether.impl.ArtifactDescriptorReader;
import org.sonatype.aether.impl.MetadataGeneratorFactory;
import org.sonatype.aether.impl.VersionRangeResolver;
import org.sonatype.aether.impl.VersionResolver;
import org.sonatype.aether.impl.internal.DefaultServiceLocator;
import org.sonatype.aether.util.DefaultRepositorySystemSession;
import org.sonatype.aether.util.graph.FileListGenerator;

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
        throws DependencyCollectionException, ArtifactResolutionException, InstallationException, DeploymentException
    {
        RepositorySystemSession session = newSession( repoSystem );

        Dependency dependency =
            new Dependency( new DefaultArtifact( "org.apache.maven:maven-profile:2.2.1" ), "compile" );
        RemoteRepository central = new RemoteRepository( "central", "default", "http://repo1.maven.org/maven2/" );

        CollectRequest collectRequest = new CollectRequest();
        collectRequest.setRoot( dependency );
        collectRequest.addRepository( central );
        DependencyNode node = repoSystem.collectDependencies( session, collectRequest ).getRoot();

        repoSystem.resolveDependencies( session, node, null );

        System.out.println( "------------------------------------------------------------" );
        System.out.println( "Resolution results" );
        dump( node, "" );

        System.out.println( FileListGenerator.getClassPath( node ) );

        System.out.println( "------------------------------------------------------------" );
        System.out.println( "Deployment into repository" );

        Artifact projectOutput = new DefaultArtifact( "test", "test", "", "jar", "0.1-SNAPSHOT" );
        projectOutput = projectOutput.setFile( new File( "pom.xml" ) );

        InstallRequest installRequest = new InstallRequest();
        installRequest.addArtifact( projectOutput );
        repoSystem.install( session, installRequest );

        DeployRequest deployRequest = new DeployRequest();
        deployRequest.addArtifact( projectOutput );
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
        locator.addService( ArtifactDescriptorReader.class, DefaultArtifactDescriptorReader.class );
        locator.addService( VersionResolver.class, DefaultVersionResolver.class );
        locator.addService( VersionRangeResolver.class, DefaultVersionRangeResolver.class );
        locator.addService( MetadataGeneratorFactory.class, SnapshotMetadataGeneratorFactory.class );
        locator.addService( MetadataGeneratorFactory.class, VersionsMetadataGeneratorFactory.class );

        return locator.getService( RepositorySystem.class );
    }

    private static RepositorySystemSession newSession( RepositorySystem system )
    {
        DefaultRepositorySystemSession session = DefaultRepositorySystemSession.newMavenRepositorySystemSession();

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
