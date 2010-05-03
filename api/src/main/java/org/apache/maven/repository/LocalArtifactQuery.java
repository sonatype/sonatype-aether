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

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * @author Benjamin Bentmann
 */
public class LocalArtifactQuery
{

    private Artifact artifact;

    private List<? extends RemoteRepository> repositories;

    private File file;

    private boolean available;

    public LocalArtifactQuery()
    {
        // enables default constructor
    }

    public LocalArtifactQuery( Artifact artifact, List<? extends RemoteRepository> repositories )
    {
        setArtifact( artifact );
        setRepositories( repositories );
    }

    public Artifact getArtifact()
    {
        return artifact;
    }

    public LocalArtifactQuery setArtifact( Artifact artifact )
    {
        this.artifact = artifact;
        return this;
    }

    public LocalArtifactQuery setRepositories( List<? extends RemoteRepository> repositories )
    {
        this.repositories = ( repositories != null ) ? repositories : Collections.<RemoteRepository> emptyList();
        return this;
    }

    public List<? extends RemoteRepository> getRepositories()
    {
        return repositories;
    }

    public File getFile()
    {
        return file;
    }

    public LocalArtifactQuery setFile( File file )
    {
        this.file = file;
        return this;
    }

    public boolean isAvailable()
    {
        return available;
    }

    public LocalArtifactQuery setAvailable( boolean available )
    {
        this.available = available;
        return this;
    }

}
