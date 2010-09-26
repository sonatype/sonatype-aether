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

import org.sonatype.aether.transfer.TransferResource;

/**
 * A simple transfer resource.
 * 
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

    /**
     * Creates a new transfer resource with the specified properties.
     * 
     * @param repositoryUrl The base URL of the repository, may be {@code null} or empty if unknown. If not empty, a
     *            trailing slash will automatically be added if missing.
     * @param resourceName The relative path to the resource within the repository, may be {@code null}. A leading slash
     *            (if any) will be automatically removed.
     * @param file The source/target file involved in the transfer, may be {@code null}.
     */
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

    /**
     * Sets the size of the resource in bytes.
     * 
     * @param contentLength The size of the resource in bytes or a negative value if unknown.
     * @return This resource for chaining, never {@code null}.
     */
    public DefaultTransferResource setContentLength( long contentLength )
    {
        this.contentLength = contentLength;
        return this;
    }

    public long getTransferStartTime()
    {
        return startTime;
    }

    @Override
    public String toString()
    {
        return getRepositoryUrl() + getResourceName() + " <> " + getFile();
    }

}
