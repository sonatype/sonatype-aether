package org.sonatype.aether.collection;

/*******************************************************************************
 * Copyright (c) 2010 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import java.util.Collection;
import java.util.Collections;

import org.sonatype.aether.RepositoryException;

/**
 * @author Benjamin Bentmann
 */
public class UnsolvableVersionConflictException
    extends RepositoryException
{

    private final Object dependencyConflictId;

    private final Collection<String> versions;

    public UnsolvableVersionConflictException( Object dependencyConflictId, Collection<String> versions )
    {
        super( "Could not resolve version conflict for " + dependencyConflictId + " with requested versions "
            + toList( versions ) );
        this.dependencyConflictId = ( dependencyConflictId != null ) ? dependencyConflictId : "";
        this.versions = ( versions != null ) ? versions : Collections.<String> emptyList();
    }

    public UnsolvableVersionConflictException( Object dependencyConflictId, Collection<String> versions, String customMessage )
    {
        super( "Could not resolve version conflict for " + dependencyConflictId + " with requested versions "
            + toList( versions ) + ", details: " + customMessage);
        this.dependencyConflictId = ( dependencyConflictId != null ) ? dependencyConflictId : "";
        this.versions = ( versions != null ) ? versions : Collections.<String> emptyList();
    }

    private static String toList( Collection<String> versions )
    {
        StringBuilder buffer = new StringBuilder( 256 );
        if ( versions != null )
        {
            for ( String version : versions )
            {
                if ( buffer.length() > 0 )
                {
                    buffer.append( ", " );
                }
                buffer.append( version );
            }
        }
        return buffer.toString();
    }

    public Object getDependencyConflictId()
    {
        return dependencyConflictId;
    }

    public Collection<String> getVersions()
    {
        return versions;
    }

}
