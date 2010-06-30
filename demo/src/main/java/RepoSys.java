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
import org.codehaus.plexus.DefaultPlexusContainer;
import org.sonatype.maven.repository.Artifact;
import org.sonatype.maven.repository.AuthenticationSelector;
import org.sonatype.maven.repository.CollectRequest;
import org.sonatype.maven.repository.DefaultArtifact;
import org.sonatype.maven.repository.Dependency;
import org.sonatype.maven.repository.DependencySelector;
import org.sonatype.maven.repository.DependencyGraphTransformer;
import org.sonatype.maven.repository.DependencyManager;
import org.sonatype.maven.repository.DependencyNode;
import org.sonatype.maven.repository.DependencyTraverser;
import org.sonatype.maven.repository.DeployRequest;
import org.sonatype.maven.repository.InstallRequest;
import org.sonatype.maven.repository.LocalRepositoryManager;
import org.sonatype.maven.repository.MirrorSelector;
import org.sonatype.maven.repository.ProxySelector;
import org.sonatype.maven.repository.RemoteRepository;
import org.sonatype.maven.repository.RepositorySystemSession;
import org.sonatype.maven.repository.RepositorySystem;
import org.sonatype.maven.repository.connector.wagon.WagonRepositoryConnectorFactory;
import org.sonatype.maven.repository.internal.DefaultArtifactDescriptorReader;
import org.sonatype.maven.repository.internal.DefaultArtifactResolver;
import org.sonatype.maven.repository.internal.DefaultDependencyCollector;
import org.sonatype.maven.repository.internal.DefaultDeployer;
import org.sonatype.maven.repository.internal.DefaultMetadataResolver;
import org.sonatype.maven.repository.internal.DefaultRemoteRepositoryManager;
import org.sonatype.maven.repository.internal.DefaultRepositorySystem;
import org.sonatype.maven.repository.internal.DefaultUpdateCheckManager;
import org.sonatype.maven.repository.internal.DefaultVersionRangeResolver;
import org.sonatype.maven.repository.internal.DefaultVersionResolver;
import org.sonatype.maven.repository.internal.EnhancedLocalRepositoryManager;
import org.sonatype.maven.repository.util.AndDependencySelector;
import org.sonatype.maven.repository.util.ChainedDependencyGraphTransformer;
import org.sonatype.maven.repository.util.ClassicVersionConflictResolver;
import org.sonatype.maven.repository.util.ConflictMarker;
import org.sonatype.maven.repository.util.DefaultArtifactType;
import org.sonatype.maven.repository.util.DefaultArtifactTypeRegistry;
import org.sonatype.maven.repository.util.DefaultAuthenticationSelector;
import org.sonatype.maven.repository.util.DefaultDependencyManager;
import org.sonatype.maven.repository.util.DefaultMirrorSelector;
import org.sonatype.maven.repository.util.DefaultProxySelector;
import org.sonatype.maven.repository.util.DefaultRepositorySystemSession;
import org.sonatype.maven.repository.util.ExclusionDependencySelector;
import org.sonatype.maven.repository.util.FatArtifactTraverser;
import org.sonatype.maven.repository.util.JavaDependencyContextRefiner;
import org.sonatype.maven.repository.util.JavaEffectiveScopeCalculator;
import org.sonatype.maven.repository.util.OptionalDependencySelector;
import org.sonatype.maven.repository.util.ScopeDependencySelector;

public class RepoSys
{

    public static void main( String[] args )
        throws Exception
    {
        RepositorySystem repoSystem = newManagedSystem();
        //RepositorySystem repoSystem = newManualSystem();

        RepositorySystemSession session = newSession();

        CollectRequest collectRequest = new CollectRequest();
        collectRequest.setRoot( new Dependency( new DefaultArtifact( "org.apache.maven", "maven-core", "", "jar",
                                                                     "2.2.1" ), "compile" ) );
        collectRequest.setRepositories( Arrays.asList( new RemoteRepository( "central", "default",
                                                                             "http://repo1.maven.org/maven2/" ) ) );
        DependencyNode root = repoSystem.collectDependencies( session, collectRequest ).getRoot();

        repoSystem.resolveDependencies( session, root, null );

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

    private static RepositorySystemSession newSession()
    {
        DefaultRepositorySystemSession session = new DefaultRepositorySystemSession();

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

        DependencyManager depManager = new DefaultDependencyManager();
        session.setDependencyManager( depManager );

        DependencySelector depFilter =
            new AndDependencySelector( new ScopeDependencySelector( "test", "provided" ), new OptionalDependencySelector(),
                                     new ExclusionDependencySelector() );
        session.setDependencySelector( depFilter );

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
