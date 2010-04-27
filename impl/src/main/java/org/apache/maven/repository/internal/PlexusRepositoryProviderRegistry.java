package org.apache.maven.repository.internal;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.maven.repository.LocalRepositoryManager;
import org.apache.maven.repository.PluggableComponent;
import org.apache.maven.repository.RepositoryProviderRegistry;
import org.apache.maven.repository.RepositoryReaderFactory;
import org.apache.maven.repository.WorkspaceReader;

/**
 * @author Benjamin Bentmann
 * @plexus.component role="org.apache.maven.repository.RepositoryProviderRegistry" role-hint="default"
 */
public class PlexusRepositoryProviderRegistry
    implements RepositoryProviderRegistry
{

    /**
     * @plexus.requirement role="org.apache.maven.repository.LocalRepositoryManager"
     */
    private LocalRepositoryManager localRepoManager;

    /**
     * @plexus.requirement role="org.apache.maven.repository.WorkspaceReader" optional="true"
     */
    private WorkspaceReader workspaceReader;

    /**
     * @plexus.requirement role="org.apache.maven.repository.RepositoryReaderFactory"
     */
    private List<RepositoryReaderFactory> readerFactories;

    public LocalRepositoryManager getLocalRepositoryManager()
    {
        return localRepoManager;
    }

    public WorkspaceReader getWorkspaceReader()
    {
        return workspaceReader;
    }

    public List<? extends RepositoryReaderFactory> getReaderFactories()
    {
        List<RepositoryReaderFactory> result = new ArrayList<RepositoryReaderFactory>( readerFactories );
        Collections.sort( result, PluggableComponent.COMPARATOR );
        return result;
    }

}
