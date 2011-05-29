package org.sonatype.aether.connector.file;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import static org.sonatype.aether.connector.file.FileRepositoryConnectorFactory.*;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.sonatype.aether.util.ConfigUtils;

/**
 * Provides methods to configure the used {@link ThreadPoolExecutor}.
 * 
 * @author Benjamin Hanzelmann
 */
abstract class ParallelRepositoryConnector
{
    /*
     * Default Configuration
     */
    private static final String THREAD_NAME = "FileRepositoryConnector";

    private static final int MAX_POOL_SIZE = 5;

    private static final int INITIAL_POOL_SIZE = 2;

    private static final long KEEPALIVE = 60L;

    private boolean closed = false;

    /**
     * The executor to use.
     * 
     * @see #initExecutor()
     */
    protected Executor executor;

    protected void initExecutor( Map<String, Object> config )
    {
        if ( executor == null )
        {
            String threadName = ConfigUtils.getString( config, THREAD_NAME, CFG_PREFIX + ".threads.name" );
            int maximumPoolSize = ConfigUtils.getInteger( config, MAX_POOL_SIZE, CFG_PREFIX + ".threads.max" );
            int initialPoolSize = ConfigUtils.getInteger( config, INITIAL_POOL_SIZE, CFG_PREFIX + ".threads.initial" );
            long keepAlive = ConfigUtils.getLong( config, KEEPALIVE, CFG_PREFIX + ".threads.keepalive" );

            if ( maximumPoolSize <= 1 )
            {
                executor = new Executor()
                {
                    public void execute( Runnable command )
                    {
                        command.run();
                    }
                };
            }
            else
            {

                ThreadFactory threadFactory = new RepositoryConnectorThreadFactory( threadName.toString() );
                BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>();
                TimeUnit timeUnit = TimeUnit.SECONDS;

                executor =
                    new ThreadPoolExecutor( initialPoolSize, maximumPoolSize, keepAlive, timeUnit, workQueue,
                                            threadFactory );
            }
        }
    }

    public void close()
    {
        this.closed = true;

        if ( executor instanceof ExecutorService )
        {
            ( (ExecutorService) executor ).shutdown();
        }
    }

    protected void checkClosed()
    {
        if ( closed )
        {
            throw new IllegalStateException( "Connector is closed" );
        }
    }

    protected static class RepositoryConnectorThreadFactory
        implements ThreadFactory
    {

        private final AtomicInteger counter = new AtomicInteger( 1 );

        private String threadName;

        public RepositoryConnectorThreadFactory( String threadName )
        {
            this.threadName = threadName;
        }

        public Thread newThread( Runnable r )
        {
            Thread t = new Thread( r, threadName + "-" + counter.getAndIncrement() );
            t.setDaemon( true );
            return t;
        }

    }

}
