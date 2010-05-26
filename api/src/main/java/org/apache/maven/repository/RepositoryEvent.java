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

import java.util.Collection;
import java.util.Collections;

/**
 * @author Benjamin Bentmann
 */
public class RepositoryEvent
{

    private Collection<Artifact> artifacts;

    private Collection<Metadata> metadata;

    public RepositoryEvent( Artifact artifact )
    {
        this.artifacts = Collections.singletonList( artifact );
        this.metadata = Collections.emptyList();
    }

    public RepositoryEvent( Metadata metadata )
    {
        this.artifacts = Collections.emptyList();
        this.metadata = Collections.singletonList( metadata );
    }

    public Collection<Artifact> getArtifacts()
    {
        return artifacts;
    }

    public Collection<Metadata> getMetadata()
    {
        return metadata;
    }

}
