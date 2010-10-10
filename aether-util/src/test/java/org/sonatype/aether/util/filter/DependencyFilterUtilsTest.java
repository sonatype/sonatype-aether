package org.sonatype.aether.util.filter;

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

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.sonatype.aether.graph.DependencyFilter;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.test.util.NodeBuilder;

/**
 * @author Benjamin Bentmann
 */
public class DependencyFilterUtilsTest
{

    private static List<DependencyNode> PARENTS = Collections.emptyList();

    @Test
    public void testClasspathFilterCompile()
    {
        NodeBuilder builder = new NodeBuilder().artifactId( "aid" );
        DependencyFilter filter = DependencyFilterUtils.classpathFilter( "compile" );

        assertTrue( filter.accept( builder.scope( "compile" ).build(), PARENTS ) );
        assertTrue( filter.accept( builder.scope( "system" ).build(), PARENTS ) );
        assertTrue( filter.accept( builder.scope( "provided" ).build(), PARENTS ) );
        assertFalse( filter.accept( builder.scope( "runtime" ).build(), PARENTS ) );
        assertFalse( filter.accept( builder.scope( "test" ).build(), PARENTS ) );
    }

    @Test
    public void testClasspathFilterRuntime()
    {
        NodeBuilder builder = new NodeBuilder().artifactId( "aid" );
        DependencyFilter filter = DependencyFilterUtils.classpathFilter( "runtime" );

        assertTrue( filter.accept( builder.scope( "compile" ).build(), PARENTS ) );
        assertFalse( filter.accept( builder.scope( "system" ).build(), PARENTS ) );
        assertFalse( filter.accept( builder.scope( "provided" ).build(), PARENTS ) );
        assertTrue( filter.accept( builder.scope( "runtime" ).build(), PARENTS ) );
        assertFalse( filter.accept( builder.scope( "test" ).build(), PARENTS ) );
    }

    @Test
    public void testClasspathFilterTest()
    {
        NodeBuilder builder = new NodeBuilder().artifactId( "aid" );
        DependencyFilter filter = DependencyFilterUtils.classpathFilter( "test" );

        assertTrue( filter.accept( builder.scope( "compile" ).build(), PARENTS ) );
        assertTrue( filter.accept( builder.scope( "system" ).build(), PARENTS ) );
        assertTrue( filter.accept( builder.scope( "provided" ).build(), PARENTS ) );
        assertTrue( filter.accept( builder.scope( "runtime" ).build(), PARENTS ) );
        assertTrue( filter.accept( builder.scope( "test" ).build(), PARENTS ) );
    }

    @Test
    public void testClasspathFilterCompileRuntime()
    {
        NodeBuilder builder = new NodeBuilder().artifactId( "aid" );
        DependencyFilter filter = DependencyFilterUtils.classpathFilter( "compile", "runtime" );

        assertTrue( filter.accept( builder.scope( "compile" ).build(), PARENTS ) );
        assertTrue( filter.accept( builder.scope( "system" ).build(), PARENTS ) );
        assertTrue( filter.accept( builder.scope( "provided" ).build(), PARENTS ) );
        assertTrue( filter.accept( builder.scope( "runtime" ).build(), PARENTS ) );
        assertFalse( filter.accept( builder.scope( "test" ).build(), PARENTS ) );
    }

    @Test
    public void testClasspathFilterCompilePlusRuntime()
    {
        NodeBuilder builder = new NodeBuilder().artifactId( "aid" );
        DependencyFilter filter = DependencyFilterUtils.classpathFilter( "compile+runtime" );

        assertTrue( filter.accept( builder.scope( "compile" ).build(), PARENTS ) );
        assertTrue( filter.accept( builder.scope( "system" ).build(), PARENTS ) );
        assertTrue( filter.accept( builder.scope( "provided" ).build(), PARENTS ) );
        assertTrue( filter.accept( builder.scope( "runtime" ).build(), PARENTS ) );
        assertFalse( filter.accept( builder.scope( "test" ).build(), PARENTS ) );
    }

    @Test
    public void testClasspathFilterRuntimeCommaSystem()
    {
        NodeBuilder builder = new NodeBuilder().artifactId( "aid" );
        DependencyFilter filter = DependencyFilterUtils.classpathFilter( "runtime,system" );

        assertTrue( filter.accept( builder.scope( "compile" ).build(), PARENTS ) );
        assertTrue( filter.accept( builder.scope( "system" ).build(), PARENTS ) );
        assertFalse( filter.accept( builder.scope( "provided" ).build(), PARENTS ) );
        assertTrue( filter.accept( builder.scope( "runtime" ).build(), PARENTS ) );
        assertFalse( filter.accept( builder.scope( "test" ).build(), PARENTS ) );
    }

    @Test
    public void testClasspathFilterNull()
    {
        NodeBuilder builder = new NodeBuilder().artifactId( "aid" );
        DependencyFilter filter = DependencyFilterUtils.classpathFilter( (String[]) null );

        assertFalse( filter.accept( builder.scope( "compile" ).build(), PARENTS ) );
        assertFalse( filter.accept( builder.scope( "system" ).build(), PARENTS ) );
        assertFalse( filter.accept( builder.scope( "provided" ).build(), PARENTS ) );
        assertFalse( filter.accept( builder.scope( "runtime" ).build(), PARENTS ) );
        assertFalse( filter.accept( builder.scope( "test" ).build(), PARENTS ) );
    }

    @Test
    public void testClasspathFilterUnknownScope()
    {
        NodeBuilder builder = new NodeBuilder().artifactId( "aid" );
        DependencyFilter filter = DependencyFilterUtils.classpathFilter( "compile" );

        assertTrue( filter.accept( builder.scope( "" ).build(), PARENTS ) );
        assertTrue( filter.accept( builder.scope( "unknown" ).build(), PARENTS ) );
    }

}
