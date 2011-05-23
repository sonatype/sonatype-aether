package org.sonatype.aether.test.impl;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import org.sonatype.aether.repository.Proxy;
import org.sonatype.aether.repository.ProxySelector;
import org.sonatype.aether.repository.RemoteRepository;

/**
 * A proxy selector which never returns any proxy.
 */
class TestProxySelector
    implements ProxySelector
{


    public Proxy getProxy( RemoteRepository repository )
    {
        return null;
    }



}
