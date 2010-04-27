package org.apache.maven.repository;

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
public class DefaultTransferResource
    implements TransferResource
{

    private final String repositoryUrl;

    private final String resourceName;

    private final long startTime;

    private long contentLength = -1;

    public DefaultTransferResource( String repositoryUrl, String resourceName )
    {
        if ( repositoryUrl == null || repositoryUrl.length() <= 0 )
        {
            this.repositoryUrl = "";
        }
        else if ( repositoryUrl.endsWith( "/" ) )
        {
            this.repositoryUrl = repositoryUrl;
        }
        else
        {
            this.repositoryUrl = repositoryUrl + '/';
        }

        if ( resourceName == null || resourceName.length() <= 0 )
        {
            this.resourceName = "";
        }
        else if ( resourceName.startsWith( "/" ) )
        {
            this.resourceName = resourceName.substring( 1 );
        }
        else
        {
            this.resourceName = resourceName;
        }

        startTime = System.currentTimeMillis();
    }

    public String getRepositoryUrl()
    {
        return repositoryUrl;
    }

    public String getResourceName()
    {
        return resourceName;
    }

    public long getContentLength()
    {
        return contentLength;
    }

    public void setContentLength( long contentLength )
    {
        this.contentLength = contentLength;
    }

    public long getTransferStartTime()
    {
        return startTime;
    }

}
