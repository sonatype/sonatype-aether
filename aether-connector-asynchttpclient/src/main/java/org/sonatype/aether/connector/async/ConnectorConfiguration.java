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

import com.ning.http.client.SimpleAsyncHttpClient;

/**
 * @author Benjamin Hanzelmann
 */
class ConnectorConfiguration
{

    private SimpleAsyncHttpClient httpClient;

    private RemoteRepository repository;

    private FileProcessor fileProcessor;

    private RepositorySystemSession session;

    private TransferListener listener;

    private Logger logger;

    private Map<String, String> checksumAlgos;

    private boolean disableResumeSupport;

    public ConnectorConfiguration( SimpleAsyncHttpClient httpClient, RemoteRepository repository,
                                   FileProcessor fileProcessor, RepositorySystemSession session, Logger logger,
                                   TransferListener listener, Map<String, String> checksumAlgos,
                                   boolean disableResumeSupport )
    {
        this.httpClient = httpClient;
        this.repository = repository;
        this.fileProcessor = fileProcessor;
        this.session = session;
        this.logger = logger;
        this.listener = listener;
        this.checksumAlgos = checksumAlgos;
        this.disableResumeSupport = disableResumeSupport;
    }

    public SimpleAsyncHttpClient getHttpClient()
    {
        return httpClient;
    }

    public RemoteRepository getRepository()
    {
        return repository;
    }

    public FileProcessor getFileProcessor()
    {
        return fileProcessor;
    }

    public RepositorySystemSession getSession()
    {
        return session;
    }

    public TransferListener getListener()
    {
        return listener;
    }

    public Logger getLogger()
    {
        return logger;
    }

    public Map<String, String> getChecksumAlgos()
    {
        return checksumAlgos;
    }

    public boolean isDisableResumeSupport()
    {
        return disableResumeSupport;
    }

}
