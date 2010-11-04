package org.sonatype.aether.test.impl;

/*******************************************************************************
 * Copyright (c) 2010 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.sonatype.aether.SessionData;

/**
 * A simple session data storage backed by a {@link ConcurrentHashMap}.
 * 
 * @author Benjamin Bentmann
 */
class TestSessionData
    implements SessionData
{

    private Map<Object, Object> data;

    public TestSessionData()
    {
        data = new ConcurrentHashMap<Object, Object>();
    }

    public void set( Object key, Object value )
    {
        if ( value != null )
        {
            data.put( key, value );
        }
        else
        {
            data.remove( key );
        }
    }

    public Object get( Object key )
    {
        return data.get( key );
    }

}
