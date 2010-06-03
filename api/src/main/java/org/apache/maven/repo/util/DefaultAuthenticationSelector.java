package org.apache.maven.repo.util;

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

import java.util.HashMap;
import java.util.Map;

import org.apache.maven.repo.Authentication;
import org.apache.maven.repo.AuthenticationSelector;
import org.apache.maven.repo.RemoteRepository;

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
