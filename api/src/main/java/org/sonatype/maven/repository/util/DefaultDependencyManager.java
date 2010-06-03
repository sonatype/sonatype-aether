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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sonatype.maven.repository.Artifact;
import org.sonatype.maven.repository.Dependency;
import org.sonatype.maven.repository.DependencyManager;
import org.sonatype.maven.repository.DependencyNode;

/**
 * A dependency manager that overrides dependency version and scope for managed dependencies.
 * 
 * @author Benjamin Bentmann
 */
public class DefaultDependencyManager
    implements DependencyManager
{

    private final Map<String, String> managedVersions;

    private final Map<String, String> managedScopes;

    /**
     * Creates a new dependency manager without any management information.
     */
    public DefaultDependencyManager()
    {
        this( Collections.<String, String> emptyMap(), Collections.<String, String> emptyMap() );
    }

    private DefaultDependencyManager( Map<String, String> managedVersions, Map<String, String> managedScopes )
    {
        this.managedVersions = managedVersions;
        this.managedScopes = managedScopes;
    }

    public DependencyManager deriveChildManager( DependencyNode childNode,
                                                 List<? extends Dependency> managedDependencies )
    {
        if ( managedDependencies == null || managedDependencies.isEmpty() )
        {
            return this;
        }

        Map<String, String> managedVersions = new HashMap<String, String>( this.managedVersions );
        Map<String, String> managedScopes = new HashMap<String, String>( this.managedScopes );

        for ( Dependency managedDependency : managedDependencies )
        {
            Artifact artifact = managedDependency.getArtifact();
            String key = getKey( artifact );

            if ( artifact.getVersion().length() > 0 && !managedVersions.containsKey( key ) )
            {
                managedVersions.put( key, artifact.getVersion() );
            }
            if ( managedDependency.getScope().length() > 0 && !managedScopes.containsKey( key ) )
            {
                managedScopes.put( key, managedDependency.getScope() );
            }
        }

        return new DefaultDependencyManager( managedVersions, managedScopes );
    }

    public void manageDependency( DependencyNode node, Dependency dependency )
    {
        String key = getKey( dependency.getArtifact() );

        String version = managedVersions.get( key );
        if ( version != null )
        {
            dependency.getArtifact().setVersion( version );
        }

        String scope = managedScopes.get( key );
        if ( scope != null )
        {
            dependency.setScope( scope );
        }
    }

    private String getKey( Artifact a )
    {
        return a.getGroupId() + ':' + a.getArtifactId() + ':' + a.getType() + ':' + a.getClassifier();
    }

}
