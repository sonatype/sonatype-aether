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

/**
 * A repository on the local file system.
 * 
 * @author Benjamin Bentmann
 */
public class LocalRepository
    implements ArtifactRepository
{

    private File basedir;

    private String type;

    /**
     * Creates a new local repository with the specified properties.
     * 
     * @param basedir The base directory of the repository, may be {@code null}.
     * @param type The type of the repository, may be {@code null}.
     */
    public LocalRepository( File basedir, String type )
    {
        setBasedir( basedir );
        setType( type );
    }

    public String getType()
    {
        return type;
    }

    private LocalRepository setType( String type )
    {
        this.type = ( type != null ) ? type : "";
        return this;
    }

    public String getId()
    {
        return "local";
    }

    public File getBasedir()
    {
        return basedir;
    }

    private LocalRepository setBasedir( File basedir )
    {
        this.basedir = basedir;
        return this;
    }

    @Override
    public String toString()
    {
        return getBasedir().getAbsolutePath() + " (" + getType() + ")";
    }

}
