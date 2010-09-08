package org.sonatype.aether.impl.internal;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.collection.CollectRequest;
import org.sonatype.aether.collection.CollectResult;
import org.sonatype.aether.collection.DependencyCollectionException;
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
        // manager.setConnector( new RecordingRepositoryConnector( null, null, null, null ) );

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
        assertEquals( expect.getDependency(), newRoot.getChildren().get( 0 ).getDependency() );

        expect = parser.parseLiteral( "gid:aid2:ext:ver:compile" );
        assertEquals( expect.getDependency(), path( newRoot, 1 ).getDependency() );
        assertEquals( expect.getDependency(), path( newRoot, 0, 0 ).getDependency() );
        assertEquals( path( newRoot, 1 ).getDependency(), path( newRoot, 0, 0 ).getDependency() );
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

    private void assertEqualSubtree( DependencyNode root1, DependencyNode root2 )
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
        assertEquals( root1.getDependency(), path( result.getRoot(), 0 ).getDependency() );

        assertEquals( 1, path( result.getRoot(), 0 ).getChildren().size() );
        assertEquals( root2.getDependency(), path( result.getRoot(), 0, 0 ).getDependency() );

        assertEquals( 0, path( result.getRoot(), 1 ).getChildren().size() );
        assertEquals( root2.getDependency(), path( result.getRoot(), 1 ).getDependency() );
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

}
