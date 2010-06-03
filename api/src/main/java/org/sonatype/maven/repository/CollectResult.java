package org.sonatype.maven.repository;

import java.util.ArrayList;
import java.util.List;

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

/**
 * @author Benjamin Bentmann
 */
public class CollectResult
{

    private final CollectRequest request;

    private final List<Exception> exceptions;

    private DependencyNode root;

    public CollectResult( CollectRequest request )
    {
        if ( request == null )
        {
            throw new IllegalArgumentException( "dependency collection request has not been specified" );
        }
        this.request = request;
        this.exceptions = new ArrayList<Exception>( 4 );
    }

    public CollectRequest getRequest()
    {
        return request;
    }

    public List<Exception> getExceptions()
    {
        return exceptions;
    }

    public CollectResult addException( Exception exception )
    {
        if ( exception != null )
        {
            this.exceptions.add( exception );
        }
        return this;
    }

    public DependencyNode getRoot()
    {
        return root;
    }

    public CollectResult setRoot( DependencyNode root )
    {
        this.root = root;
        return this;
    }

}
