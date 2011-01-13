package org.sonatype.aether.connector.async;

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

import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.spi.io.FileProcessor;
import org.sonatype.aether.spi.log.Logger;
import org.sonatype.aether.transfer.TransferListener;

import com.ning.http.client.AsyncHttpClient;

/**
 * @author Benjamin Hanzelmann
 */
class ConnectorConfiguration
{

    private AsyncHttpClient httpClient;

    private RemoteRepository repository;

    private FileProcessor fileProcessor;

    private RepositorySystemSession session;

    private TransferListener listener;

    private Logger logger;

    private Map<String, String> checksumAlgos;

    private boolean disableResumeSupport;

    private int maxIOExceptionRetry;

    private boolean useCache;

    public ConnectorConfiguration( AsyncHttpClient httpClient, RemoteRepository repository,
                                   FileProcessor fileProcessor, RepositorySystemSession session, Logger logger,
                                   TransferListener listener, Map<String, String> checksumAlgos,
                                   boolean disableResumeSupport, int maxIOExceptionRetry, boolean useCache )
    {
        this.httpClient = httpClient;
        this.repository = repository;
        this.fileProcessor = fileProcessor;
        this.session = session;
        this.logger = logger;
        this.listener = listener;
        this.checksumAlgos = checksumAlgos;
        this.disableResumeSupport = disableResumeSupport;
        this.maxIOExceptionRetry = maxIOExceptionRetry;
        this.useCache = useCache;
    }

    protected AsyncHttpClient getHttpClient()
    {
        return httpClient;
    }

    protected RemoteRepository getRepository()
    {
        return repository;
    }

    protected FileProcessor getFileProcessor()
    {
        return fileProcessor;
    }

    protected RepositorySystemSession getSession()
    {
        return session;
    }

    protected TransferListener getListener()
    {
        return listener;
    }

    protected Logger getLogger()
    {
        return logger;
    }

    protected Map<String, String> getChecksumAlgos()
    {
        return checksumAlgos;
    }

    protected boolean isDisableResumeSupport()
    {
        return disableResumeSupport;
    }

    protected int getMaxIOExceptionRetry()
    {
        return maxIOExceptionRetry;
    }

    protected boolean isUseCache()
    {
        return useCache;
    }

}
