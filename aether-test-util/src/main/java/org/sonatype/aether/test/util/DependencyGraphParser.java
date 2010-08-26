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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.sonatype.aether.graph.DependencyNode;

/**
 * Creates a dependency tree from a text description. <h2>Definition</h2> The description format is based on 'mvn
 * dependency:tree'. Line format:
 * 
 * <pre>
 * [level](id|[(id)]gid:aid:ext:ver[:scope])[;key=value;key=value;...]
 * </pre>
 * 
 * <h2>Levels</h2> If <code>level</code> is empty, the line defines the root node. Only one root node may be defined.
 * The level is calculated by the distance from the beginning of the line. One level is three characters. A level
 * definition has to follow this format:
 * 
 * <pre>
 * '[| ]*[+\\]- '
 * </pre>
 * 
 * <h2>ID</h2> An ID may be used to reference a previously built node. An ID is of the form:
 * 
 * <pre>
 * '[0-9a-zA-Z]+'
 * </pre>
 * 
 * To define a node with an ID, prefix the definition with an id in parens:
 * 
 * <pre>
 * (id)gid:aid:ext:ver
 * </pre>
 * 
 * To insert a previously defined node into the graph, use a caret followed by the ID:
 * 
 * <pre>
 * ^id
 * </pre>
 * 
 * <h2>Comments</h2> A hash starts a comment. A comment ends with the end of the line. Empty lines are ignored. <h2>
 * Example</h2>
 * 
 * <pre>
 * gid:aid:ext:ver
 * +- gid:aid2:ext:ver:scope
 * |  \- (id1)gid:aid3:ext:ver
 * +- gid:aid4:ext:ver:scope
 * \- ^id1
 * </pre>
 * 
 * @author Benjamin Hanzelmann
 */
public class DependencyGraphParser
{
    private Map<String, DependencyNode> nodes = new HashMap<String, DependencyNode>();
    private String prefix = "";

    /**
     * Parse the given graph definition.
     */
    public DependencyNode parseLiteral( String dependencyGraph )
        throws IOException
    {

        StringReader reader = new StringReader( dependencyGraph );

        return parse( reader );
    }
    
    /**
     * Create a parser with the given prefix.
     * 
     * @see DependencyGraphParser#parse(String)
     */
    public DependencyGraphParser(String prefix)
    {
        this();
        this.prefix = prefix;
    }

    /**
     * Create a parser with an empty prefix.
     */
    public DependencyGraphParser()
    {
        super();
    }

    /**
     * Parse the graph definition read from the given resource.
     * 
     * If a prefix is set, this method will load the resource from 'prefix + resource'.
     */
    public DependencyNode parse( String resource ) throws IOException
    {
        URL res = this.getClass().getClassLoader().getResource( prefix + resource );
        return parse(res);
    }
    
    /**
     * Parse the graph definition read from the given URL.
     */
    public DependencyNode parse( URL resource ) throws IOException
    {
        return parse(new InputStreamReader( resource.openStream(), "UTF-8" ));
    }

    private DependencyNode parse( Reader reader )
        throws IOException
    {
        BufferedReader in = new BufferedReader( reader );

        try
        {
            String line = null;

            DependencyNode root = null;
            DependencyNode node = null;
            int prevLevel = 0;

            LinkedList<DependencyNode> stack = new LinkedList<DependencyNode>();
            boolean isRootNode = true;

            while ( ( line = in.readLine() ) != null )
            {
                line = cutComment( line );

                if ( isEmpty( line ) )
                {
                    // skip empty line
                    continue;
                }

                LineContext ctx = createContext( line );
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

                if ( ctx.getDefinition().isReference() )
                {
                    DependencyNode child = reference( ctx.getDefinition().getReference() );
                    node.getChildren().add( child );
                    node = child;
                }
                else
                {

                    node = build( isRootNode ? null : stack.getLast(), ctx, isRootNode );

                    if ( isRootNode )
                    {
                        root = node;
                        isRootNode = false;
                    }

                    if ( ctx.getDefinition().hasId() )
                    {
                        this.nodes.put( ctx.getDefinition().getId(), node );
                    }
                }
            }

            this.nodes.clear();
            if ( root == null )
            {
                throw new IllegalArgumentException( "No root definition found" );
            }
            
            return root;
        }
        finally
        {
            in.close();
        }

    }

    private DependencyNode reference( String reference )
    {
        if ( !nodes.containsKey( reference ) )
        {
            throw new IllegalArgumentException( "undefined reference " + reference );
        }

        return this.nodes.get( reference );
    }

    private static boolean isEmpty( String line )
    {
        return line == null || line.length() == 0;
    }

    private static String cutComment( String line )
    {
        int idx = line.indexOf( '#' );

        if ( idx != -1 )
        {
            line = line.substring( 0, idx );
        }

        return line;
    }

    private DependencyNode build( DependencyNode parent, LineContext ctx, boolean isRoot )
    {
        Definition def = ctx.getDefinition();
        if ( !isRoot && parent == null )
        {
            throw new IllegalArgumentException( "dangling node: " + def );
        }
        else if ( ctx.getLevel() == 0 && parent != null )
        {
            throw new IllegalArgumentException( "inconsistent leveling (parent for level 0?): " + def );
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

    static class Definition
    {
        private String groupId;

        private String artifactId;

        private String extension;

        private String version;

        private String scope = "";

        private String definition;

        private String id = null;

        private String reference = null;

        public Definition( String def )
        {
            super();

            this.definition = def.trim();

            if ( definition.startsWith( "(" ) )
            {
                int idx = definition.indexOf( ')' );
                this.id = definition.substring( 1, idx );
                this.definition = definition.substring( idx + 1 );
            }
            else if ( definition.startsWith( "^" ) )
            {
                this.reference = definition.substring( 1 );
                return;
            }

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

        public String getId()
        {
            return id;
        }

        public String getReference()
        {
            return reference;
        }

        public boolean isReference()
        {
            return reference != null;
        }

        public boolean hasId()
        {
            return id != null;
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
