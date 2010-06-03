package org.sonatype.maven.repository.util;

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

import org.sonatype.maven.repository.RepositoryEvent;
import org.sonatype.maven.repository.RepositoryListener;

/**
 * A skeleton implementation for custom repository listeners. The callback methods in this class do nothing.
 * 
 * @author Benjamin Bentmann
 */
public abstract class AbstractRepositoryListener
    implements RepositoryListener
{

    public void artifactDeployed( RepositoryEvent event )
    {
    }

    public void artifactDeploying( RepositoryEvent event )
    {
    }

    public void artifactDescriptorInvalid( RepositoryEvent event )
    {
    }

    public void artifactDescriptorMissing( RepositoryEvent event )
    {
    }

    public void artifactInstalled( RepositoryEvent event )
    {
    }

    public void artifactInstalling( RepositoryEvent event )
    {
    }

    public void artifactRelocated( RepositoryEvent event )
    {
    }

    public void artifactResolved( RepositoryEvent event )
    {
    }

    public void artifactResolving( RepositoryEvent event )
    {
    }

    public void metadataDeployed( RepositoryEvent event )
    {
    }

    public void metadataDeploying( RepositoryEvent event )
    {
    }

    public void metadataInstalled( RepositoryEvent event )
    {
    }

    public void metadataInstalling( RepositoryEvent event )
    {
    }

    public void metadataInvalid( RepositoryEvent event )
    {
    }

    public void metadataResolved( RepositoryEvent event )
    {
    }

    public void metadataResolving( RepositoryEvent event )
    {
    }

}
