package org.sonatype.aether.util.repository;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *******************************************************************************/

import java.util.HashMap;
import java.util.Map;

import org.sonatype.aether.repository.Authentication;
import org.sonatype.aether.repository.AuthenticationSelector;
import org.sonatype.aether.repository.RemoteRepository;

/**
 * A simple authentication selector that selects authentication based on repository identifiers.
 * 
 * @author Benjamin Bentmann
 */
public class DefaultAuthenticationSelector
    implements AuthenticationSelector
{

    private final Map<String, Authentication> repos = new HashMap<String, Authentication>();

    /**
     * Adds the specified authentication info for the given repository identifier.
     * 
     * @param id The identifier of the repository to add the authentication for, must not be {@code null}.
     * @param auth The authentication to add, must not be {@code null}.
     * @return This selector for chaining, never {@code null}.
     */
    public DefaultAuthenticationSelector add( String id, Authentication auth )
    {
        repos.put( id, auth );

        return this;
    }

    public Authentication getAuthentication( RemoteRepository repository )
    {
        return repos.get( repository.getId() );
    }

}
