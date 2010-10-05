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

import org.sonatype.aether.RepositoryException;
import org.sonatype.aether.collection.DependencyGraphTransformationContext;
import org.sonatype.aether.collection.DependencyGraphTransformer;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.util.artifact.JavaScopes;

/**
 * A dependency graph transformer that refines the request context for nodes that belong to the "project" context by
 * appending the classpath type to which the node belongs. For instance, a compile-time project dependency will be
 * assigned the request context "project/compile".
 * 
 * @author Benjamin Bentmann
 * @see DependencyNode#getRequestContext()
 */
public class JavaDependencyContextRefiner
    implements DependencyGraphTransformer
{

    public DependencyNode transformGraph( DependencyNode node, DependencyGraphTransformationContext context )
        throws RepositoryException
    {
        String ctx = node.getRequestContext();

        if ( "project".equals( ctx ) )
        {
            String scope = getClasspathScope( node );
            if ( scope != null )
            {
                ctx += '/' + scope;
                node.setRequestContext( ctx );
            }
        }

        for ( DependencyNode child : node.getChildren() )
        {
            transformGraph( child, context );
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

        if ( JavaScopes.COMPILE.equals( scope ) || JavaScopes.SYSTEM.equals( scope )
            || JavaScopes.PROVIDED.equals( scope ) )
        {
            return JavaScopes.COMPILE;
        }
        else if ( JavaScopes.RUNTIME.equals( scope ) )
        {
            return JavaScopes.RUNTIME;
        }
        else if ( JavaScopes.TEST.equals( scope ) )
        {
            return JavaScopes.TEST;
        }

        return null;
    }

}
