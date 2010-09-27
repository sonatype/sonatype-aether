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

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.collection.CollectRequest;
import org.sonatype.aether.collection.CollectResult;
import org.sonatype.aether.collection.DependencyCollectionContext;
import org.sonatype.aether.collection.DependencyCollectionException;
import org.sonatype.aether.collection.DependencyManagement;
import org.sonatype.aether.collection.DependencyManager;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.impl.ArtifactDescriptorReader;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.ArtifactDescriptorException;
import org.sonatype.aether.resolution.ArtifactDescriptorRequest;
import org.sonatype.aether.resolution.ArtifactDescriptorResult;
import org.sonatype.aether.test.impl.TestRepositorySystemSession;
import org.sonatype.aether.test.util.DependencyGraphParser;
import org.sonatype.aether.test.util.IniArtifactDescriptorReader;
import org.sonatype.aether.util.artifact.ArtifactProperties;
import org.sonatype.aether.util.graph.manager.ClassicDependencyManager;

/**
 * @author Benjamin Hanzelmann
 */
public class DefaultDependencyCollectorTest
{

    private DefaultDependencyCollector collector;

    private StubRemoteRepositoryManager manager;

    private TestRepositorySystemSession session;

    private DependencyGraphParser parser;

    private RemoteRepository repository;

    @Before
    public void setup()
    {
        session = new TestRepositorySystemSession();

        collector = new DefaultDependencyCollector();
        collector.setArtifactDescriptorReader( new ArtifactDescriptorReader()
        {
            IniArtifactDescriptorReader reader = new IniArtifactDescriptorReader( "artifact-descriptions/" );

            public ArtifactDescriptorResult readArtifactDescriptor( RepositorySystemSession session,
                                                                    ArtifactDescriptorRequest request )
                throws ArtifactDescriptorException
            {
                return reader.readArtifactDescriptor( session, request );
            }
        } );

        collector.setVersionRangeResolver( new StubVersionRangeResolver() );

        manager = new StubRemoteRepositoryManager();

        collector.setRemoteRepositoryManager( manager );

        parser = new DependencyGraphParser( "artifact-descriptions/" );

        repository = new RemoteRepository( "id", "default", "file:///" );
    }

    @Test
    public void testSimpleCollection()
        throws IOException, DependencyCollectionException
    {
        DependencyNode root = parser.parseLiteral( "gid:aid:ext:ver" );
        Dependency dependency = root.getDependency();
        CollectRequest request = new CollectRequest( dependency, Arrays.asList( repository ) );
        CollectResult result = collector.collectDependencies( session, request );

        assertEquals( 0, result.getExceptions().size() );

        DependencyNode newRoot = result.getRoot();
        Dependency newDependency = newRoot.getDependency();

        assertEquals( dependency, newDependency );
        assertEquals( dependency.getArtifact(), newDependency.getArtifact() );

        assertEquals( 1, newRoot.getChildren().size() );

        DependencyNode expect = parser.parseLiteral( "gid:aid2:ext:ver:compile" );
        assertEquals( expect.getDependency(), newRoot.getChildren().get( 0 ).getDependency() );

    }

    @Test
    public void testMissingDependencyDescription()
        throws IOException
    {
        DependencyNode root = parser.parseLiteral( "missing:description:ext:ver" );
        CollectRequest request = new CollectRequest( root.getDependency(), Arrays.asList( repository ) );
        try
        {
            collector.collectDependencies( session, request );
            fail( "expected exception" );
        }
        catch ( DependencyCollectionException e )
        {
            CollectResult result = e.getResult();
            assertEquals( request, result.getRequest() );
            assertNotNull( result.getExceptions() );
            assertEquals( 1, result.getExceptions().size() );

            assertTrue( result.getExceptions().get( 0 ) instanceof ArtifactDescriptorException );

            assertEquals( request.getRoot(), result.getRoot().getDependency() );
        }
    }

    @Test
    public void testDuplicates()
        throws IOException, DependencyCollectionException
    {
        DependencyNode root = parser.parseLiteral( "duplicate:transitive:ext:dependency" );
        Dependency dependency = root.getDependency();
        CollectRequest request = new CollectRequest( dependency, Arrays.asList( repository ) );

        CollectResult result = collector.collectDependencies( session, request );

        assertEquals( 0, result.getExceptions().size() );

        DependencyNode newRoot = result.getRoot();
        Dependency newDependency = newRoot.getDependency();

        assertEquals( dependency, newDependency );
        assertEquals( dependency.getArtifact(), newDependency.getArtifact() );

        assertEquals( 2, newRoot.getChildren().size() );

        DependencyNode expect = parser.parseLiteral( "gid:aid:ext:ver:compile" );
        Dependency dep = expect.getDependency();
        assertEquals( dep, dep( newRoot, 0 ) );

        expect = parser.parseLiteral( "gid:aid2:ext:ver:compile" );
        dep = expect.getDependency();
        assertEquals( dep, dep( newRoot, 1 ) );
        assertEquals( dep, dep( newRoot, 0, 0 ) );
        assertEquals( dep( newRoot, 1 ), dep( newRoot, 0, 0 ) );
    }

    @Test
    public void testEqualSubtree()
        throws IOException, DependencyCollectionException
    {
        DependencyNode root = parser.parse( "expectedSubtreeComparisonResult.txt" );
        Dependency dependency = root.getDependency();
        CollectRequest request = new CollectRequest( dependency, Arrays.asList( repository ) );

        CollectResult result = collector.collectDependencies( session, request );
        assertEqualSubtree( root, result.getRoot() );

    }

    private static void assertEqualSubtree( DependencyNode root1, DependencyNode root2 )
    {
        assertEquals( root1.getDependency(), root2.getDependency() );
        assertEquals( root1.getChildren().size(), root2.getChildren().size() );

        Iterator<DependencyNode> iterator1 = root1.getChildren().iterator();
        Iterator<DependencyNode> iterator2 = root2.getChildren().iterator();

        while ( iterator1.hasNext() )
        {
            assertEqualSubtree( iterator1.next(), iterator2.next() );
        }

    }

    @Test
    public void testPartialResultOnError()
        throws IOException
    {

        DependencyNode root = parser.parse( "expectedPartialSubtreeOnError.txt" );

        Dependency dependency = root.getDependency();
        CollectRequest request = new CollectRequest( dependency, Arrays.asList( repository ) );

        CollectResult result;
        try
        {
            result = collector.collectDependencies( session, request );
            fail( "expected exception " );
        }
        catch ( DependencyCollectionException e )
        {
            result = e.getResult();

            assertEquals( request, result.getRequest() );
            assertNotNull( result.getExceptions() );
            assertEquals( 1, result.getExceptions().size() );

            assertTrue( result.getExceptions().get( 0 ) instanceof ArtifactDescriptorException );

            assertEqualSubtree( root, result.getRoot() );
        }

    }

    @Test
    public void testCollectMultipleDependencies()
        throws IOException, DependencyCollectionException
    {
        DependencyNode root1 = parser.parseLiteral( "gid:aid:ext:ver:compile" );
        DependencyNode root2 = parser.parseLiteral( "gid:aid2:ext:ver:compile" );
        List<Dependency> dependencies = Arrays.asList( root1.getDependency(), root2.getDependency() );
        CollectRequest request = new CollectRequest( dependencies, null, Arrays.asList( repository ) );
        CollectResult result = collector.collectDependencies( session, request );

        assertEquals( 0, result.getExceptions().size() );
        assertEquals( 2, result.getRoot().getChildren().size() );
        assertEquals( root1.getDependency(), dep( result.getRoot(), 0 ) );

        assertEquals( 1, path( result.getRoot(), 0 ).getChildren().size() );
        assertEquals( root2.getDependency(), dep( result.getRoot(), 0, 0 ) );

        assertEquals( 0, path( result.getRoot(), 1 ).getChildren().size() );
        assertEquals( root2.getDependency(), dep( result.getRoot(), 1 ) );
    }

    private Dependency dep( DependencyNode root, int... coords )
    {
        return path( root, coords ).getDependency();
    }

    private DependencyNode path( DependencyNode root, int... coords )
    {
        try
        {
            DependencyNode node = root;
            for ( int i = 0; i < coords.length; i++ )
            {
                node = node.getChildren().get( coords[i] );
            }

            return node;

        }
        catch ( IndexOutOfBoundsException e )
        {
            throw new IllegalArgumentException( "Illegal coordinates for child", e );
        }
        catch ( NullPointerException e )
        {
            throw new IllegalArgumentException( "Illegal coordinates for child", e );
        }

    }

    @Test
    public void testManagedVersionScope()
        throws IOException, DependencyCollectionException
    {
        DependencyNode root = parser.parseLiteral( "managed:aid:ext:ver" );
        Dependency dependency = root.getDependency();
        CollectRequest request = new CollectRequest( dependency, Arrays.asList( repository ) );

        session.setDependencyManager( new ClassicDependencyManager() );

        CollectResult result = collector.collectDependencies( session, request );

        assertEquals( 0, result.getExceptions().size() );

        DependencyNode newRoot = result.getRoot();

        assertEquals( dependency, dep( newRoot ) );
        assertEquals( dependency.getArtifact(), dep( newRoot ).getArtifact() );

        assertEquals( 1, newRoot.getChildren().size() );
        DependencyNode expect = parser.parseLiteral( "gid:aid:ext:ver:compile" );
        assertEquals( dep( expect ), dep( newRoot, 0 ) );

        assertEquals( 1, path( newRoot, 0 ).getChildren().size() );
        expect = parser.parseLiteral( "gid:aid2:ext:managedVersion:managedScope" );
        assertEquals( dep( expect ), dep( newRoot, 0, 0 ) );
    }

    @Test
    public void testDependencyManagement()
        throws IOException, DependencyCollectionException
    {
        collector.setArtifactDescriptorReader( new ArtifactDescriptorReader()
        {
            IniArtifactDescriptorReader reader = new IniArtifactDescriptorReader( "artifact-descriptions/managed/" );

            public ArtifactDescriptorResult readArtifactDescriptor( RepositorySystemSession session,
                                                                    ArtifactDescriptorRequest request )
                throws ArtifactDescriptorException
            {
                return reader.readArtifactDescriptor( session, request );
            }
        } );

        DependencyNode root = parser.parse( "expectedSubtreeComparisonResult.txt" );
        TestDependencyManager depMgmt = new TestDependencyManager();
        depMgmt.addManagedDependency( dep( root, 0 ), "managed", null, null );
        depMgmt.addManagedDependency( dep( root, 0, 1 ), "managed", "managed", null );
        depMgmt.addManagedDependency( dep( root, 1 ), null, null, "managed" );
        session.setDependencyManager( depMgmt );

        // collect result will differ from expectedSubtreeComparisonResult.txt
        // set localPath -> no dependency traversal
        CollectRequest request = new CollectRequest( dep( root ), Arrays.asList( repository ) );
        CollectResult result = collector.collectDependencies( session, request );

        DependencyNode node = result.getRoot();
        assertEquals( "managed", dep( node, 0, 1 ).getArtifact().getVersion() );
        assertEquals( "managed", dep( node, 0, 1 ).getScope() );

        assertEquals( "managed", dep( node, 1 ).getArtifact().getProperty( ArtifactProperties.LOCAL_PATH, null ) );
        assertEquals( "managed", dep( node, 0, 0 ).getArtifact().getProperty( ArtifactProperties.LOCAL_PATH, null ) );
    }

    /**
     * @author Benjamin Hanzelmann
     */
    public class TestDependencyManager
        implements DependencyManager
    {
        private Map<Dependency, String> versions = new HashMap<Dependency, String>();

        private Map<Dependency, String> scopes = new HashMap<Dependency, String>();

        private Map<Dependency, String> paths = new HashMap<Dependency, String>();

        public void addManagedDependency( Dependency d, String version, String scope, String localPath )
        {
            versions.put( d, version );
            scopes.put( d, scope );
            paths.put( d, localPath );
        }

        public DependencyManagement manageDependency( Dependency d)
        {
            DependencyManagement mgmt = new DependencyManagement();
            mgmt.setVersion( versions.get(d) );
            mgmt.setScope( scopes.get(d) );
            String path = paths.get( d );
            if ( path != null )
            {
                Map<String, String> p = new HashMap<String, String>();
                p.put( ArtifactProperties.LOCAL_PATH, path );
                mgmt.setProperties( p );
            }
            return mgmt;
        }

        public DependencyManager deriveChildManager( DependencyCollectionContext context )
        {
            return this;
        }

    }

}
