package org.sonatype.aether;

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

import java.util.UUID;

/**
 * A repository backed by an IDE workspace or the output of a build session.
 * 
 * @author Benjamin Bentmann
 */
public final class WorkspaceRepository
    implements ArtifactRepository
{

    private final String type;

    private final Object key;

    public WorkspaceRepository()
    {
        this( "workspace" );
    }

    public WorkspaceRepository( String type )
    {
        this( type, null );
    }

    public WorkspaceRepository( String type, Object key )
    {
        this.type = ( type != null ) ? type : "";
        this.key = ( key != null ) ? key : UUID.randomUUID().toString().replace( "-", "" );
    }

    public String getContentType()
    {
        return type;
    }

    public String getId()
    {
        return "workspace";
    }

    /**
     * Gets the key of this workspace repository. The key is used to distinguish one workspace from another and should
     * be sensitive to the artifacts that are (potentially) available in the workspace.
     * 
     * @return The (comparison) key for this workspace repository, never {@code null}.
     */
    public Object getKey()
    {
        return key;
    }

    @Override
    public String toString()
    {
        return "(" + getContentType() + ")";
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

        return getContentType().equals( that.getContentType() ) && getKey().equals( that.getKey() );
    }

    @Override
    public int hashCode()
    {
        int hash = 17;
        hash = hash * 31 + getKey().hashCode();
        hash = hash * 31 + getContentType().hashCode();
        return hash;
    }

}
