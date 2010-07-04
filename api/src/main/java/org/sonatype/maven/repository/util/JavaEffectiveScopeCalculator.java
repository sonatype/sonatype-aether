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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sonatype.maven.repository.Dependency;
import org.sonatype.maven.repository.DependencyGraphTransformer;
import org.sonatype.maven.repository.DependencyNode;

/**
 * @author Benjamin Bentmann
 */
public class JavaEffectiveScopeCalculator
    implements DependencyGraphTransformer
{

    private static final String SYSTEM = "system";

    private static final String PROVIDED = "provided";

    private static final String COMPILE = "compile";

    private static final String RUNTIME = "runtime";

    private static final String TEST = "test";

    public DependencyNode transformGraph( DependencyNode node )
    {
        Map<DependencyNode, String> scopes = new IdentityHashMap<DependencyNode, String>( 16 * 1024 );
        Map<Object, DependencyGroup> groups = new HashMap<Object, DependencyGroup>( 1024 );

        Dependency parent = node.getDependency();
        String parentScope = ( parent != null ) ? parent.getScope() : "";
        analyze( node, parentScope, scopes, groups );

        Set<Object> keys = new HashSet<Object>( groups.keySet() );
        while ( !keys.isEmpty() )
        {
            Iterator<Object> it = keys.iterator();
            Object key = it.next();
            it.remove();

            DependencyGroup group = groups.get( key );
            resolve( group, scopes, keys );
        }

        for ( Map.Entry<DependencyNode, String> entry : scopes.entrySet() )
        {
            entry.getKey().setScope( entry.getValue() );
        }

        return node;
    }

    private void analyze( DependencyNode node, String scope, Map<DependencyNode, String> scopes,
                          Map<Object, DependencyGroup> groups )
    {
        Dependency dependency = node.getDependency();
        if ( dependency != null )
        {
            if ( node.getPremanagedScope() != null )
            {
                scope = dependency.getScope();
            }
            scopes.put( node, scope );

            Object key = node.getConflictId();
            DependencyGroup group = groups.get( key );
            if ( group == null )
            {
                group = new DependencyGroup();
                groups.put( key, group );
            }
            group.nodes.add( node );
        }

        for ( DependencyNode childNode : node.getChildren() )
        {
            Dependency child = childNode.getDependency();
            String childScope = child.getScope();
            String inheritedScope = getInheritedScope( scope, childScope );
            analyze( childNode, inheritedScope, scopes, groups );
        }
    }

    private void resolve( DependencyGroup group, Map<DependencyNode, String> scopes, Set<Object> keys )
    {
        if ( group.nodes.size() <= 1 )
        {
            return;
        }

        String effectiveScope = null;
        Set<String> groupScopes = new HashSet<String>();

        for ( DependencyNode node : group.nodes )
        {
            String scope = scopes.get( node );
            groupScopes.add( scope );
            if ( node.getParent().getDependency() == null )
            {
                effectiveScope = scope;
            }
        }

        if ( groupScopes.size() <= 1 )
        {
            return;
        }

        if ( effectiveScope == null )
        {
            if ( groupScopes.contains( COMPILE ) )
            {
                effectiveScope = COMPILE;
            }
            else if ( groupScopes.contains( SYSTEM ) || groupScopes.contains( PROVIDED ) )
            {
                effectiveScope = groupScopes.contains( RUNTIME ) ? COMPILE : PROVIDED;
            }
            else if ( groupScopes.contains( RUNTIME ) )
            {
                effectiveScope = RUNTIME;
            }
            else
            {
                return;
            }
        }

        for ( DependencyNode node : group.nodes )
        {
            String scope = scopes.get( node );
            if ( !effectiveScope.equals( scope ) && !SYSTEM.equals( scope ) )
            {
                adjust( node, effectiveScope, scopes, keys );
            }
        }
    }

    private void adjust( DependencyNode node, String scope, Map<DependencyNode, String> scopes, Set<Object> keys )
    {
        scopes.put( node, scope );

        for ( DependencyNode childNode : node.getChildren() )
        {
            if ( childNode.getPremanagedScope() == null )
            {
                String childScope = childNode.getDependency().getScope();
                String inheritedScope = getInheritedScope( scope, childScope );
                String oldScope = scopes.get( childNode );
                if ( !inheritedScope.equals( oldScope ) )
                {
                    keys.add( childNode.getConflictId() );
                    adjust( childNode, inheritedScope, scopes, keys );
                }
            }
        }
    }

    private String getInheritedScope( String parentScope, String childScope )
    {
        String result;

        if ( SYSTEM.equals( childScope ) || TEST.equals( childScope ) )
        {
            result = childScope;
        }
        else if ( parentScope.length() <= 0 || COMPILE.equals( parentScope ) )
        {
            result = childScope;
        }
        else if ( TEST.equals( parentScope ) || RUNTIME.equals( parentScope ) )
        {
            result = parentScope;
        }
        else if ( SYSTEM.equals( parentScope ) || PROVIDED.equals( parentScope ) )
        {
            result = PROVIDED;
        }
        else
        {
            result = RUNTIME;
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
