package org.apache.maven.repository;

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
import java.util.List;

/**
 * @author Benjamin Bentmann
 */
public class DefaultMirrorSelector
    implements MirrorSelector
{

    private static final String WILDCARD = "*";

    private static final String EXTERNAL_WILDCARD = "external:*";

    private final List<MirrorDef> mirrors = new ArrayList<MirrorDef>();

    public DefaultMirrorSelector add( String id, String url, String type, String mirrorOfIds, String mirrorOfTypes )
    {
        mirrors.add( new MirrorDef( id, url, type, mirrorOfIds, mirrorOfTypes ) );

        return this;
    }

    public RemoteRepository getMirror( RemoteRepository repository )
    {
        MirrorDef mirror = findMirror( repository );

        if ( mirror == null )
        {
            return null;
        }

        RemoteRepository repo = new RemoteRepository( repository );

        repo.setId( mirror.id );
        repo.setUrl( mirror.url );
        if ( mirror.type != null && mirror.type.length() > 0 )
        {
            repo.setType( mirror.type );
        }

        return repo;
    }

    private MirrorDef findMirror( RemoteRepository repository )
    {
        String repoId = repository.getId();

        if ( repoId != null && !mirrors.isEmpty() )
        {
            for ( MirrorDef mirror : mirrors )
            {
                if ( repoId.equals( mirror.mirrorOfIds ) && matchesType( repository.getType(), mirror.mirrorOfTypes ) )
                {
                    return mirror;
                }
            }

            for ( MirrorDef mirror : mirrors )
            {
                if ( matchPattern( repository, mirror.mirrorOfIds )
                    && matchesType( repository.getType(), mirror.mirrorOfTypes ) )
                {
                    return mirror;
                }
            }
        }

        return null;
    }

    /**
     * This method checks if the pattern matches the originalRepository. Valid patterns: * = everything external:* =
     * everything not on the localhost and not file based. repo,repo1 = repo or repo1 *,!repo1 = everything except repo1
     * 
     * @param repository to compare for a match.
     * @param pattern used for match. Currently only '*' is supported.
     * @return true if the repository is a match to this pattern.
     */
    static boolean matchPattern( RemoteRepository repository, String pattern )
    {
        boolean result = false;
        String originalId = repository.getId();

        // simple checks first to short circuit processing below.
        if ( WILDCARD.equals( pattern ) || pattern.equals( originalId ) )
        {
            result = true;
        }
        else
        {
            // process the list
            String[] repos = pattern.split( "," );
            for ( String repo : repos )
            {
                // see if this is a negative match
                if ( repo.length() > 1 && repo.startsWith( "!" ) )
                {
                    if ( repo.substring( 1 ).equals( originalId ) )
                    {
                        // explicitly exclude. Set result and stop processing.
                        result = false;
                        break;
                    }
                }
                // check for exact match
                else if ( repo.equals( originalId ) )
                {
                    result = true;
                    break;
                }
                // check for external:*
                else if ( EXTERNAL_WILDCARD.equals( repo ) && isExternalRepo( repository ) )
                {
                    result = true;
                    // don't stop processing in case a future segment explicitly excludes this repo
                }
                else if ( WILDCARD.equals( repo ) )
                {
                    result = true;
                    // don't stop processing in case a future segment explicitly excludes this repo
                }
            }
        }
        return result;
    }

    /**
     * Checks the URL to see if this repository refers to an external repository.
     * 
     * @param repository The repository to check, must not be {@code null}.
     * @return {@code true} if external, {@code false} otherwise.
     */
    static boolean isExternalRepo( RemoteRepository repository )
    {
        boolean local =
            "localhost".equals( repository.getHost() ) || "127.0.0.1".equals( repository.getHost() )
                || "file".equalsIgnoreCase( repository.getProtocol() );
        return !local;
    }

    /**
     * Checks whether the types configured for a mirror match with the type of the repository.
     * 
     * @param repoType The type of the repository, may be {@code null}.
     * @param mirrorType The types supported by the mirror, may be {@code null}.
     * @return {@code true} if the types associated with the mirror match the type of the original repository, {@code
     *         false} otherwise.
     */
    static boolean matchesType( String repoType, String mirrorType )
    {
        boolean result = false;

        // simple checks first to short circuit processing below.
        if ( mirrorType == null || mirrorType.length() <= 0 || WILDCARD.equals( mirrorType ) )
        {
            result = true;
        }
        else if ( mirrorType.equals( repoType ) )
        {
            result = true;
        }
        else
        {
            // process the list
            String[] layouts = mirrorType.split( "," );
            for ( String layout : layouts )
            {
                // see if this is a negative match
                if ( layout.length() > 1 && layout.startsWith( "!" ) )
                {
                    if ( layout.substring( 1 ).equals( repoType ) )
                    {
                        // explicitly exclude. Set result and stop processing.
                        result = false;
                        break;
                    }
                }
                // check for exact match
                else if ( layout.equals( repoType ) )
                {
                    result = true;
                    break;
                }
                else if ( WILDCARD.equals( layout ) )
                {
                    result = true;
                    // don't stop processing in case a future segment explicitly excludes this repo
                }
            }
        }

        return result;
    }

    static class MirrorDef
    {

        final String id;

        final String url;

        final String type;

        final String mirrorOfIds;

        final String mirrorOfTypes;

        public MirrorDef( String id, String url, String type, String mirrorOfIds, String mirrorOfTypes )
        {
            this.id = id;
            this.url = url;
            this.type = type;
            this.mirrorOfIds = mirrorOfIds;
            this.mirrorOfTypes = mirrorOfTypes;
        }

    }

}
