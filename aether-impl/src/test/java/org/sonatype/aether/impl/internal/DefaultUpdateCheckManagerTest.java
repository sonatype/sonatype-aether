package org.sonatype.aether.impl.internal;

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

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.impl.UpdateCheck;
import org.sonatype.aether.metadata.Metadata;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.repository.RepositoryPolicy;
import org.sonatype.aether.test.impl.TestRepositorySystemSession;
import org.sonatype.aether.test.util.FileUtil;
import org.sonatype.aether.test.util.impl.StubArtifact;
import org.sonatype.aether.test.util.impl.StubMetadata;
import org.sonatype.aether.transfer.ArtifactTransferException;
import org.sonatype.aether.transfer.MetadataTransferException;

/**
 * @author Benjamin Hanzelmann
 */
public class DefaultUpdateCheckManagerTest
{

    private DefaultUpdateCheckManager manager;

    private TestRepositorySystemSession session;

    private StubMetadata metadata;

    private RemoteRepository repository;

    private StubArtifact artifact;

    @Before
    public void setup()
        throws IOException
    {
        session = new TestRepositorySystemSession();
        repository = new RemoteRepository( "id", "default", new File( "target/test-DUCM/" ).toURL().toString() );
        manager = new DefaultUpdateCheckManager();
        metadata =
            new StubMetadata( "gid", "aid", "ver", "maven-metadata.xml", Metadata.Nature.RELEASE_OR_SNAPSHOT,
                              FileUtil.createTempFile( "metadata" ) );
        artifact = new StubArtifact( "gid", "aid", "", "ext", "ver" ).setFile( FileUtil.createTempFile( "artifact" ) );
    }

    @Test( expected = IllegalArgumentException.class )
    public void testCheckMetadataFailOnNoFile()
    {
        UpdateCheck<Metadata, MetadataTransferException> check = newMetadataCheck();
        check.setItem( metadata.setFile( null ) );
        check.setFile( null );

        manager.checkMetadata( session, check );
    }

    @Test
    public void testCheckMetadataUpdatePolicyRequired()
    {
        UpdateCheck<Metadata, MetadataTransferException> check = newMetadataCheck();
        check.setItem( metadata );
        check.setFile( metadata.getFile() );

        Calendar cal = Calendar.getInstance( TimeZone.getTimeZone( "UTC" ) );
        cal.set( Calendar.DATE, cal.get( Calendar.DATE ) - 1 );
        check.setLocalLastUpdated( cal.getTimeInMillis() );

        check.setPolicy( RepositoryPolicy.UPDATE_POLICY_ALWAYS );
        manager.checkMetadata( session, check );
        assertNull( check.getException() );
        assertTrue( check.isRequired() );

        check.setPolicy( RepositoryPolicy.UPDATE_POLICY_DAILY );
        manager.checkMetadata( session, check );
        assertNull( check.getException() );
        assertTrue( check.isRequired() );

        check.setPolicy( RepositoryPolicy.UPDATE_POLICY_INTERVAL + ":60" );
        manager.checkMetadata( session, check );
        assertNull( check.getException() );
        assertTrue( check.isRequired() );
    }

    @Test
    public void testCheckMetadataUpdatePolicyNotRequired()
    {
        UpdateCheck<Metadata, MetadataTransferException> check = new UpdateCheck<Metadata, MetadataTransferException>();
        check.setItem( metadata );
        check.setFile( metadata.getFile() );

        Calendar cal = Calendar.getInstance( TimeZone.getTimeZone( "UTC" ) );
        cal.set( Calendar.HOUR, cal.get( Calendar.HOUR ) - 1 );
        check.setLocalLastUpdated( cal.getTimeInMillis() );

        check.setPolicy( RepositoryPolicy.UPDATE_POLICY_NEVER );
        manager.checkMetadata( session, check );
        assertFalse( check.isRequired() );

        check.setPolicy( RepositoryPolicy.UPDATE_POLICY_DAILY );
        manager.checkMetadata( session, check );
        assertFalse( check.isRequired() );

        check.setPolicy( RepositoryPolicy.UPDATE_POLICY_INTERVAL + ":61" );
        manager.checkMetadata( session, check );
        assertFalse( check.isRequired() );

        check.setPolicy( "no particular policy" );
        manager.checkMetadata( session, check );
        assertFalse( check.isRequired() );
    }

    @Test
    public void testCheckMetadata()
        throws FileNotFoundException, IOException
    {
        UpdateCheck<Metadata, MetadataTransferException> check = new UpdateCheck<Metadata, MetadataTransferException>();
        check.setItem( metadata );
        check.setFile( metadata.getFile() );
        check.setRepository( repository );
        check.setPolicy( RepositoryPolicy.UPDATE_POLICY_DAILY );

        File propertiesFile = new File( check.getFile().getParentFile(), "resolver-status.properties" );

        // never checked before
        manager.checkMetadata( session, check );
        assertEquals( true, check.isRequired() );
        assertEquals( check.getLocalLastUpdated(), 0 );

        // just checked
        Properties p = new Properties();
        long lastUpdate = new Date().getTime();
        p.put( check.getFile().getName() + ".lastUpdated", String.valueOf( lastUpdate ) );
        FileOutputStream out = new FileOutputStream( propertiesFile );
        p.store( out, "" );
        out.close();

        manager.checkMetadata( session, check );
        assertEquals( false, check.isRequired() );

        // no local file, no repo timestamp
        check.setLocalLastUpdated( 0 );
        check.getFile().delete();
        manager.checkMetadata( session, check );
        assertEquals( true, check.isRequired() );
        // (! file.exists && ! repoKey) -> no timestamp

    }

    @Test
    public void testCheckMetadataNoLocalFile()
        throws IOException
    {
        metadata.getFile().delete();

        UpdateCheck<Metadata, MetadataTransferException> check = newMetadataCheck();

        File propertiesFile = new File( check.getFile().getParentFile(), "resolver-status.properties" );

        long lastUpdate = new Date().getTime() - ( 1800 * 1000 );
        Properties p = new Properties();
        p.put( check.getFile().getName() + ".lastUpdated", String.valueOf( lastUpdate ) );
        p.put( repository.getUrl() + ".maven-metadata.xml.lastUpdated", String.valueOf( lastUpdate ) );
        FileOutputStream out = new FileOutputStream( propertiesFile );
        p.store( out, "" );
        out.close();

        // ! file.exists && updateRequired -> check in remote repo
        check.setLocalLastUpdated( lastUpdate );
        manager.checkMetadata( session, check );
        assertEquals( true, check.isRequired() );
    }

    @Test
    public void testCheckMetadataNotFoundInRepoCachingEnabled()
        throws IOException
    {
        metadata.getFile().delete();
        session.setNotFoundCachingEnabled( true );

        UpdateCheck<Metadata, MetadataTransferException> check = newMetadataCheck();
        File propertiesFile = new File( check.getFile().getParentFile(), "resolver-status.properties" );

        long lastUpdate = new Date().getTime() - ( 1800 * 1000 );
        Properties p = new Properties();
        p.put( check.getFile().getName() + ".lastUpdated", String.valueOf( lastUpdate ) );
        p.put( repository.getUrl() + ".maven-metadata.xml.lastUpdated", String.valueOf( lastUpdate ) );
        FileOutputStream out = new FileOutputStream( propertiesFile );
        p.store( out, "" );
        out.close();

        // ! file.exists && ! updateRequired -> artifact not found in remote repo
        check.setPolicy( RepositoryPolicy.UPDATE_POLICY_DAILY );
        manager.checkMetadata( session, check );
        assertEquals( false, check.isRequired() );
        assertNotNull( check.getException() );
    }

    @Test
    public void testCheckMetadataNotFoundInRepoCachingDisabled()
        throws IOException
    {
        metadata.getFile().delete();
        session.setNotFoundCachingEnabled( false );

        UpdateCheck<Metadata, MetadataTransferException> check = newMetadataCheck();
        File propertiesFile = new File( check.getFile().getParentFile(), "resolver-status.properties" );

        long lastUpdate = new Date().getTime() - ( 1800 * 1000 );
        Properties p = new Properties();
        p.put( check.getFile().getName() + ".lastUpdated", String.valueOf( lastUpdate ) );
        p.put( repository.getUrl() + ".maven-metadata.xml.lastUpdated", String.valueOf( lastUpdate ) );
        FileOutputStream out = new FileOutputStream( propertiesFile );
        p.store( out, "" );
        out.close();

        // ! file.exists && ! updateRequired -> artifact not found in remote repo
        // ignore NotFoundCaching-setting, don't check if update policy does not say so for metadata
        check.setPolicy( RepositoryPolicy.UPDATE_POLICY_DAILY );
        manager.checkMetadata( session, check );
        assertEquals( false, check.isRequired() );
        assertNotNull( check.getException() );
    }

    @Test
    public void testCheckMetadataErrorFromRepo()
        throws IOException
    {
        metadata.getFile().delete();

        UpdateCheck<Metadata, MetadataTransferException> check = newMetadataCheck();
        check.setPolicy( RepositoryPolicy.UPDATE_POLICY_DAILY );
        File propertiesFile = new File( check.getFile().getParentFile(), "resolver-status.properties" );

        long lastUpdate = new Date().getTime() - ( 1800 * 1000 );
        Properties p = new Properties();
        p.put( check.getFile().getName() + ".lastUpdated", String.valueOf( lastUpdate ) );
        p.put( repository.getUrl() + ".maven-metadata.xml.lastUpdated", String.valueOf( lastUpdate ) );
        p.put( repository.getUrl() + ".maven-metadata.xml.error", "some error message" );
        FileOutputStream out = new FileOutputStream( propertiesFile );
        p.store( out, "" );
        out.close();

        // ! file.exists && ! updateRequired && previousError -> depends on transfer error caching
        session.setTransferErrorCachingEnabled( true );
        manager.checkMetadata( session, check );
        assertEquals( false, check.isRequired() );
        assertNotNull( check.getException() );
    }

    @Test
    public void testCheckMetadataErrorFromRepoNoCaching()
        throws IOException
    {
        metadata.getFile().delete();

        UpdateCheck<Metadata, MetadataTransferException> check = newMetadataCheck();
        check.setPolicy( RepositoryPolicy.UPDATE_POLICY_DAILY );
        File propertiesFile = new File( check.getFile().getParentFile(), "resolver-status.properties" );

        long lastUpdate = new Date().getTime() - ( 1800 * 1000 );
        Properties p = new Properties();
        p.put( check.getFile().getName() + ".lastUpdated", String.valueOf( lastUpdate ) );
        p.put( repository.getUrl() + ".maven-metadata.xml.lastUpdated", String.valueOf( lastUpdate ) );
        p.put( repository.getUrl() + ".maven-metadata.xml.error", "some error message" );
        FileOutputStream out = new FileOutputStream( propertiesFile );
        p.store( out, "" );
        out.close();

        // ! file.exists && ! updateRequired && previousError -> depends on transfer error caching
        session.setTransferErrorCachingEnabled( false );
        manager.checkMetadata( session, check );
        assertEquals( true, check.isRequired() );
        assertNull( check.getException() );
    }

    public UpdateCheck<Metadata, MetadataTransferException> newMetadataCheck()
    {
        UpdateCheck<Metadata, MetadataTransferException> check = new UpdateCheck<Metadata, MetadataTransferException>();
        check.setItem( metadata );
        check.setFile( metadata.getFile() );
        check.setRepository( repository );
        check.setPolicy( RepositoryPolicy.UPDATE_POLICY_INTERVAL + ":10" );
        return check;
    }

    public UpdateCheck<Artifact, ArtifactTransferException> newArtifactCheck()
    {
        UpdateCheck<Artifact, ArtifactTransferException> check = new UpdateCheck<Artifact, ArtifactTransferException>();
        check.setItem( artifact );
        check.setFile( artifact.getFile() );
        check.setRepository( repository );
        check.setPolicy( RepositoryPolicy.UPDATE_POLICY_INTERVAL + ":10" );
        return check;
    }

    @After
    public void teardown()
    {
        new File( metadata.getFile().getParent(), "resolver-status.properties" ).delete();
        new File( artifact.getFile().getPath() + ".lastUpdated" ).delete();
        metadata.getFile().delete();
        artifact.getFile().delete();
    }

    @Test( expected = IllegalArgumentException.class )
    public void testCheckArtifactFailOnNoFile()
    {
        UpdateCheck<Artifact, ArtifactTransferException> check = newArtifactCheck();
        check.setItem( artifact.setFile( null ) );
        check.setFile( null );

        manager.checkArtifact( session, check );
        assertNotNull( check.getException() );
        assertEquals( 0, check.getLocalLastUpdated() );
    }

    @Test
    public void testCheckArtifactUpdatePolicyRequired()
    {
        UpdateCheck<Artifact, ArtifactTransferException> check = newArtifactCheck();
        check.setItem( artifact );
        check.setFile( artifact.getFile() );

        Calendar cal = Calendar.getInstance( TimeZone.getTimeZone( "UTC" ) );
        cal.set( Calendar.DATE, cal.get( Calendar.DATE ) - 1 );
        long lastUpdate = cal.getTimeInMillis();
        artifact.getFile().setLastModified( lastUpdate );
        check.setLocalLastUpdated( lastUpdate );

        check.setPolicy( RepositoryPolicy.UPDATE_POLICY_ALWAYS );
        manager.checkArtifact( session, check );
        assertNull( check.getException() );
        assertTrue( check.isRequired() );

        check.setPolicy( RepositoryPolicy.UPDATE_POLICY_DAILY );
        manager.checkArtifact( session, check );
        assertNull( check.getException() );
        assertTrue( check.isRequired() );

        check.setPolicy( RepositoryPolicy.UPDATE_POLICY_INTERVAL + ":60" );
        manager.checkArtifact( session, check );
        assertNull( check.getException() );
        assertTrue( check.isRequired() );
    }

    @Test
    public void testCheckArtifactUpdatePolicyNotRequired()
    {
        UpdateCheck<Artifact, ArtifactTransferException> check = newArtifactCheck();
        check.setItem( artifact );
        check.setFile( artifact.getFile() );

        Calendar cal = Calendar.getInstance( TimeZone.getTimeZone( "UTC" ) );
        cal.set( Calendar.HOUR, cal.get( Calendar.HOUR ) - 1 );
        check.setLocalLastUpdated( cal.getTimeInMillis() );

        check.setPolicy( RepositoryPolicy.UPDATE_POLICY_NEVER );
        manager.checkArtifact( session, check );
        assertFalse( check.isRequired() );

        check.setPolicy( RepositoryPolicy.UPDATE_POLICY_DAILY );
        manager.checkArtifact( session, check );
        assertFalse( check.isRequired() );

        check.setPolicy( RepositoryPolicy.UPDATE_POLICY_INTERVAL + ":61" );
        manager.checkArtifact( session, check );
        assertFalse( check.isRequired() );

        check.setPolicy( "no particular policy" );
        manager.checkArtifact( session, check );
        assertFalse( check.isRequired() );
    }

    @Test
    public void testCheckArtifact()
        throws FileNotFoundException, IOException
    {
        UpdateCheck<Artifact, ArtifactTransferException> check = newArtifactCheck();
        long fifteenMinutes = new Date().getTime() - ( 15 * 60 * 1000 );
        check.getFile().setLastModified( fifteenMinutes );
        // time is truncated on setLastModfied
        fifteenMinutes = check.getFile().lastModified();

        // never checked before
        manager.checkArtifact( session, check );
        assertEquals( true, check.isRequired() );

        // just checked
        check.setLocalLastUpdated( 0 );
        long lastUpdate = new Date().getTime();
        check.getFile().setLastModified( lastUpdate );
        lastUpdate = check.getFile().lastModified();

        manager.checkArtifact( session, check );
        assertEquals( false, check.isRequired() );

        // no local file, no repo timestamp
        check.setLocalLastUpdated( 0 );
        check.getFile().delete();
        manager.checkArtifact( session, check );
        assertEquals( true, check.isRequired() );
    }

    @Test
    public void testCheckArtifactNoLocalFile()
        throws IOException
    {
        artifact.getFile().delete();
        UpdateCheck<Artifact, ArtifactTransferException> check = newArtifactCheck();

        File propertiesFile = new File( check.getFile().getPath() + ".lastUpdated" );

        long lastUpdate = new Date().getTime() - ( 1800 * 1000 );
        Properties p = new Properties();
        p.put( check.getFile().getName() + ".lastUpdated", String.valueOf( lastUpdate ) );
        p.put( repository.getUrl() + ".lastUpdated", String.valueOf( lastUpdate ) );
        FileOutputStream out = new FileOutputStream( propertiesFile );
        p.store( out, "" );
        out.close();

        // ! file.exists && updateRequired -> check in remote repo
        check.setLocalLastUpdated( lastUpdate );
        manager.checkArtifact( session, check );
        assertEquals( true, check.isRequired() );
    }

    @Test
    public void testCheckArtifactNotFoundInRepoCachingEnabled()
        throws IOException
    {
        artifact.getFile().delete();
        session.setNotFoundCachingEnabled( true );

        UpdateCheck<Artifact, ArtifactTransferException> check = newArtifactCheck();
        File propertiesFile = new File( check.getFile().getPath() + ".lastUpdated" );

        long lastUpdate = new Date().getTime() - ( 1800 * 1000 );
        Properties p = new Properties();
        p.put( repository.getUrl() + ".lastUpdated", String.valueOf( lastUpdate ) );
        FileOutputStream out = new FileOutputStream( propertiesFile );
        p.store( out, "" );
        out.close();

        // ! file.exists && ! updateRequired -> artifact not found in remote repo
        check.setPolicy( RepositoryPolicy.UPDATE_POLICY_DAILY );
        manager.checkArtifact( session, check );
        assertEquals( false, check.isRequired() );
        assertNotNull( check.getException() );
    }

    @Test
    public void testCheckArtifactNotFoundInRepoCachingDisabled()
        throws IOException
    {
        artifact.getFile().delete();
        session.setNotFoundCachingEnabled( false );

        UpdateCheck<Artifact, ArtifactTransferException> check = newArtifactCheck();
        File propertiesFile = new File( check.getFile().getPath() + ".lastUpdated" );

        long lastUpdate = new Date().getTime() - ( 1800 * 1000 );
        Properties p = new Properties();
        p.put( repository.getUrl() + ".lastUpdated", String.valueOf( lastUpdate ) );
        FileOutputStream out = new FileOutputStream( propertiesFile );
        p.store( out, "" );
        out.close();

        // ! file.exists && ! updateRequired -> artifact not found in remote repo
        check.setPolicy( RepositoryPolicy.UPDATE_POLICY_DAILY );
        manager.checkArtifact( session, check );
        assertEquals( true, check.isRequired() );
        assertNull( check.getException() );
    }

    @Test
    public void testCheckArtifactErrorFromRepoCachingEnabled()
        throws IOException
    {
        artifact.getFile().delete();

        UpdateCheck<Artifact, ArtifactTransferException> check = newArtifactCheck();
        check.setPolicy( RepositoryPolicy.UPDATE_POLICY_DAILY );
        File propertiesFile = new File( check.getFile().getPath() + ".lastUpdated" );

        long lastUpdate = new Date().getTime() - ( 1800 * 1000 );
        Properties p = new Properties();
        p.put( repository.getUrl() + ".lastUpdated", String.valueOf( lastUpdate ) );
        p.put( repository.getUrl() + ".error", "some error message" );
        FileOutputStream out = new FileOutputStream( propertiesFile );
        p.store( out, "" );
        out.close();

        // ! file.exists && ! updateRequired && previousError -> depends on transfer error caching
        session.setTransferErrorCachingEnabled( true );
        manager.checkArtifact( session, check );
        assertEquals( false, check.isRequired() );
        assertNotNull( check.getException() );
    }

    @Test
    public void testCheckArtifactErrorFromRepoCachingDisabled()
        throws IOException
    {
        artifact.getFile().delete();

        UpdateCheck<Artifact, ArtifactTransferException> check = newArtifactCheck();
        check.setPolicy( RepositoryPolicy.UPDATE_POLICY_DAILY );
        File propertiesFile = new File( check.getFile().getPath() + ".lastUpdated" );

        long lastUpdate = new Date().getTime() - ( 1800 * 1000 );
        Properties p = new Properties();
        p.put( repository.getUrl() + ".lastUpdated", String.valueOf( lastUpdate ) );
        p.put( repository.getUrl() + ".error", "some error message" );
        FileOutputStream out = new FileOutputStream( propertiesFile );
        p.store( out, "" );
        out.close();

        // ! file.exists && ! updateRequired && previousError -> depends on transfer error caching
        session.setTransferErrorCachingEnabled( false );
        manager.checkArtifact( session, check );
        assertEquals( true, check.isRequired() );
        assertNull( check.getException() );
    }

    @Test
    public void testTouchMetadata()
        throws IOException
    {
        long previous = new Date().getTime() - ( 2 * 60 * 60 * 1000 );
        UpdateCheck<Metadata, MetadataTransferException> check = newMetadataCheck();

        artifact.getFile().setLastModified( previous );
        check.setPolicy( RepositoryPolicy.UPDATE_POLICY_INTERVAL + ":60" );
        manager.checkMetadata( session, check );
        assertTrue( "check is marked as not required, but should be", check.isRequired() );
        manager.touchMetadata( session, check );

        check = newMetadataCheck();
        check.setPolicy( RepositoryPolicy.UPDATE_POLICY_INTERVAL + ":60" );
        manager.checkMetadata( session, check );
        assertFalse( "check is marked as required directly after touchMetadata()", check.isRequired() );
    }

}
