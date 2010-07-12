package org.sonatype.maven.repository.internal;

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

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Pool of immutable object instances, used to avoid excessive memory consumption of dependency graph.
 * 
 * @author Benjamin Bentmann
 */
class ObjectPool<T>
{

    private final Map<Object, WeakReference<T>> objects = new WeakHashMap<Object, WeakReference<T>>( 256 );

    public synchronized T intern( T object )
    {
        WeakReference<T> pooledRef = objects.get( object );
        if ( pooledRef != null )
        {
            T pooled = pooledRef.get();
            if ( pooled != null )
            {
                return pooled;
            }
        }

        objects.put( object, new WeakReference<T>( object ) );
        return object;
    }

}
