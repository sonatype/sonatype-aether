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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import org.sonatype.aether.Artifact;
import org.sonatype.aether.Dependency;
import org.sonatype.aether.DependencyFilter;
import org.sonatype.aether.DependencyNode;
import org.sonatype.aether.InvalidVersionException;
import org.sonatype.aether.InvalidVersionRangeException;
import org.sonatype.aether.Version;
import org.sonatype.aether.VersionRange;
import org.sonatype.aether.VersionScheme;

/**
 * A simple filter to include artifacts from a list of patterns. The artifact pattern syntax is of the form:
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
public class PatternInclusionsDependencyFilter
    implements DependencyFilter
{

    private final Collection<String> patterns = new HashSet<String>();

    private final VersionScheme versionScheme;

    /**
     * Creates a new filter using the specified patterns.
     * 
     * @param patterns The include patterns, may be {@code null} or empty to include no artifacts.
     */
    public PatternInclusionsDependencyFilter( final String... patterns )
    {
        this( null, patterns );
    }

    /**
     * Creates a new filter using the specified patterns.
     * 
     * @param versionScheme To be used for parsing versions/version ranges. If {@code null} and pattern specifies a
     *            range no artifact will be included.
     * @param patterns The include patterns, may be {@code null} or empty to include no artifacts.
     */
    public PatternInclusionsDependencyFilter( final VersionScheme versionScheme, final String... patterns )
    {
        this( versionScheme, Arrays.asList( patterns ) );
    }

    /**
     * Creates a new filter using the specified patterns.
     * 
     * @param patterns The include patterns, may be {@code null} or empty to include no artifacts.
     */
    public PatternInclusionsDependencyFilter( final Collection<String> patterns )
    {
        this( null, patterns );
    }

    /**
     * Creates a new filter using the specified patterns and {@link VersionScheme} .
     * 
     * @param versionScheme To be used for parsing versions/version ranges. If {@code null} and pattern specifies a
     *            range no artifact will be included.
     * @param patterns The include patterns, may be {@code null} or empty to include no artifacts.
     */
    public PatternInclusionsDependencyFilter( final VersionScheme versionScheme, final Collection<String> patterns )
    {
        if ( patterns != null )
        {
            this.patterns.addAll( patterns );
        }
        this.versionScheme = versionScheme;
    }

    public boolean accept( final DependencyNode node )
    {
        final Dependency dependency = node.getDependency();
        if ( dependency == null )
        {
            return true;
        }
        final Artifact artifact = dependency.getArtifact();
        for ( final String pattern : patterns )
        {
            final boolean matched = accept( artifact, pattern );
            if ( matched )
            {
                return true;
            }
        }
        return false;
    }

    private boolean accept( final Artifact artifact, final String pattern )
    {
        final String[] tokens =
            new String[] { artifact.getGroupId(), artifact.getArtifactId(), artifact.getExtension(),
                artifact.getBaseVersion() };

        final String[] patternTokens = pattern.split( ":" );

        // fail immediately if pattern tokens outnumber tokens to match
        boolean matched = ( patternTokens.length <= tokens.length );

        for ( int i = 0; matched && i < patternTokens.length; i++ )
        {
            matched = matches( tokens[i], patternTokens[i] );
        }

        return matched;
    }

    private boolean matches( final String token, final String pattern )
    {
        boolean matches;

        // support full wildcard and implied wildcard
        if ( "*".equals( pattern ) || pattern.length() == 0 )
        {
            matches = true;
        }
        // support contains wildcard
        else if ( pattern.startsWith( "*" ) && pattern.endsWith( "*" ) )
        {
            final String contains = pattern.substring( 1, pattern.length() - 1 );

            matches = ( token.indexOf( contains ) != -1 );
        }
        // support leading wildcard
        else if ( pattern.startsWith( "*" ) )
        {
            final String suffix = pattern.substring( 1, pattern.length() );

            matches = token.endsWith( suffix );
        }
        // support trailing wildcard
        else if ( pattern.endsWith( "*" ) )
        {
            final String prefix = pattern.substring( 0, pattern.length() - 1 );

            matches = token.startsWith( prefix );
        }
        // support versions range
        else if ( pattern.startsWith( "[" ) || pattern.startsWith( "(" ) )
        {
            matches = isVersionIncludedInRange( token, pattern );
        }
        // support exact match
        else
        {
            matches = token.equals( pattern );
        }

        return matches;
    }

    private boolean isVersionIncludedInRange( final String version, final String range )
    {
        if ( versionScheme == null )
        {
            return false;
        }
        else
        {
            try
            {
                final Version parsedVersion = versionScheme.parseVersion( version );
                final VersionRange parsedRange = versionScheme.parseVersionRange( range );

                return parsedRange.containsVersion( parsedVersion );
            }
            catch ( final InvalidVersionException e )
            {
                return false;
            }
            catch ( final InvalidVersionRangeException e )
            {
                return false;
            }
        }
    }

    @Override
    public boolean equals( final Object obj )
    {
        if ( this == obj )
        {
            return true;
        }

        if ( obj == null || !getClass().equals( obj.getClass() ) )
        {
            return false;
        }

        final PatternInclusionsDependencyFilter that = (PatternInclusionsDependencyFilter) obj;

        return this.patterns.equals( that.patterns )
            && ( this.versionScheme == null ? that.versionScheme == null
                            : this.versionScheme.equals( that.versionScheme ) );
    }

    @Override
    public int hashCode()
    {
        int hash = 17;
        hash = hash * 31 + patterns.hashCode();
        hash = hash * 31 + ( ( versionScheme == null ) ? 0 : versionScheme.hashCode() );
        return hash;
    }

}
