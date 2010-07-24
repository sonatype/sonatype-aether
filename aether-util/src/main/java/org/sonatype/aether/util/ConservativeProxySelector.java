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

import org.sonatype.aether.Proxy;
import org.sonatype.aether.ProxySelector;
import org.sonatype.aether.RemoteRepository;

/**
 * A proxy selector that delegates to another selector but only if a repository has no proxy yet. If a proxy has already
 * been assigned to a repository, that is selected.
 * 
 * @author Benjamin Bentmann
 */
public class ConservativeProxySelector
    implements ProxySelector
{

    private final ProxySelector selector;

    /**
     * Creates a new selector that delegates to the specified selector.
     * 
     * @param selector The selector to delegate to in case a repository has no proxy yet, must not be {@code null}.
     */
    public ConservativeProxySelector( ProxySelector selector )
    {
        if ( selector == null )
        {
            throw new IllegalArgumentException( "no proxy selector specified" );
        }
        this.selector = selector;
    }

    public Proxy getProxy( RemoteRepository repository )
    {
        Proxy proxy = repository.getProxy();
        if ( proxy != null )
        {
            return proxy;
        }
        return selector.getProxy( repository );
    }

}
