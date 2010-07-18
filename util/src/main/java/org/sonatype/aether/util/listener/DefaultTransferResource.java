package org.sonatype.aether.util.listener;

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

import java.io.File;

import org.sonatype.aether.TransferResource;

/**
 * @author Benjamin Bentmann
 */
public class DefaultTransferResource
    implements TransferResource
{

    private final String repositoryUrl;

    private final String resourceName;

    private final File file;

    private final long startTime;

    private long contentLength = -1;

    public DefaultTransferResource( String repositoryUrl, String resourceName, File file )
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

        this.file = file;

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

    public File getFile()
    {
        return file;
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
