package org.sonatype.aether.impl;

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

import org.sonatype.aether.RepositoryEvent;

/**
 * Dispatches repository events to registered listeners.
 * 
 * @author Benjamin Bentmann
 */
public interface RepositoryEventDispatcher
{

    /**
     * Dispatches the specified repository event to all registered listeners.
     * 
     * @param event The event to dispatch, must not be {@code null}.
     */
    void dispatch( RepositoryEvent event );

}
