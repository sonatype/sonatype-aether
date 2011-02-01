package org.sonatype.aether.connector.async;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *******************************************************************************/

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
