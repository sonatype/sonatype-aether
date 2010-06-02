package org.apache.maven.repo;

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

import java.util.Collection;
import java.util.Collections;

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
