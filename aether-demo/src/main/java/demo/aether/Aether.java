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

import org.apache.maven.repository.internal.DefaultServiceLocator;
import org.apache.maven.repository.internal.MavenRepositorySystemSession;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.collection.CollectRequest;
import org.sonatype.aether.collection.DependencyCollectionException;
import org.sonatype.aether.connector.wagon.WagonProvider;
import org.sonatype.aether.connector.wagon.WagonRepositoryConnectorFactory;
import org.sonatype.aether.deployment.DeployRequest;
import org.sonatype.aether.deployment.DeploymentException;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.installation.InstallRequest;
import org.sonatype.aether.installation.InstallationException;
import org.sonatype.aether.repository.Authentication;
import org.sonatype.aether.repository.LocalRepository;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.ArtifactResolutionException;
import org.sonatype.aether.spi.connector.RepositoryConnectorFactory;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.util.graph.PreorderNodeListGenerator;

import demo.ConsoleRepositoryListener;
import demo.ConsoleTransferListener;
import demo.ManualWagonProvider;

public class Aether
{
    private String remoteRepository;
    private RepositorySystem repositorySystem;
    private LocalRepository localRepository;

    public Aether( String remoteRepository, String localRepository )
    {
        this.remoteRepository = remoteRepository;
        this.repositorySystem = newManualSystem();
        this.localRepository = new LocalRepository( localRepository );
    }

    //
    // Setting up the repository system with the mechanism to find components
    // and setting up the implementations to use. This would be much easier
    // using Guice, but we want Aether to be easily embedded.
    //
    private RepositorySystem newManualSystem()
    {
        DefaultServiceLocator locator = new DefaultServiceLocator();
        locator.setServices( WagonProvider.class, new ManualWagonProvider() );
        locator.addService( RepositoryConnectorFactory.class, WagonRepositoryConnectorFactory.class );
        return locator.getService( RepositorySystem.class );
    }

    private RepositorySystemSession newSession()
    {
        MavenRepositorySystemSession session = new MavenRepositorySystemSession();
        session.setLocalRepositoryManager( repositorySystem.newLocalRepositoryManager( localRepository ) );
        session.setTransferListener( new ConsoleTransferListener( System.out ) );
        session.setRepositoryListener( new ConsoleRepositoryListener( System.out ) );
        return session;
    }
    
    public AetherResult resolve( String groupId, String artifactId, String version )
        throws DependencyCollectionException, ArtifactResolutionException
    {
        RepositorySystemSession session = newSession();
        Dependency dependency = new Dependency( new DefaultArtifact( groupId, artifactId, "", "jar", version ), "runtime" );
        RemoteRepository central = new RemoteRepository( "central", "default", remoteRepository );

        CollectRequest collectRequest = new CollectRequest();
        collectRequest.setRoot( dependency );
        collectRequest.addRepository( central );

        DependencyNode rootNode = repositorySystem.collectDependencies( session, collectRequest ).getRoot();

        repositorySystem.resolveDependencies( session, rootNode, null );

        StringBuffer dump = new StringBuffer();
        dump( rootNode, "", dump );

        PreorderNodeListGenerator nlg = new PreorderNodeListGenerator();
        rootNode.accept( nlg );

        return new AetherResult( dump.toString(), nlg.getFiles(), nlg.getClassPath() );
    }

    public void install( Artifact artifact, Artifact pom )
        throws InstallationException
    {
        RepositorySystemSession session = newSession();

        InstallRequest installRequest = new InstallRequest();
        installRequest.addArtifact( artifact ).addArtifact( pom );
        
        repositorySystem.install( session, installRequest );
    }

    public void deploy( Artifact artifact, Artifact pom, String remoteRepository )
        throws DeploymentException
    {
        RepositorySystemSession session = newSession();

        RemoteRepository nexus = new RemoteRepository( "nexus", "default", remoteRepository );
        Authentication authentication = new Authentication( "admin", "admin123" );
        nexus.setAuthentication( authentication );

        DeployRequest deployRequest = new DeployRequest();
        deployRequest.addArtifact( artifact ).addArtifact( pom );        
        deployRequest.setRepository( nexus );
        
        repositorySystem.deploy( session, deployRequest );
    }

    private void dump( DependencyNode node, String indent, StringBuffer sb )
    {
        sb.append( indent + node.getDependency() ).append( "\n" );
        indent += "  ";
        for ( DependencyNode child : node.getChildren() )
        {
            dump( child, indent, sb );
        }
    }

}
