package org.apache.maven.repository;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.util.ArrayList;
import java.util.List;

/**
 * @author Benjamin Bentmann
 */
public class Dependency
{

    private Artifact artifact;

    private String scope = "";

    private boolean optional;

    private List<Exclusion> exclusions = new ArrayList<Exclusion>( 4 );

    public Dependency()
    {
        // enables default constructor
    }

    public Dependency( Artifact artifact, String scope )
    {
        this( artifact, scope, false );
    }

    public Dependency( Artifact artifact, String scope, boolean optional )
    {
        setArtifact( artifact );
        setScope( scope );
        setOptional( optional );
    }

    public Dependency( Dependency original )
    {
        setArtifact( original.getArtifact() );
        setScope( original.getScope() );
        setOptional( original.isOptional() );
        setExclusions( original.getExclusions() );
    }

    public Artifact getArtifact()
    {
        return artifact;
    }

    public Dependency setArtifact( Artifact artifact )
    {
        this.artifact = artifact;
        return this;
    }

    public String getScope()
    {
        return scope;
    }

    public Dependency setScope( String scope )
    {
        this.scope = ( scope != null ) ? scope : "";
        return this;
    }

    public boolean isOptional()
    {
        return optional;
    }

    public Dependency setOptional( boolean optional )
    {
        this.optional = optional;
        return this;
    }

    public List<Exclusion> getExclusions()
    {
        return exclusions;
    }

    public Dependency setExclusions( List<Exclusion> exclusions )
    {
        this.exclusions = ( exclusions != null ) ? exclusions : new ArrayList<Exclusion>( 4 );
        return this;
    }

    public Dependency addExclusion( Exclusion exclusion )
    {
        if ( exclusion == null )
        {
            throw new IllegalArgumentException( "no exclusion specified" );
        }
        this.exclusions.add( exclusion );
        return this;
    }

    @Override
    public String toString()
    {
        return String.valueOf( getArtifact() ) + " (" + getScope() + ( isOptional() ? "?" : "" ) + ")";
    }

}
