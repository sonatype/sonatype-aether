package org.sonatype.maven.repository.util;

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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.sonatype.maven.repository.RepositoryCache;
import org.sonatype.maven.repository.RepositorySystemSession;

/**
 * A simplistic repository cache backed by a {@link ConcurrentHashMap}. The simplistic nature of this cache makes it
 * only suitable for use with short-lived repository system sessions.
 * 
 * @author Benjamin Bentmann
 */
public class DefaultRepositoryCache
    implements RepositoryCache
{

    private Map<Object, Object> cache = new ConcurrentHashMap<Object, Object>( 256 );

    public Object get( RepositorySystemSession session, Object key )
    {
        return cache.get( key );
    }

    public void put( RepositorySystemSession session, Object key, Object data )
    {
        if ( data != null )
        {
            cache.put( key, data );
        }
    }

}
