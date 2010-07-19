package org.sonatype.aether.util.graph;

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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.sonatype.aether.DependencyFilter;
import org.sonatype.aether.DependencyNode;
import org.sonatype.aether.DependencyVisitor;

/**
 * Generates a sequence of artifact files from a dependeny graph by traversing the graph in pre-order. Unresolved
 * artifacts will be skipped.
 * 
 * @author Benjamin Bentmann
 */
public class FileListGenerator
    implements DependencyVisitor
{

    private DependencyFilter filter;

    private List<File> files;

    /**
     * Creates a new list generator.
     */
    public FileListGenerator()
    {
        this( null );
    }

    /**
     * Generates a new list generator with the specified filter.
     * 
     * @param filter The dependency filter to use, may be {@code null} to include all artifacts.
     */
    public FileListGenerator( DependencyFilter filter )
    {
        this.filter = filter;
        this.files = new ArrayList<File>();
    }

    /**
     * Gets the list of artifact files that was generated during the graph traversal.
     * 
     * @return The list of artifact files in preorder, never {@code null}.
     */
    public List<File> getFiles()
    {
        return files;
    }

    /**
     * Gets the list of artifact files that was generated during the traversal of the specified dependency graph.
     * 
     * @param node The root node of the dependency graph to traverse, must not be {@code null}.
     * @return The list of artifact files in preorder, never {@code null}.
     */
    public static List<File> getFiles( DependencyNode node )
    {
        FileListGenerator flg = new FileListGenerator();
        node.accept( flg );
        return flg.getFiles();
    }

    /**
     * Gets the class path by concatenating the list of artifact files that was generated during the graph traversal.
     * 
     * @return The class path, using the platform-specific path separator, never {@code null}.
     */
    public String getClassPath()
    {
        StringBuilder buffer = new StringBuilder( 1024 );
        for ( Iterator<File> it = files.iterator(); it.hasNext(); )
        {
            File file = it.next();
            buffer.append( file.getAbsolutePath() );
            if ( it.hasNext() )
            {
                buffer.append( File.pathSeparatorChar );
            }
        }
        return buffer.toString();
    }

    /**
     * Gets the class path by concatenating the list of artifact files that was generated during the traversal of the
     * specified graph.
     * 
     * @param node The root node of the dependency graph to traverse, must not be {@code null}.
     * @return The class path, using the platform-specific path separator, never {@code null}.
     */
    public static String getClassPath( DependencyNode node )
    {
        FileListGenerator flg = new FileListGenerator();
        node.accept( flg );
        return flg.getClassPath();
    }

    public boolean visitEnter( DependencyNode node )
    {
        if ( node.getDependency() != null && ( filter == null || filter.accept( node ) ) )
        {
            File file = node.getDependency().getArtifact().getFile();
            if ( file != null )
            {
                files.add( file );
            }
        }

        return true;
    }

    public boolean visitLeave( DependencyNode node )
    {
        return true;
    }

}
