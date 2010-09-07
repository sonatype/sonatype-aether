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
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sonatype.aether.RepositoryException;
import org.sonatype.aether.collection.DependencyGraphTransformationContext;
import org.sonatype.aether.collection.DependencyGraphTransformer;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.util.artifact.JavaScopes;

/**
 * A dependency graph transformer that handles scope inheritance and conflict resolution among conflicting scopes as
 * seen in Maven 2.x. For a given set of conflicting nodes, a single scope will be chosen and assigned to all of the
 * nodes. This transformer will query the keys {@link TransformationContextKeys#CONFLICT_IDS} and
 * {@link TransformationContextKeys#SORTED_CONFLICT_IDS} for existing information about conflict ids. In absence of this
 * information, it will automatically invoke the {@link ConflictIdSorter} to calculate it.
 * 
 * @author Benjamin Bentmann
 */
public class JavaEffectiveScopeCalculator
    implements DependencyGraphTransformer
{

    public DependencyNode transformGraph( DependencyNode node, DependencyGraphTransformationContext context )
        throws RepositoryException
    {
        List<?> sortedConflictIds = (List<?>) context.get( TransformationContextKeys.SORTED_CONFLICT_IDS );
        if ( sortedConflictIds == null )
        {
            ConflictIdSorter sorter = new ConflictIdSorter();
            sorter.transformGraph( node, context );

            sortedConflictIds = (List<?>) context.get( TransformationContextKeys.SORTED_CONFLICT_IDS );
        }

        Map<?, ?> conflictIds = (Map<?, ?>) context.get( TransformationContextKeys.CONFLICT_IDS );
        if ( conflictIds == null )
        {
            throw new RepositoryException( "conflict groups have not been identified" );
        }

        Map<Object, ConflictGroup> groups = new HashMap<Object, ConflictGroup>();

        buildConflictGroups( groups, node, null, conflictIds );

        String rootScope = "";
        if ( node.getDependency() != null )
        {
            Object key = conflictIds.get( node );
            groups.get( key ).scope = rootScope = node.getDependency().getScope();
        }
        for ( DependencyNode child : node.getChildren() )
        {
            Object key = conflictIds.get( child );
            groups.get( key ).scope = getInheritedScope( rootScope, child.getDependency().getScope() );
        }

        for ( Object key : sortedConflictIds )
        {
            ConflictGroup group = groups.get( key );
            resolve( group );
        }

        return node;
    }

    private void buildConflictGroups( Map<Object, ConflictGroup> groups, DependencyNode node, DependencyNode parent,
                                      Map<?, ?> conflictIds )
    {
        Object key = conflictIds.get( node );

        ConflictGroup group = groups.get( key );
        if ( group == null )
        {
            group = new ConflictGroup( key );
            groups.put( key, group );
        }

        List<DependencyNode> parents = group.parents.get( node );
        boolean visited = parents != null;

        if ( parents == null )
        {
            parents = new ArrayList<DependencyNode>( 4 );
            group.parents.put( node, parents );
        }

        if ( parent != null )
        {
            parents.add( parent );
        }

        if ( !visited )
        {
            parent = ( node.getDependency() != null ) ? node : null;
            for ( DependencyNode child : node.getChildren() )
            {
                buildConflictGroups( groups, child, parent, conflictIds );
            }
        }
    }

    private void resolve( ConflictGroup group )
    {
        if ( group.scope == null )
        {
            Set<String> inheritedScopes = getInheritedScopes( group );
            group.scope = chooseEffectiveScope( inheritedScopes );
        }

        for ( DependencyNode node : group.parents.keySet() )
        {
            if ( node.getPremanagedScope() == null )
            {
                String scope = node.getDependency().getScope();
                if ( !group.scope.equals( scope ) && !JavaScopes.SYSTEM.equals( scope ) )
                {
                    node.setScope( group.scope );
                }
            }
        }
    }

    private Set<String> getInheritedScopes( ConflictGroup group )
    {
        Set<String> inheritedScopes = new HashSet<String>();

        for ( Map.Entry<DependencyNode, List<DependencyNode>> entry : group.parents.entrySet() )
        {
            String childScope = entry.getKey().getDependency().getScope();

            if ( entry.getValue().isEmpty() )
            {
                inheritedScopes.add( childScope );
            }
            else
            {
                for ( DependencyNode parent : entry.getValue() )
                {
                    String parentScope = parent.getDependency().getScope();
                    String inheritedScope = getInheritedScope( parentScope, childScope );
                    inheritedScopes.add( inheritedScope );
                }
            }
        }

        return inheritedScopes;
    }

    private String getInheritedScope( String parentScope, String childScope )
    {
        String inheritedScope;

        if ( JavaScopes.SYSTEM.equals( childScope ) || JavaScopes.TEST.equals( childScope ) )
        {
            inheritedScope = childScope;
        }
        else if ( parentScope == null || parentScope.length() <= 0 || JavaScopes.COMPILE.equals( parentScope ) )
        {
            inheritedScope = childScope;
        }
        else if ( JavaScopes.TEST.equals( parentScope ) || JavaScopes.RUNTIME.equals( parentScope ) )
        {
            inheritedScope = parentScope;
        }
        else if ( JavaScopes.SYSTEM.equals( parentScope ) || JavaScopes.PROVIDED.equals( parentScope ) )
        {
            inheritedScope = JavaScopes.PROVIDED;
        }
        else
        {
            inheritedScope = JavaScopes.RUNTIME;
        }

        return inheritedScope;
    }

    private String chooseEffectiveScope( Set<String> scopes )
    {
        if ( scopes.size() > 1 )
        {
            scopes.remove( JavaScopes.SYSTEM );
        }

        String effectiveScope = "";

        if ( scopes.size() == 1 )
        {
            effectiveScope = scopes.iterator().next();
        }
        else if ( scopes.contains( JavaScopes.COMPILE ) )
        {
            effectiveScope = JavaScopes.COMPILE;
        }
        else if ( scopes.contains( JavaScopes.RUNTIME ) )
        {
            effectiveScope = JavaScopes.RUNTIME;
        }
        else if ( scopes.contains( JavaScopes.PROVIDED ) )
        {
            effectiveScope = JavaScopes.PROVIDED;
        }
        else if ( scopes.contains( JavaScopes.TEST ) )
        {
            effectiveScope = JavaScopes.TEST;
        }

        return effectiveScope;
    }

    static final class ConflictGroup
    {

        final Object key;

        final Map<DependencyNode, List<DependencyNode>> parents;

        String scope;

        public ConflictGroup( Object key )
        {
            this.key = key;
            this.parents = new IdentityHashMap<DependencyNode, List<DependencyNode>>();
        }

        @Override
        public boolean equals( Object obj )
        {
            if ( this == obj )
            {
                return true;
            }
            else if ( !( obj instanceof ConflictGroup ) )
            {
                return false;
            }
            ConflictGroup that = (ConflictGroup) obj;
            return this.key.equals( that.key );
        }

        @Override
        public int hashCode()
        {
            return key.hashCode();
        }

        @Override
        public String toString()
        {
            return String.valueOf( key );
        }
    }

}
