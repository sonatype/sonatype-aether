package org.sonatype.aether.test.impl;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import org.sonatype.aether.repository.Authentication;
import org.sonatype.aether.repository.AuthenticationSelector;
import org.sonatype.aether.repository.RemoteRepository;

/**
 * A simple authentication selector that never provides authentication.
 */
class TestAuthenticationSelector
    implements AuthenticationSelector
{

    public Authentication getAuthentication( RemoteRepository repository )
    {
        return null;
    }

}
