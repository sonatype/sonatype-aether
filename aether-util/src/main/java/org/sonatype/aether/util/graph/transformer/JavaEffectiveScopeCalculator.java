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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sonatype.aether.Dependency;
import org.sonatype.aether.DependencyGraphTransformationContext;
import org.sonatype.aether.DependencyGraphTransformer;
import org.sonatype.aether.DependencyNode;
import org.sonatype.aether.RepositoryException;
import org.sonatype.aether.util.JavaScopes;

/**
 * A dependency graph transformer that handles scope inheritance and conflict resolution among conflicting scopes. For a
 * given set of conflicting nodes, the strongest scope will be assigned to all of the nodes. Note: This transformer
 * assumes conflict groups have already been marked by a previous graph transformer like {@link ConflictMarker}.
 * 
 * @author Benjamin Bentmann
 */
public class JavaEffectiveScopeCalculator
    implements DependencyGraphTransformer
{

    public DependencyNode transformGraph( DependencyNode node, DependencyGraphTransformationContext context )
        throws RepositoryException
    {
        Map<?, ?> conflictIds = (Map<?, ?>) context.get( TransformationContextKeys.CONFLICT_IDS );
        if ( conflictIds == null )
        {
            throw new RepositoryException( "conflict groups have not been identified" );
        }

        Map<DependencyNode, Collection<String>> scopes = new IdentityHashMap<DependencyNode, Collection<String>>( 1024 );
        Map<Object, DependencyGroup> groups = new HashMap<Object, DependencyGroup>( 1024 );

        analyze( node, "", scopes, groups, conflictIds );

        Map<DependencyNode, Object> directNodes = new IdentityHashMap<DependencyNode, Object>();
        if ( node.getDependency() == null )
        {
            for ( DependencyNode child : node.getChildren() )
            {
                directNodes.put( child, null );
            }
        }
        else
        {
            directNodes.put( node, null );
        }

        resolve( scopes, groups.values(), directNodes.keySet() );

        return node;
    }

    private void analyze( DependencyNode node, String parentScope, Map<DependencyNode, Collection<String>> scopes,
                          Map<Object, DependencyGroup> groups, Map<?, ?> conflictIds )
    {
        String scope;

        Dependency dependency = node.getDependency();
        if ( dependency != null )
        {
            scope = dependency.getScope();
            if ( node.getPremanagedScope() == null )
            {
                scope = getInheritedScope( parentScope, scope );
            }

            Collection<String> nodeScopes = scopes.get( node );
            if ( nodeScopes == null )
            {
                nodeScopes = new HashSet<String>();
                scopes.put( node, nodeScopes );
            }
            if ( !nodeScopes.add( scope ) )
            {
                return;
            }

            if ( nodeScopes.size() == 1 )
            {
                Object key = conflictIds.get( node );
                DependencyGroup group = groups.get( key );
                if ( group == null )
                {
                    group = new DependencyGroup();
                    groups.put( key, group );
                }
                group.nodes.add( node );
            }
        }
        else
        {
            scope = parentScope;
        }

        for ( DependencyNode childNode : node.getChildren() )
        {
            analyze( childNode, scope, scopes, groups, conflictIds );
        }
    }

    private void resolve( Map<DependencyNode, Collection<String>> scopes, Collection<DependencyGroup> groups,
                          Collection<DependencyNode> directNodes )
    {
        for ( DependencyGroup group : groups )
        {
            String effectiveScope = null;

            Set<String> groupScopes = new HashSet<String>();

            for ( DependencyNode node : group.nodes )
            {
                groupScopes.addAll( scopes.get( node ) );

                if ( directNodes.contains( node ) )
                {
                    effectiveScope = node.getDependency().getScope();
                }
            }

            if ( effectiveScope == null )
            {
                if ( groupScopes.size() > 1 )
                {
                    groupScopes.remove( JavaScopes.SYSTEM );
                }

                if ( groupScopes.size() == 1 )
                {
                    effectiveScope = groupScopes.iterator().next();
                }
                else if ( groupScopes.contains( JavaScopes.COMPILE ) )
                {
                    effectiveScope = JavaScopes.COMPILE;
                }
                else if ( groupScopes.contains( JavaScopes.RUNTIME ) )
                {
                    effectiveScope = JavaScopes.RUNTIME;
                }
                else if ( groupScopes.contains( JavaScopes.PROVIDED ) )
                {
                    effectiveScope = JavaScopes.PROVIDED;
                }
                else if ( groupScopes.contains( JavaScopes.TEST ) )
                {
                    effectiveScope = JavaScopes.TEST;
                }
                else
                {
                    continue;
                }
            }

            for ( DependencyNode node : group.nodes )
            {
                String scope = node.getDependency().getScope();
                if ( !effectiveScope.equals( scope ) && !JavaScopes.SYSTEM.equals( scope ) )
                {
                    node.setScope( effectiveScope );
                }
            }
        }
    }

    private String getInheritedScope( String parentScope, String childScope )
    {
        String result;

        if ( JavaScopes.SYSTEM.equals( childScope ) || JavaScopes.TEST.equals( childScope ) )
        {
            result = childScope;
        }
        else if ( parentScope == null || parentScope.length() <= 0 || JavaScopes.COMPILE.equals( parentScope ) )
        {
            result = childScope;
        }
        else if ( JavaScopes.TEST.equals( parentScope ) || JavaScopes.RUNTIME.equals( parentScope ) )
        {
            result = parentScope;
        }
        else if ( JavaScopes.SYSTEM.equals( parentScope ) || JavaScopes.PROVIDED.equals( parentScope ) )
        {
            result = JavaScopes.PROVIDED;
        }
        else
        {
            result = JavaScopes.RUNTIME;
        }

        return result;
    }

    static class DependencyGroup
    {

        List<DependencyNode> nodes;

        public DependencyGroup()
        {
            nodes = new ArrayList<DependencyNode>();
        }

    }

}
