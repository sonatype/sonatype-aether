import java.io.File;
import java.util.Arrays;

import org.apache.maven.repository.Artifact;
import org.apache.maven.repository.AuthenticationSelector;
import org.apache.maven.repository.CollectRequest;
import org.apache.maven.repository.DefaultArtifact;
import org.apache.maven.repository.DefaultRepositorySession;
import org.apache.maven.repository.Dependency;
import org.apache.maven.repository.DependencyFilter;
import org.apache.maven.repository.DependencyGraphTransformer;
import org.apache.maven.repository.DependencyManager;
import org.apache.maven.repository.DependencyNode;
import org.apache.maven.repository.DependencyTraverser;
import org.apache.maven.repository.DeployRequest;
import org.apache.maven.repository.LocalRepositoryManager;
import org.apache.maven.repository.MirrorSelector;
import org.apache.maven.repository.ProxySelector;
import org.apache.maven.repository.RemoteRepository;
import org.apache.maven.repository.RepositorySession;
import org.apache.maven.repository.RepositorySystem;
import org.apache.maven.repository.internal.TrackingLocalRepositoryManager;
import org.apache.maven.repository.spi.NullLogger;
import org.apache.maven.repository.util.AndDependencyFilter;
import org.apache.maven.repository.util.ChainedDependencyGraphTransformer;
import org.apache.maven.repository.util.ClassicVersionConflictResolver;
import org.apache.maven.repository.util.ConflictMarker;
import org.apache.maven.repository.util.DefaultArtifactStereotype;
import org.apache.maven.repository.util.DefaultArtifactStereotypeManager;
import org.apache.maven.repository.util.DefaultAuthenticationSelector;
import org.apache.maven.repository.util.DefaultDependencyManager;
import org.apache.maven.repository.util.DefaultMirrorSelector;
import org.apache.maven.repository.util.DefaultProxySelector;
import org.apache.maven.repository.util.ExclusionDependencyFilter;
import org.apache.maven.repository.util.FatArtifactTraverser;
import org.apache.maven.repository.util.JavaDependencyContextRefiner;
import org.apache.maven.repository.util.JavaEffectiveScopeCalculator;
import org.apache.maven.repository.util.OptionalDependencyFilter;
import org.apache.maven.repository.util.ScopeDependencyFilter;
import org.codehaus.plexus.DefaultPlexusContainer;

public class RepoSys
{

    public static void main( String[] args )
        throws Exception
    {
        DefaultPlexusContainer container = new DefaultPlexusContainer();

        RepositorySystem repoSystem = container.lookup( RepositorySystem.class );

        RepositorySession session = newSession();

        CollectRequest collectRequest = new CollectRequest();
        collectRequest.setRoot( new Dependency( new DefaultArtifact( "org.apache.maven", "maven-core", "", "jar",
                                                                     "2.2.1" ), "compile" ) );
        collectRequest.setRepositories( Arrays.asList( new RemoteRepository( "central", "default",
                                                                             "http://repo1.maven.org/maven2/" ) ) );
        DependencyNode root = repoSystem.collectDependencies( session, collectRequest ).getRoot();

        dump( root, "" );

        repoSystem.resolveDependencies( session, root );
        
        Artifact projectOutput = new DefaultArtifact( "test", "test", "", "jar", "0.1-SNAPSHOT" );
        projectOutput.setFile( new File("/Users/bentmann/tmp/z/pom.xml") );
        DeployRequest deployRequest = new DeployRequest();
        deployRequest.addArtifact( projectOutput );
        deployRequest.setRepository( new RemoteRepository( "nexus", "default", "file:///Users/bentmann/tmp/dist-repo/" ) );
        repoSystem.deploy( session, deployRequest );
    }

    private static RepositorySession newSession()
    {
        DefaultRepositorySession session = new DefaultRepositorySession();

        LocalRepositoryManager localRepoMan =
            new TrackingLocalRepositoryManager( new File( "target/local-repo" ), NullLogger.INSTANCE );
        session.setLocalRepositoryManager( localRepoMan );

        session.setTransferListener( new ConsoleTransferListener( System.out ) );

        MirrorSelector mirrorSelector = new DefaultMirrorSelector();
        session.setMirrorSelector( mirrorSelector );

        ProxySelector proxySelector = new DefaultProxySelector();
        session.setProxySelector( proxySelector );

        AuthenticationSelector authSelector = new DefaultAuthenticationSelector();
        session.setAuthenticationSelector( authSelector );

        DependencyTraverser depTraverser = new FatArtifactTraverser();
        session.setDependencyTraverser( depTraverser );

        DependencyManager depManager = new DefaultDependencyManager();
        session.setDependencyManager( depManager );

        DependencyFilter depFilter =
            new AndDependencyFilter( new ScopeDependencyFilter( "test", "provided" ), new OptionalDependencyFilter(),
                                     new ExclusionDependencyFilter() );
        session.setDependencyFilter( depFilter );

        DependencyGraphTransformer transformer =
            new ChainedDependencyGraphTransformer( new ConflictMarker(), new JavaEffectiveScopeCalculator(),
                                                   new ClassicVersionConflictResolver(),
                                                   new JavaDependencyContextRefiner() );
        session.setDependencyGraphTransformer( transformer );

        session.setIgnoreInvalidArtifactDescriptor( true );
        session.setIgnoreMissingArtifactDescriptor( true );

        session.setSystemProperties( System.getProperties() );

        DefaultArtifactStereotypeManager stereotypes = new DefaultArtifactStereotypeManager();
        stereotypes.addStereotype( new DefaultArtifactStereotype( "pom" ) );
        stereotypes.addStereotype( new DefaultArtifactStereotype( "maven-plugin", "jar", "", "java" ) );
        stereotypes.addStereotype( new DefaultArtifactStereotype( "jar", "jar", "", "java" ) );
        stereotypes.addStereotype( new DefaultArtifactStereotype( "ejb", "jar", "", "java" ) );
        stereotypes.addStereotype( new DefaultArtifactStereotype( "ejb-client", "jar", "client", "java" ) );
        stereotypes.addStereotype( new DefaultArtifactStereotype( "test-jar", "jar", "tests", "java" ) );
        stereotypes.addStereotype( new DefaultArtifactStereotype( "javadoc", "jar", "javadoc", "java" ) );
        stereotypes.addStereotype( new DefaultArtifactStereotype( "java-source", "jar", "sources", "java", false, false ) );
        stereotypes.addStereotype( new DefaultArtifactStereotype( "war", "war", "", "java", false, true ) );
        stereotypes.addStereotype( new DefaultArtifactStereotype( "ear", "ear", "", "java", false, true ) );
        stereotypes.addStereotype( new DefaultArtifactStereotype( "rar", "rar", "", "java", false, true ) );
        stereotypes.addStereotype( new DefaultArtifactStereotype( "par", "par", "", "java", false, true ) );
        session.setArtifactStereotypeManager( stereotypes );

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
