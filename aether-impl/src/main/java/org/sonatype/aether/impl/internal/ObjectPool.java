package org.sonatype.aether.impl.internal;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

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
