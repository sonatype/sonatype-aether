package org.sonatype.aether.util;

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

import org.sonatype.aether.Authentication;
import org.sonatype.aether.AuthenticationSelector;
import org.sonatype.aether.RemoteRepository;

/**
 * An authentication selector that delegates to another selector but only if a repository has no authentication data
 * yet. If authentication has already been assigned to a repository, that is selected.
 * 
 * @author Benjamin Bentmann
 */
public class ConservativeAuthenticationSelector
    implements AuthenticationSelector
{

    private final AuthenticationSelector selector;

    /**
     * Creates a new selector that delegates to the specified selector.
     * 
     * @param selector The selector to delegate to in case a repository has no authentication yet, must not be {@code
     *            null}.
     */
    public ConservativeAuthenticationSelector( AuthenticationSelector selector )
    {
        if ( selector == null )
        {
            throw new IllegalArgumentException( "no authentication selector specified" );
        }
        this.selector = selector;
    }

    public Authentication getAuthentication( RemoteRepository repository )
    {
        Authentication auth = repository.getAuthentication();
        if ( auth != null )
        {
            return auth;
        }
        return selector.getAuthentication( repository );
    }

}
