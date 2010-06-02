package org.apache.maven.repo.util;

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

import org.apache.maven.repo.Dependency;
import org.apache.maven.repo.DependencyGraphTransformer;
import org.apache.maven.repo.DependencyNode;
import org.apache.maven.repo.RepositoryException;

/**
 * @author Benjamin Bentmann
 */
public class JavaDependencyContextRefiner
    implements DependencyGraphTransformer
{

    public DependencyNode transformGraph( DependencyNode node )
        throws RepositoryException
    {
        String context = node.getContext();
        if ( "project".equals( context ) )
        {
            String scope = getClasspathScope( node );
            if ( scope != null )
            {
                context += '/' + scope;
                node.setContext( context );
            }
        }

        for ( DependencyNode child : node.getChildren() )
        {
            transformGraph( child );
        }

        return node;
    }

    private String getClasspathScope( DependencyNode node )
    {
        Dependency dependency = node.getDependency();
        if ( dependency == null )
        {
            return null;
        }
        String scope = dependency.getScope();
        if ( "compile".equals( scope ) || "system".equals( scope ) || "provided".equals( scope ) )
        {
            return "compile";
        }
        else if ( "runtime".equals( scope ) )
        {
            return "runtime";
        }
        else if ( "test".equals( scope ) )
        {
            return "test";
        }
        return null;
    }

}
