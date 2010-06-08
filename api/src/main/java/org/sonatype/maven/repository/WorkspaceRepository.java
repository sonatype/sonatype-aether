package org.sonatype.maven.repository;

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

import java.util.UUID;

/**
 * A repository backed by an IDE workspace or a build session.
 * 
 * @author Benjamin Bentmann
 */
public class WorkspaceRepository
    implements ArtifactRepository
{

    private final String type;

    private final String uid;

    public WorkspaceRepository()
    {
        this( "workspace" );
    }

    public WorkspaceRepository( String type )
    {
        this( type, null );
    }

    public WorkspaceRepository( String type, String uid )
    {
        this.type = ( type != null ) ? type : "";
        this.uid = ( uid != null ) ? uid : UUID.randomUUID().toString().replace( "-", "" );
    }

    public String getType()
    {
        return type;
    }

    public String getId()
    {
        return "workspace";
    }

    @Override
    public String toString()
    {
        return "(" + getType() + ")";
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj )
        {
            return true;
        }
        if ( obj == null || !getClass().equals( obj.getClass() ) )
        {
            return false;
        }

        WorkspaceRepository that = (WorkspaceRepository) obj;

        return this.getType().equals( that.getType() ) && this.uid.equals( that.uid );
    }

    @Override
    public int hashCode()
    {
        int hash = 17;
        hash = hash * 31 + uid.hashCode();
        hash = hash * 31 + getType().hashCode();
        return hash;
    }

}
