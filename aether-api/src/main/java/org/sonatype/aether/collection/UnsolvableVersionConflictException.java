package org.sonatype.aether.collection;

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
        super( "Could not resolve version conflict for " + dependencyConflictId + " with requested versions: "
            + versions );
        this.dependencyConflictId = ( dependencyConflictId != null ) ? dependencyConflictId : "";
        this.versions = ( versions != null ) ? versions : Collections.<String> emptyList();
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
