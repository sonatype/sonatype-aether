package org.sonatype.aether.util.repository;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import org.sonatype.aether.repository.Authentication;
import org.sonatype.aether.repository.AuthenticationSelector;
import org.sonatype.aether.repository.RemoteRepository;

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
