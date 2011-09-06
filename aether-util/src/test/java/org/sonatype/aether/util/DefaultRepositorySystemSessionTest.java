package org.sonatype.aether.util;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import static org.junit.Assert.*;

import org.junit.Test;
import org.sonatype.aether.repository.Authentication;
import org.sonatype.aether.repository.Proxy;
import org.sonatype.aether.repository.RemoteRepository;

/**
 */
public class DefaultRepositorySystemSessionTest
{

    @Test
    public void testDefaultProxySelectorUsesExistingProxy()
    {
        DefaultRepositorySystemSession session = new DefaultRepositorySystemSession();

        RemoteRepository repo = new RemoteRepository( "id", "default", "void" );
        assertSame( null, session.getProxySelector().getProxy( repo ) );

        repo.setProxy( new Proxy( "http", "localhost", 8080, null ) );
        assertSame( repo.getProxy(), session.getProxySelector().getProxy( repo ) );
    }

    @Test
    public void testDefaultAuthenticationSelectorUsesExistingAuth()
    {
        DefaultRepositorySystemSession session = new DefaultRepositorySystemSession();

        RemoteRepository repo = new RemoteRepository( "id", "default", "void" );
        assertSame( null, session.getAuthenticationSelector().getAuthentication( repo ) );

        repo.setAuthentication( new Authentication( "user", "pass" ) );
        assertSame( repo.getAuthentication(), session.getAuthenticationSelector().getAuthentication( repo ) );
    }

}
