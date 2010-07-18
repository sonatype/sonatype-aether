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
import java.util.Arrays;

import org.apache.maven.repository.internal.DefaultArtifactDescriptorReader;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.sonatype.aether.Artifact;
import org.sonatype.aether.AuthenticationSelector;
import org.sonatype.aether.CollectRequest;
import org.sonatype.aether.DefaultArtifact;
import org.sonatype.aether.Dependency;
import org.sonatype.aether.DependencyGraphTransformer;
import org.sonatype.aether.DependencyManager;
import org.sonatype.aether.DependencyNode;
import org.sonatype.aether.DependencySelector;
import org.sonatype.aether.DependencyTraverser;
import org.sonatype.aether.DeployRequest;
import org.sonatype.aether.InstallRequest;
import org.sonatype.aether.LocalRepositoryManager;
import org.sonatype.aether.MirrorSelector;
import org.sonatype.aether.ProxySelector;
import org.sonatype.aether.RemoteRepository;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.spi.connector.RepositoryConnectorFactory;
import org.sonatype.aether.connector.wagon.WagonProvider;
import org.sonatype.aether.connector.wagon.WagonRepositoryConnectorFactory;
import org.sonatype.aether.impl.ArtifactDescriptorReader;
import org.sonatype.aether.impl.internal.DefaultServiceLocator;
import org.sonatype.aether.impl.internal.EnhancedLocalRepositoryManager;
import org.sonatype.aether.util.AndDependencySelector;
import org.sonatype.aether.util.ChainedDependencyGraphTransformer;
import org.sonatype.aether.util.ClassicDependencyManager;
import org.sonatype.aether.util.ClassicVersionConflictResolver;
import org.sonatype.aether.util.ConflictMarker;
import org.sonatype.aether.util.DefaultArtifactType;
import org.sonatype.aether.util.DefaultArtifactTypeRegistry;
import org.sonatype.aether.util.DefaultAuthenticationSelector;
import org.sonatype.aether.util.DefaultMirrorSelector;
import org.sonatype.aether.util.DefaultProxySelector;
import org.sonatype.aether.util.DefaultRepositorySystemSession;
import org.sonatype.aether.util.ExclusionDependencySelector;
import org.sonatype.aether.util.FatArtifactTraverser;
import org.sonatype.aether.util.JavaDependencyContextRefiner;
import org.sonatype.aether.util.JavaEffectiveScopeCalculator;
import org.sonatype.aether.util.OptionalDependencySelector;
import org.sonatype.aether.util.ScopeDependencySelector;

public class RepoSys
{

    public static void main( String[] args )
        throws Exception
    {
        // RepositorySystem repoSystem = newManagedSystem();
        RepositorySystem repoSystem = newManualSystem();

        RepositorySystemSession session = newSession();

        CollectRequest collectRequest = new CollectRequest();
        collectRequest.setRoot( new Dependency( new DefaultArtifact( "org.apache.maven", "maven-core", "", "jar",
                                                                     "2.2.1" ), "compile" ) );
        collectRequest.setRepositories( Arrays.asList( new RemoteRepository( "central", "default",
                                                                             "http://repo1.maven.org/maven2/" ) ) );
        DependencyNode root = repoSystem.collectDependencies( session, collectRequest ).getRoot();

        repoSystem.resolveDependencies( session, root, null );

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

        System.out.println( "============================================================" );
        dump( root, "" );
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

        return locator.getService( RepositorySystem.class );
    }

    private static RepositorySystemSession newSession()
    {
        DefaultRepositorySystemSession session = new DefaultRepositorySystemSession();

        session.setUserAgent( "Aether" );

        LocalRepositoryManager localRepoMan = new EnhancedLocalRepositoryManager( new File( "target/local-repo" ) );
        session.setLocalRepositoryManager( localRepoMan );

        session.setTransferListener( new ConsoleTransferListener( System.out ) );
        session.setRepositoryListener( new ConsoleRepositoryListener( System.out ) );

        MirrorSelector mirrorSelector = new DefaultMirrorSelector();
        session.setMirrorSelector( mirrorSelector );

        ProxySelector proxySelector = new DefaultProxySelector();
        session.setProxySelector( proxySelector );

        AuthenticationSelector authSelector = new DefaultAuthenticationSelector();
        session.setAuthenticationSelector( authSelector );

        DependencyTraverser depTraverser = new FatArtifactTraverser();
        session.setDependencyTraverser( depTraverser );

        DependencyManager depManager = new ClassicDependencyManager();
        session.setDependencyManager( depManager );

        DependencySelector depFilter =
            new AndDependencySelector( new ScopeDependencySelector( "test", "provided" ),
                                       new OptionalDependencySelector(), new ExclusionDependencySelector() );
        session.setDependencySelector( depFilter );

        DependencyGraphTransformer transformer =
            new ChainedDependencyGraphTransformer( new ConflictMarker(), new JavaEffectiveScopeCalculator(),
                                                   new ClassicVersionConflictResolver(),
                                                   new JavaDependencyContextRefiner() );
        transformer = null;
        session.setDependencyGraphTransformer( transformer );

        session.setIgnoreInvalidArtifactDescriptor( true );
        session.setIgnoreMissingArtifactDescriptor( true );

        session.setSystemProps( System.getProperties() );

        DefaultArtifactTypeRegistry stereotypes = new DefaultArtifactTypeRegistry();
        stereotypes.add( new DefaultArtifactType( "pom" ) );
        stereotypes.add( new DefaultArtifactType( "maven-plugin", "jar", "", "java" ) );
        stereotypes.add( new DefaultArtifactType( "jar", "jar", "", "java" ) );
        stereotypes.add( new DefaultArtifactType( "ejb", "jar", "", "java" ) );
        stereotypes.add( new DefaultArtifactType( "ejb-client", "jar", "client", "java" ) );
        stereotypes.add( new DefaultArtifactType( "test-jar", "jar", "tests", "java" ) );
        stereotypes.add( new DefaultArtifactType( "javadoc", "jar", "javadoc", "java" ) );
        stereotypes.add( new DefaultArtifactType( "java-source", "jar", "sources", "java", false, false ) );
        stereotypes.add( new DefaultArtifactType( "war", "war", "", "java", false, true ) );
        stereotypes.add( new DefaultArtifactType( "ear", "ear", "", "java", false, true ) );
        stereotypes.add( new DefaultArtifactType( "rar", "rar", "", "java", false, true ) );
        stereotypes.add( new DefaultArtifactType( "par", "par", "", "java", false, true ) );
        session.setArtifactTypeRegistry( stereotypes );

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
