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

import org.sonatype.aether.RepositoryEvent;
import org.sonatype.aether.RepositoryListener;

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
