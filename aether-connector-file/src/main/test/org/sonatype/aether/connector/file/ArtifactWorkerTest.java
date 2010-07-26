package org.sonatype.aether.connector.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sonatype.aether.ArtifactTransferException;
import org.sonatype.aether.DefaultArtifact;
import org.sonatype.aether.DefaultMetadata;
import org.sonatype.aether.Metadata.Nature;
import org.sonatype.aether.MetadataTransferException;
import org.sonatype.aether.RemoteRepository;
import org.sonatype.aether.RepositoryPolicy;
import org.sonatype.aether.TransferCancelledException;
import org.sonatype.aether.TransferEvent;
import org.sonatype.aether.spi.connector.ArtifactDownload;
import org.sonatype.aether.spi.connector.ArtifactUpload;
import org.sonatype.aether.spi.connector.MetadataDownload;
import org.sonatype.aether.spi.connector.MetadataUpload;
import org.sonatype.aether.util.DefaultRepositorySystemSession;
import org.sonatype.aether.util.listener.AbstractTransferListener;
import org.sonatype.aether.util.listener.DefaultTransferEvent;

import com.thoughtworks.xstream.XStream;

public class ArtifactWorkerTest
{

    private static final class PrintTransferListener
        extends AbstractTransferListener
    {

        private XStream xstream;

        @Override
        public void transferInitiated( TransferEvent event )
            throws TransferCancelledException
        {
            super.transferInitiated( event );
            print(event);
        }

        private void print( TransferEvent event )
        {
            String out = String.format("%s %s :: %s", event.getRequestType(), event.getResource().getResourceName(), event.getType());
            System.out.println(out);
            System.out.println(xstream.toXML( event ));
        }

        public PrintTransferListener()
        {
            super();
            this.xstream = new XStream();
            xstream.omitField( DefaultTransferEvent.class, "dataBuffer" );
            // TODO Auto-generated constructor stub
        }

        @Override
        public void transferStarted( TransferEvent event )
            throws TransferCancelledException
        {
            super.transferStarted( event );
            print(event);
        }

        @Override
        public void transferSucceeded( TransferEvent event )
        {
            super.transferSucceeded( event );
            print(event);
        }

        @Override
        public void transferProgressed( TransferEvent event )
            throws TransferCancelledException
        {
            super.transferProgressed( event );
            print(event);
        }
    }

    private static RemoteRepository repository;

    private static DefaultRepositorySystemSession session;

    @BeforeClass
    public static void setup() throws MalformedURLException {
       repository = new RemoteRepository("test", "default", new File("target/test-repository").toURI().toURL().toString());
       session = new DefaultRepositorySystemSession();
       session.setTransferListener( new PrintTransferListener());
    }

    @Test
    public void testArtifactTransfer() throws IOException, ArtifactTransferException
    {
        DefaultArtifact artifact = new DefaultArtifact( "test", "artId1", "jar", "1" );
        File file = File.createTempFile( "ArtifactWorkerTest", ".jar" );
        file.deleteOnExit();
        FileWriter w = new FileWriter( file );
        String expectedContent = "Dies ist ein Test.";
        w.write( expectedContent );
        w.close();
        
        ArtifactUpload transfer = new ArtifactUpload( artifact, file );
        ArtifactWorker worker = new ArtifactWorker( transfer, repository, session );
        worker.run();
        if ( transfer.getException() != null ) {
            throw transfer.getException();
        }
        
        file = File.createTempFile( "ArtifactWorkerTest", ".jar" );
        file.deleteOnExit();
        
        ArtifactDownload down = new ArtifactDownload(artifact, "", file, "");
        down.setChecksumPolicy( RepositoryPolicy.CHECKSUM_POLICY_FAIL );
        worker = new ArtifactWorker( down, repository, session );
        worker.run();
        if ( down.getException() != null) {
            throw down.getException();
        }
        
        BufferedReader r = new BufferedReader(new FileReader(file));
        String content = null;
        String actualContent = "";
        while ( (content = r.readLine()) != null )
	        actualContent += content;
        
        Assert.assertEquals( expectedContent, actualContent );
    }
    
    @Test
    public void testMetadataTransfer() throws IOException, MetadataTransferException {
        File file = File.createTempFile( "ArtifactWorkerTest", ".jar" );
        file.deleteOnExit();
        FileWriter w = new FileWriter( file );
        String expectedContent = "Dies ist ein Test.";
        w.write( expectedContent );
        w.close();
        
        DefaultMetadata metadata = new DefaultMetadata("test", "artId1", "1", "jar" , Nature.RELEASE_OR_SNAPSHOT);
        MetadataUpload up = new MetadataUpload( metadata, file );
        ArtifactWorker worker = new ArtifactWorker( up, repository, session );
        worker.run();
        if ( up.getException() != null ) {
            throw up.getException();
        }

        file = File.createTempFile( "ArtifactWorkerTest", ".jar" );
        file.deleteOnExit();
        
        MetadataDownload down = new MetadataDownload();
        down.setChecksumPolicy( RepositoryPolicy.CHECKSUM_POLICY_FAIL );
        down.setMetadata( metadata ).setFile( file );
        worker = new ArtifactWorker( down, repository, session );
        worker.run();
        
        if ( down.getException() != null) {
            throw down.getException();
        }
        
        BufferedReader r = new BufferedReader(new FileReader(file));
        String content = null;
        String actualContent = "";
        while ( (content = r.readLine()) != null )
            actualContent += content;
        
        Assert.assertEquals( expectedContent, actualContent );
    }

}
