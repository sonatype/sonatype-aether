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

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;

import org.ini4j.Ini;
import org.ini4j.Profile.Section;

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

    private ArtifactDescription parse( Reader reader )
        throws IOException
    {
        ArtifactDescription description = new ArtifactDescription();

        ArtifactData self = new ArtifactData();

        Ini ini = new Ini( reader );
        
        // self section is mandatory
        ini.get( "self" ).to( self );
        description.setSelfData( self );

        ArtifactData parent = new ArtifactData();
        loadSection( ini, "parent", parent);
        description.setParentData( parent );

        DependencyData dependency = new DependencyData();
        loadSection( ini, "dependencies", dependency );
        description.setDependencyData( dependency );
        
        return description;
    }

    private void loadSection( Ini ini, String section, Object bean )
    {
        Section parentSection = ini.get( section );
        if ( parentSection != null )
        {
            parentSection.to( bean );
        }
    }

    public static class ArtifactDescription
    {
        private ArtifactData selfData;

        private ArtifactData parentData;
        
        private DependencyData dependencyData;

        public ArtifactData getSelfData()
        {
            return selfData;
        }

        public void setSelfData( ArtifactData selfData )
        {
            this.selfData = selfData;
        }

        public ArtifactData getParentData()
        {
            return parentData;
        }

        public void setParentData( ArtifactData parentData )
        {
            this.parentData = parentData;
        }

        public DependencyData getDependencyData()
        {
            return dependencyData;
        }

        public void setDependencyData( DependencyData dependencyData )
        {
            this.dependencyData = dependencyData;
        }

    }

    public static class ArtifactData
    {
        private String artifactId;

        private String groupId;

        private String version;

        private String packaging;

        public String getArtifactId()
        {
            return artifactId;
        }

        public void setArtifactId( String artifactId )
        {
            this.artifactId = artifactId;
        }

        public String getGroupId()
        {
            return groupId;
        }

        public void setGroupId( String groupId )
        {
            this.groupId = groupId;
        }

        public String getVersion()
        {
            return version;
        }

        public void setVersion( String version )
        {
            this.version = version;
        }

        public String getPackaging()
        {
            return packaging;
        }

        public void setPackaging( String packaging )
        {
            this.packaging = packaging;
        }

    }
    
    public static class Dependency
    {
        private String artifactId;

        private String groupId;

        private String version;

        private String scope;

        private String type;

        private boolean optional;

        public String getArtifactId()
        {
            return artifactId;
        }

        public void setArtifactId( String artifactId )
        {
            this.artifactId = artifactId;
        }

        public String getGroupId()
        {
            return groupId;
        }

        public void setGroupId( String groupId )
        {
            this.groupId = groupId;
        }

        public String getVersion()
        {
            return version;
        }

        public void setVersion( String version )
        {
            this.version = version;
        }

        public String getScope()
        {
            return scope;
        }

        public void setScope( String scope )
        {
            this.scope = scope;
        }

        public String getType()
        {
            return type;
        }

        public void setType( String type )
        {
            this.type = type;
        }

        public boolean isOptional()
        {
            return optional;
        }

        public void setOptional( boolean optional )
        {
            this.optional = optional;
        }

    }

    public static class DependencyData
    {
        private String[] dependency;

        private ArrayList<Dependency> dependencies;

        public void setDependency( String[] dependency )
        {
            this.dependency = dependency;
            dependencies = null;
        }
        
        public ArrayList<Dependency> getDependencies()
        {
            if ( dependencies == null )
            {
                convertDependencies();
            }
            return dependencies;
        }

        private void convertDependencies()
        {
            dependencies = new ArrayList<Dependency>();
            for ( int i = 0; i < dependency.length; i++ )
            {
                Definition def = new Definition( dependency[i] );
                Dependency dep = new Dependency();
                dep.setArtifactId( def.getArtifactId() );
                dep.setGroupId( def.getGroupId() );
                dep.setVersion( def.getVersion() );
                dep.setType( def.getType() );
                dep.setScope( def.getScope() );

                dep.setOptional( def.isOptional() );

                dependencies.add( dep );
            }
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
