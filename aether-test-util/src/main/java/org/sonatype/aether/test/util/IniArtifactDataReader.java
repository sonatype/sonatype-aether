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
package org.sonatype.aether.test.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.graph.Exclusion;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.test.util.impl.StubArtifact;

/**
 * @author Benjamin Hanzelmann
 */
public class IniArtifactDataReader
{

    private String prefix = "";

    public IniArtifactDataReader()
    {
        this( "" );
    }

    public IniArtifactDataReader( String prefix )
    {
        this.prefix = prefix;

    }

    public ArtifactDescription parse( String resource )
        throws IOException
    {
        URL res = this.getClass().getClassLoader().getResource( prefix + resource );

        if ( res == null )
        {
            throw new IllegalArgumentException( "cannot find resource: " + resource );
        }
        return parse( res );
    }

    public ArtifactDescription parse( URL res )
        throws IOException
    {
        return parse( new InputStreamReader( res.openStream(), "UTF-8" ) );
    }

    public ArtifactDescription parseLiteral( String description )
        throws IOException
    {
        StringReader reader = new StringReader( description );
        return parse( reader );
    }

    private enum State
    {
        NONE, RELOCATIONS, DEPENDENCIES, MANAGEDDEPENDENCIES, REPOSITORIES
    }

    private ArtifactDescription parse( Reader reader )
        throws IOException
    {

        String line = null;

        BufferedReader in = new BufferedReader( reader );

        State state = State.NONE;

        Map<State, List<String>> sections = new HashMap<State, List<String>>();

        while ( ( line = in.readLine() ) != null )
        {

            line = cutComment( line );
            if ( isEmpty( line ) )
            {
                continue;
            }

            if ( line.startsWith( "[" ) )
            {
                state = State.valueOf( line.substring( 1, line.length() - 1 ).toUpperCase( Locale.ENGLISH ) );
                sections.put( state, new ArrayList<String>() );
            }
            else
            {
                sections.get( state ).add( line.trim() );
            }
        }

        List<Artifact> relocations = relocations( sections.get( State.RELOCATIONS ) );
        List<Dependency> dependencies = dependencies( sections.get( State.DEPENDENCIES ) );
        List<Dependency> managedDependencies = dependencies( sections.get( State.MANAGEDDEPENDENCIES ) );
        List<RemoteRepository> repositories = repositories( sections.get( State.REPOSITORIES ) );

        ArtifactDescription description =
            new ArtifactDescription( relocations, dependencies, managedDependencies, repositories );
        return description;
    }

    /**
     * @param list
     * @return
     */
    private List<RemoteRepository> repositories( List<String> list )
    {
        ArrayList<RemoteRepository> ret = new ArrayList<RemoteRepository>();
        if ( list == null )
        {
            return ret;
        }
        for ( String coords : list )
        {
            String[] split = coords.split( ":", 3 );
            String id = split[0];
            String type = split[1];
            String url = split[2];

            ret.add( new RemoteRepository( id, type, url ) );
        }
        return ret;
    }

    /**
     * @param list
     * @return
     */
    private List<Dependency> dependencies( List<String> list )
    {
        List<Dependency> ret = new ArrayList<Dependency>();
        if ( list == null )
        {
            return ret;
        }

        Collection<Exclusion> exclusions = new ArrayList<Exclusion>();

        boolean optional = false;
        Artifact artifact = null;
        String scope = null;

        for ( String coords : list )
        {
            if ( coords.startsWith( "-" ) )
            {
                coords = coords.substring( 1 );
                String[] split = coords.split( ":" );
                exclusions.add( new Exclusion( split[0], split[1], "", "" ) );
            }
            else
            {
                if ( artifact != null )
                {
                    // commit dependency
                    Dependency dep = new Dependency( artifact, scope, optional, exclusions );
                    ret.add( dep );

                    exclusions = new ArrayList<Exclusion>();
                }

                optional = coords.endsWith( "optional" );
                if ( optional )
                {
                    coords.substring( 0, coords.length() - ":optional".length() );
                }

                String[] split = coords.split( ":" );

                scope = "compile";
                if ( split.length >= 5 )
                {
                    scope = split[4];
                }
                coords = coords.replace( ":" + scope, "" );

                artifact = new StubArtifact( coords );
            }
        }
            if ( artifact != null )
            {
                // commit dependency
                Dependency dep = new Dependency( artifact, scope, optional, exclusions );
                ret.add( dep );

                exclusions = new ArrayList<Exclusion>();
            }

        return ret;
    }

    private List<Artifact> relocations( List<String> list )
    {
        List<Artifact> ret = new ArrayList<Artifact>();
        if ( list == null )
        {
            return ret;
        }
        for ( String coords : list )
        {
            ret.add( new StubArtifact( coords ) );
        }
        return ret;
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

        private boolean optional = false;

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
            if ( split.length > 5 && "true".equalsIgnoreCase( split[5] ) )
            {
                optional = true;
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

        public String getType()
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

        public boolean isOptional()
        {
            return optional;
        }
    }

}
