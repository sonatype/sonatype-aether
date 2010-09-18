package demo.aether;

import java.io.File;

import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.util.artifact.SubArtifact;

public class AetherDemo
{
    public static void main( String[] args )
        throws Exception
    {
        Aether aether = new Aether( "http://localhost:8081/nexus/content/groups/public", "/Users/jvanzyl/aether-repo" );
                
        AetherResult result = aether.resolve( "org.apache.maven", "maven-aether-provider", "3.0-beta-3" );
        
        System.out.println( "-------------------------------------" );
        System.out.println( "Resolved Tree" );
        System.out.println( "-------------------------------------" );
        System.out.println();
        System.out.println( result.getTree() );
        System.out.println();

        System.out.println( "-------------------------------------" );
        System.out.println( "Resolved Files" );
        System.out.println( "-------------------------------------" );
        System.out.println();
        for( File file : result.getResolvedFiles() )
        {
            System.out.println( file );
        }
        System.out.println();
        
        System.out.println( "-------------------------------------" );
        System.out.println( "Resolved ClassPath" );
        System.out.println( "-------------------------------------" );
        System.out.println();
        System.out.println( result.getResolvedClassPath() );
        System.out.println();
        
        Artifact artifact = new DefaultArtifact( "test", "demo", "", "jar", "0.1-SNAPSHOT" );
        artifact = artifact.setFile( new File( "demo.jar" ) );
        Artifact pom = new SubArtifact( artifact, "", "pom" );
        pom = pom.setFile( new File( "pom.xml" ) );
                
        System.out.println( "-------------------------------------" );
        System.out.println( "Installing..." );
        System.out.println( "-------------------------------------" );
        System.out.println();
        aether.install( artifact, pom );
        
        System.out.println( "-------------------------------------" );
        System.out.println( "Deploying..." );
        System.out.println( "-------------------------------------" );
        System.out.println();
        aether.deploy( artifact, pom, "http://localhost:8081/nexus/content/repositories/snapshots/" );
    }
}
