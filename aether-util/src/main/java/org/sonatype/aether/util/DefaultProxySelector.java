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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import org.sonatype.aether.Proxy;
import org.sonatype.aether.ProxySelector;
import org.sonatype.aether.RemoteRepository;

/**
 * A simple proxy selector that selects the first matching proxy from a list of configured proxies.
 * 
 * @author Benjamin Bentmann
 */
public class DefaultProxySelector
    implements ProxySelector
{

    private List<ProxyDef> proxies = new ArrayList<ProxyDef>();

    /**
     * Adds the specified proxy definition to the selector. Proxy definitions are ordered, the first matching proxy for
     * a given repository will be used.
     * 
     * @param proxy The proxy definition to add, must not be {@code null}.
     * @param nonProxyHosts The list of hosts to exclude from proxying, may be {@code null}.
     * @return This proxy selector for chaining, never {@code null}.
     */
    public DefaultProxySelector add( Proxy proxy, String nonProxyHosts )
    {
        proxies.add( new ProxyDef( proxy, nonProxyHosts ) );

        return this;
    }

    public Proxy getProxy( RemoteRepository repository )
    {
        Map<String, ProxyDef> candidates = new HashMap<String, ProxyDef>();

        String host = repository.getHost();
        for ( ProxyDef proxy : proxies )
        {
            if ( !isNonProxyHosts( host, proxy.nonProxyHosts ) )
            {
                String key = proxy.proxy.getType().toLowerCase( Locale.ENGLISH );
                if ( !candidates.containsKey( key ) )
                {
                    candidates.put( key, proxy );
                }
            }
        }

        String protocol = repository.getProtocol().toLowerCase( Locale.ENGLISH );

        if ( "davs".equals( protocol ) )
        {
            protocol = "https";
        }
        else if ( "dav".equals( protocol ) )
        {
            protocol = "http";
        }
        else if ( protocol.startsWith( "dav:" ) )
        {
            protocol = protocol.substring( "dav:".length() );
        }

        ProxyDef proxy = candidates.get( protocol );
        if ( proxy == null && "https".equals( protocol ) )
        {
            proxy = candidates.get( "http" );
        }

        return ( proxy != null ) ? proxy.proxy : null;
    }

    static boolean isNonProxyHosts( String host, String nonProxyHosts )
    {
        if ( host != null && nonProxyHosts != null && nonProxyHosts.length() > 0 )
        {
            for ( StringTokenizer tokenizer = new StringTokenizer( nonProxyHosts, "|" ); tokenizer.hasMoreTokens(); )
            {
                String pattern = tokenizer.nextToken();
                pattern = pattern.replace( ".", "\\." ).replace( "*", ".*" );
                if ( host.matches( pattern ) )
                {
                    return true;
                }
            }
        }

        return false;
    }

    static class ProxyDef
    {

        final Proxy proxy;

        final String nonProxyHosts;

        public ProxyDef( Proxy proxy, String nonProxyHosts )
        {
            this.proxy = proxy;
            this.nonProxyHosts = nonProxyHosts;
        }

    }

}
