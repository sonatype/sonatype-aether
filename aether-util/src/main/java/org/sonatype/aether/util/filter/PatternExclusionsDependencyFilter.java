package org.sonatype.aether.util.filter;

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

import java.util.Collection;

import org.sonatype.aether.Dependency;
import org.sonatype.aether.DependencyNode;
import org.sonatype.aether.VersionScheme;

/**
 * A simple filter to exclude artifacts from a list of patterns. The artifact pattern syntax is of the form:
 * 
 * <pre>
 * [groupId]:[artifactId]:[extension]:[version]
 * </pre>
 * <p>
 * Where each pattern segment is optional and supports full and partial <code>*</code> wildcards. An empty pattern
 * segment is treated as an implicit wildcard. Version can be a range in case a {@link VersionScheme} is specified.
 * </p>
 * <p>
 * For example, <code>org.apache.*</code> would match all artifacts whose group id started with <code>org.apache.</code>
 * , and <code>:::*-SNAPSHOT</code> would match all snapshot artifacts.
 * </p>
 * 
 * @author Alin Dreghiciu
 */
public class PatternExclusionsDependencyFilter
    extends PatternInclusionsDependencyFilter
{

    /**
     * Creates a new filter using the specified patterns.
     * 
     * @param patterns The exclude patterns, may be {@code null} or empty to exclude no artifacts.
     */
    public PatternExclusionsDependencyFilter( final String... patterns )
    {
        super( patterns );
    }

    /**
     * Creates a new filter using the specified patterns.
     * 
     * @param versionScheme To be used for parsing versions/version ranges. If {@code null} and pattern specifies a
     *            range no artifact will be excluded.
     * @param patterns The exclude patterns, may be {@code null} or empty to exclude no artifacts.
     */
    public PatternExclusionsDependencyFilter( final VersionScheme versionScheme, final String... patterns )
    {
        super( versionScheme, patterns );
    }

    /**
     * Creates a new filter using the specified patterns.
     * 
     * @param patterns The include patterns, may be {@code null} or empty to include no artifacts.
     */
    public PatternExclusionsDependencyFilter( final Collection<String> patterns )
    {
        super( patterns );
    }

    /**
     * Creates a new filter using the specified patterns and {@link VersionScheme} .
     * 
     * @param versionScheme To be used for parsing versions/version ranges. If {@code null} and pattern specifies a
     *            range no artifact will be excluded.
     * @param patterns The exclude patterns, may be {@code null} or empty to exclude no artifacts.
     */
    public PatternExclusionsDependencyFilter( final VersionScheme versionScheme, final Collection<String> patterns )
    {
        super( versionScheme, patterns );
    }

    @Override
    public boolean accept( final DependencyNode node )
    {
        final Dependency dependency = node.getDependency();
        if ( dependency == null )
        {
            return true;
        }
        return !super.accept( node );
    }

}
