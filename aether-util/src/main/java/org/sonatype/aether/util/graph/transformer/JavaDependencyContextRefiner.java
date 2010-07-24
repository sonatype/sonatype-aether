package org.sonatype.aether.util.graph.transformer;

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

import org.sonatype.aether.Dependency;
import org.sonatype.aether.DependencyGraphTransformer;
import org.sonatype.aether.DependencyNode;
import org.sonatype.aether.RepositoryException;

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
