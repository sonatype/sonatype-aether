package org.sonatype.aether.repository;

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

/**
 * Selects a mirror for a given remote repository.
 * 
 * @author Benjamin Bentmann
 */
public interface MirrorSelector
{

    /**
     * Selects a mirror for the specified repository.
     * 
     * @param repository The repository to select a mirror for, must not be {@code null}.
     * @return The selected mirror or {@code null} if none.
     */
    RemoteRepository getMirror( RemoteRepository repository );

}
