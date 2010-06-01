import java.io.File;
import java.util.Arrays;

import org.apache.maven.model.building.DefaultModelBuilder;
import org.apache.maven.model.building.DefaultModelProcessor;
import org.apache.maven.model.building.ModelBuilder;
import org.apache.maven.model.composition.DefaultDependencyManagementImporter;
import org.apache.maven.model.inheritance.DefaultInheritanceAssembler;
import org.apache.maven.model.interpolation.StringSearchModelInterpolator;
import org.apache.maven.model.io.DefaultModelReader;
import org.apache.maven.model.locator.DefaultModelLocator;
import org.apache.maven.model.management.DefaultDependencyManagementInjector;
import org.apache.maven.model.management.DefaultPluginManagementInjector;
import org.apache.maven.model.normalization.DefaultModelNormalizer;
import org.apache.maven.model.path.DefaultModelPathTranslator;
import org.apache.maven.model.path.DefaultModelUrlNormalizer;
import org.apache.maven.model.path.DefaultPathTranslator;
import org.apache.maven.model.path.DefaultUrlNormalizer;
import org.apache.maven.model.profile.DefaultProfileInjector;
import org.apache.maven.model.profile.DefaultProfileSelector;
import org.apache.maven.model.profile.activation.JdkVersionProfileActivator;
import org.apache.maven.model.profile.activation.OperatingSystemProfileActivator;
import org.apache.maven.model.profile.activation.PropertyProfileActivator;
import org.apache.maven.model.superpom.DefaultSuperPomProvider;
import org.apache.maven.model.validation.DefaultModelValidator;
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
import org.apache.maven.repository.InstallRequest;
import org.apache.maven.repository.LocalRepositoryManager;
import org.apache.maven.repository.MirrorSelector;
import org.apache.maven.repository.ProxySelector;
import org.apache.maven.repository.RemoteRepository;
import org.apache.maven.repository.RepositorySession;
import org.apache.maven.repository.RepositorySystem;
import org.apache.maven.repository.internal.DefaultArtifactDescriptorReader;
import org.apache.maven.repository.internal.DefaultArtifactResolver;
import org.apache.maven.repository.internal.DefaultDependencyCollector;
import org.apache.maven.repository.internal.DefaultDeployer;
import org.apache.maven.repository.internal.DefaultMetadataResolver;
import org.apache.maven.repository.internal.DefaultRemoteRepositoryManager;
import org.apache.maven.repository.internal.DefaultRepositorySystem;
import org.apache.maven.repository.internal.DefaultUpdateCheckManager;
import org.apache.maven.repository.internal.DefaultVersionRangeResolver;
import org.apache.maven.repository.internal.DefaultVersionResolver;
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
import org.apache.maven.repository.wagon.WagonRepositoryConnectorFactory;
import org.codehaus.plexus.DefaultPlexusContainer;

public class RepoSys
{

    public static void main( String[] args )
        throws Exception
    {
        RepositorySystem repoSystem = newManagedSystem();
        //RepositorySystem repoSystem = newManualSystem();

        RepositorySession session = newSession();

        CollectRequest collectRequest = new CollectRequest();
        collectRequest.setRoot( new Dependency( new DefaultArtifact( "org.apache.maven", "maven-core", "", "jar",
                                                                     "RELEASE" ), "compile" ) );
        collectRequest.setRepositories( Arrays.asList( new RemoteRepository( "central", "default",
                                                                             "http://repo1.maven.org/maven2/" ) ) );
        DependencyNode root = repoSystem.collectDependencies( session, collectRequest ).getRoot();

        repoSystem.resolveDependencies( session, root );

        Artifact projectOutput = new DefaultArtifact( "test", "test", "", "jar", "0.1-SNAPSHOT" );
        projectOutput.setFile( new File( "pom.xml" ) );

        InstallRequest installRequest = new InstallRequest();
        installRequest.addArtifact( projectOutput );
        repoSystem.install( session, installRequest );

        DeployRequest deployRequest = new DeployRequest();
        deployRequest.addArtifact( projectOutput );
        deployRequest.setRepository( new RemoteRepository( "nexus", "default",
                                                           new File( "target/dist-repo" ).toURI().toString() ) );
        repoSystem.deploy( session, deployRequest );

        System.out.println("============================================================");
        dump( root, "" );
    }

    private static RepositorySystem newManagedSystem()
        throws Exception
    {
        return new DefaultPlexusContainer().lookup( RepositorySystem.class );
    }

    private static RepositorySystem newManualSystem()
    {
        DefaultUpdateCheckManager updateCheckMan = new DefaultUpdateCheckManager();

        WagonRepositoryConnectorFactory connectorFactory = new WagonRepositoryConnectorFactory();
        connectorFactory.setWagonProvider( new ManualWagonProvider() );
        
        DefaultRemoteRepositoryManager remoteRepoMan = new DefaultRemoteRepositoryManager();
        remoteRepoMan.setUpdateCheckManager( updateCheckMan );
        remoteRepoMan.addRepositoryConnectorFactory( connectorFactory );

        DefaultMetadataResolver metadataResolver = new DefaultMetadataResolver();
        metadataResolver.setRemoteRepositoryManager( remoteRepoMan );
        metadataResolver.setUpdateCheckManager( updateCheckMan );

        DefaultVersionResolver versionResolver = new DefaultVersionResolver();
        versionResolver.setMetadataResolver( metadataResolver );

        DefaultVersionRangeResolver versionRangeResolver = new DefaultVersionRangeResolver();
        versionRangeResolver.setMetadataResolver( metadataResolver );

        DefaultArtifactResolver artifactResolver = new DefaultArtifactResolver();
        artifactResolver.setRemoteRepositoryManager( remoteRepoMan );
        artifactResolver.setUpdateCheckManager( updateCheckMan );
        artifactResolver.setVersionResolver( versionResolver );

        DefaultArtifactDescriptorReader pomReader = new DefaultArtifactDescriptorReader();
        pomReader.setVersionResolver( versionResolver );
        pomReader.setArtifactResolver( artifactResolver );
        pomReader.setRemoteRepositoryManager( remoteRepoMan );
        pomReader.setModelBuilder( newModelBuilder() );

        DefaultDependencyCollector collector = new DefaultDependencyCollector();
        collector.setRemoteRepositoryManager( remoteRepoMan );
        collector.setArtifactDescriptorReader( pomReader );
        collector.setVersionRangeResolver( versionRangeResolver );

        DefaultDeployer deployer = new DefaultDeployer();
        deployer.setRemoteRepositoryManager( remoteRepoMan );
        deployer.setUpdateCheckManager( updateCheckMan );

        DefaultRepositorySystem repoSys = new DefaultRepositorySystem();
        repoSys.setArtifactDescriptorReader( pomReader );
        repoSys.setArtifactResolver( artifactResolver );
        repoSys.setMetadataResolver( metadataResolver );
        repoSys.setVersionResolver( versionResolver );
        repoSys.setVersionRangeResolver( versionRangeResolver );
        repoSys.setDependencyCollector( collector );
        repoSys.setDeployer( deployer );

        return repoSys;
    }

    private static ModelBuilder newModelBuilder()
    {
        DefaultModelProcessor processor = new DefaultModelProcessor();
        processor.setModelLocator( new DefaultModelLocator() );
        processor.setModelReader( new DefaultModelReader() );

        DefaultProfileSelector profileSelector = new DefaultProfileSelector();
        profileSelector.addProfileActivator( new JdkVersionProfileActivator() );
        profileSelector.addProfileActivator( new OperatingSystemProfileActivator() );
        profileSelector.addProfileActivator( new PropertyProfileActivator() );

        DefaultUrlNormalizer urlNormalizer = new DefaultUrlNormalizer();
        DefaultPathTranslator pathTranslator = new DefaultPathTranslator();
        
        DefaultModelBuilder modelBuilder = new DefaultModelBuilder();
        modelBuilder.setModelProcessor( processor );
        modelBuilder.setModelValidator( new DefaultModelValidator() );
        modelBuilder.setModelNormalizer( new DefaultModelNormalizer() );
        modelBuilder.setModelPathTranslator( new DefaultModelPathTranslator().setPathTranslator( pathTranslator ) );
        modelBuilder.setModelUrlNormalizer( new DefaultModelUrlNormalizer().setUrlNormalizer( urlNormalizer ) );
        modelBuilder.setModelInterpolator( new StringSearchModelInterpolator().setPathTranslator( pathTranslator ).setUrlNormalizer( urlNormalizer ) );
        modelBuilder.setInheritanceAssembler( new DefaultInheritanceAssembler() );
        modelBuilder.setProfileInjector( new DefaultProfileInjector() );
        modelBuilder.setProfileSelector( profileSelector );
        modelBuilder.setSuperPomProvider( new DefaultSuperPomProvider().setModelProcessor( processor ) );
        modelBuilder.setDependencyManagementImporter( new DefaultDependencyManagementImporter() );
        modelBuilder.setDependencyManagementInjector( new DefaultDependencyManagementInjector() );
        modelBuilder.setPluginManagementInjector( new DefaultPluginManagementInjector() );

        return modelBuilder;
    }

    private static RepositorySession newSession()
    {
        DefaultRepositorySession session = new DefaultRepositorySession();

        LocalRepositoryManager localRepoMan =
            new TrackingLocalRepositoryManager( new File( "target/local-repo" ), NullLogger.INSTANCE );
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

        DependencyManager depManager = new DefaultDependencyManager();
        session.setDependencyManager( depManager );

        DependencyFilter depFilter =
            new AndDependencyFilter( new ScopeDependencyFilter( "test", "provided" ), new OptionalDependencyFilter(),
                                     new ExclusionDependencyFilter() );
        session.setDependencyFilter( depFilter );

        DependencyGraphTransformer transformer =
            new ChainedDependencyGraphTransformer( 
                                                   new ConflictMarker(), 
                                                   new JavaEffectiveScopeCalculator(),
                                                   new ClassicVersionConflictResolver(),
                                                   new JavaDependencyContextRefiner() 
                                                   );
        transformer = null;
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
