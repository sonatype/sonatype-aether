package org.sonatype.aether.test.util;

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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.sonatype.aether.graph.DependencyNode;

/**
 * Creates a dependency tree from a text description. The description format is based on 'mvn dependency:tree'. Line
 * format:
 * 
 * <pre>
 * [level]gid:aid:ext:ver[:scope][;key=value;key=value;...]
 * </pre>
 * 
 * If <code>level</code> is empty, the line defines the root node. Only one root node may be defined. A level definition
 * has to follow this format:
 * 
 * <pre>
 * '[| ]*[+\\]- '
 * </pre>
 * 
 * The level is then calculated by the distance from the beginning of the line. One level is three characters. Example:
 * 
 * <pre>
 * gid:aid:ext:ver
 * +- gid:aid2:ext:ver:scope
 * |  \- gid:aid3:ext:ver
 * \- gid:aid4:ext:ver:scope
 * </pre>
 * 
 * @author Benjamin Hanzelmann
 */
public class DependencyGraphParser
{
    public static DependencyNode parse( String dependencyTree )
        throws TreeParseException
    {

        StringReader reader = new StringReader( dependencyTree );

        return parse( reader );
    }

    public static DependencyNode parse( File definition )
        throws TreeParseException
    {

        try
        {
            Reader reader = new FileReader( definition );
            return parse( reader );
        }
        catch ( IOException e )
        {
            throw new TreeParseException( "could not read definition: " + e.getMessage(), e );
        }

    }

    public static DependencyNode parse( Reader reader )
        throws TreeParseException
    {
        try
        {
            BufferedReader in = new BufferedReader( reader );

            String line = in.readLine();
            LineContext ctx = createContext( line );
            DependencyNode root = build( null, ctx );

            DependencyNode node = root;
            int prevLevel = 0;

            LinkedList<DependencyNode> stack = new LinkedList<DependencyNode>();
            // stack.add( root );

            while ( ( line = in.readLine() ) != null )
            {
                ctx = createContext( line );
                if ( prevLevel < ctx.getLevel() )
                {
                    // previous node is new parent
                    stack.add( node );
                }

                // get to real parent
                while ( prevLevel > ctx.getLevel() )
                {
                    stack.removeLast();
                    prevLevel -= 1;
                }

                prevLevel = ctx.getLevel();
                node = build( stack.getLast(), ctx );
            }
            return root;
        }
        catch ( IOException e )
        {
            throw new TreeParseException( "Could not read definition: " + e.getMessage(), e );
        }

    }

    /**
     * @param object
     * @param ctx
     * @return
     * @throws TreeParseException
     */
    private static DependencyNode build( DependencyNode parent, LineContext ctx )
        throws TreeParseException
    {
        Definition def = ctx.getDefinition();
        if ( ctx.getLevel() != 0 && parent == null )
        {
            // another root? no, thanks
            throw new TreeParseException( "dangling node: " + def );
        }
        else if ( ctx.getLevel() == 0 && parent != null )
        {
            throw new TreeParseException( "inconsistent leveling (parent for level 0?): " + def );
        }

        NodeBuilder builder = new NodeBuilder();

        builder.artifactId( def.getArtifactId() ).groupId( def.getGroupId() ).ext( def.getExtension() ).version( def.getVersion() ).scope( def.getScope() );
        DependencyNode node = builder.build();

        if ( parent != null )
        {
            parent.getChildren().add( node );
        }

        return node;
    }

    private static LineContext createContext( String line )
    {
        LineContext ctx = new LineContext();
        String definition;

        String[] split = line.split( "- " );
        if ( split.length == 1 ) // root
        {
            ctx.setLevel( 0 );
            definition = split[0];
        }
        else
        {
            ctx.setLevel( (int) Math.ceil( (double) split[0].length() / (double) 3 ) );
            definition = split[1];
        }

        split = definition.split( ";" );
        ctx.setDefinition( new Definition( split[0] ) );

        if ( split.length > 1 ) // properties
        {
            Map<String, String> props = new HashMap<String, String>();
            for ( int i = 1; i < split.length; i++ )
            {
                String[] keyValue = split[i].split( "=" );
                String key = keyValue[0];
                String value = keyValue[1];
                props.put( key, value );
            }
            ctx.setProperties( props );
        }

        return ctx;
    }

    /**
     * @author Benjamin Hanzelmann
     */
    public static class TreeParseException
        extends Exception
    {

        public TreeParseException( String message, Throwable cause )
        {
            super( message, cause );
        }

        public TreeParseException( String message )
        {
            super( message );
        }

    }

    static class Definition
    {
        private String groupId;

        private String artifactId;

        private String extension;

        private String version;

        private String scope = "";

        private String definition;

        public Definition( String definition )
        {
            super();

            this.definition = definition;

            String[] split = definition.split( ":" );
            if ( split.length < 4 )
            {
                throw new IllegalArgumentException( "Need definition like 'gid:aid:ext:ver[:scope]', but was: "
                    + definition );
            }
            groupId = split[0];
            artifactId = split[1];
            extension = split[2];
            version = split[3];
            if ( split.length > 4 )
            {
                scope = split[4];
            }
        }

        public String getGroupId()
        {
            return groupId;
        }

        public String getArtifactId()
        {
            return artifactId;
        }

        public String getExtension()
        {
            return extension;
        }

        public String getVersion()
        {
            return version;
        }

        public String getScope()
        {
            return scope;
        }

        @Override
        public String toString()
        {
            return definition;
        }
    }

    static class LineContext
    {
        Definition definition;

        Map<String, String> properties;

        int level;

        public Definition getDefinition()
        {
            return definition;
        }

        public void setDefinition( Definition definition )
        {
            this.definition = definition;
        }

        public Map<String, String> getProperties()
        {
            return properties;
        }

        public void setProperties( Map<String, String> properties )
        {
            this.properties = properties;
        }

        public int getLevel()
        {
            return level;
        }

        public void setLevel( int level )
        {
            this.level = level;
        }
    }

}
