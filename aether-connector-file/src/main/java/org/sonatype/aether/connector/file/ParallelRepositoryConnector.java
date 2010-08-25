package org.sonatype.aether.connector.file;

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

import static org.sonatype.aether.connector.file.FileRepositoryConnectorFactory.*;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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
    private static final String TG_NAME = "FileRepositoryConnector";

    private static final String T_NAME = "FileRepositoryConnectorThread";

    private static final int MAX_POOL_SIZE = 5;

    private static final int INITIAL_POOL_SIZE = 2;

    private static final long KEEPALIVE = 60L;

    private static final long SHUTDOWN_TIMEOUT = 5L;

    private Map<String, Object> config = Collections.emptyMap();

    private long shutdownTimeout;

    private boolean closed = false;

    /**
     * The executor to use.
     * 
     * @see #initExecutor()
     */
    protected static ThreadPoolExecutor executor;

    public ParallelRepositoryConnector( Map<String, Object> config )
    {
        super();
        this.config = config;
        initExecutor();
    }

    protected void initExecutor()
    {
        initExecutor( false );
    }

    protected void initExecutor( boolean forceInit )
    {
        if ( executor == null || forceInit )
        {
            Object tgName = config.get( CFG_PREFIX + ".threads.groupname" );
            Object tName = config.get( CFG_PREFIX + ".threads.name" );
            Object maximumPoolSize = config.get( CFG_PREFIX + ".threads.max" );
            Object initialPoolSize = config.get( CFG_PREFIX + ".threads.initial" );
            Object keepAlive = config.get( CFG_PREFIX + ".threads.keepalive" );
            Object timeoutCfg = config.get( FileRepositoryConnectorFactory.CFG_PREFIX + ".shutdownTime" );

            tgName = tgName != null ? tgName : TG_NAME;
            tName = tName != null ? tName : T_NAME;
            int mPS = maximumPoolSize != null ? Integer.valueOf( maximumPoolSize.toString() ) : MAX_POOL_SIZE;
            int iPS = initialPoolSize != null ? Integer.valueOf( initialPoolSize.toString() ) : INITIAL_POOL_SIZE;
            long kAlive = keepAlive != null ? Long.valueOf( keepAlive.toString() ) : KEEPALIVE;
            shutdownTimeout = timeoutCfg != null ? Long.valueOf( timeoutCfg.toString() ) : SHUTDOWN_TIMEOUT;

            ThreadFactory threadFactory = new RepositoryConnectorThreadFactory( tgName.toString(), tName.toString() );
            BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>();
            TimeUnit timeUnit = TimeUnit.SECONDS;

            executor = new ThreadPoolExecutor( iPS, mPS, kAlive, timeUnit, workQueue, threadFactory );
        }
    }

    public void close()
    {
        this.closed = true;
    }

    protected void checkClosed()
    {
        if ( closed ) {
            throw new IllegalStateException( "Connector is closed" );
        }
        
    }

    protected static class RepositoryConnectorThreadFactory
        implements ThreadFactory
    {

        private final ThreadGroup myTG;

        private final AtomicInteger counter = new AtomicInteger( 1 );

        private String tName;

        public RepositoryConnectorThreadFactory( String tgName, String tName )
        {
            super();

            myTG = new ThreadGroup( Thread.currentThread().getThreadGroup().getParent(), tgName );
            this.tName = tName;
        }

        public Thread newThread( Runnable r )
        {
            Thread t = new Thread( myTG, r, tName + "-" + counter.getAndIncrement() );
            t.setDaemon( true );
            return t;
        }

    }

}
