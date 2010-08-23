package org.sonatype.aether.test.impl;

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
