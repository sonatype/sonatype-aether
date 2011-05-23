package org.sonatype.aether.impl;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.collection.CollectRequest;
import org.sonatype.aether.collection.CollectResult;
import org.sonatype.aether.collection.DependencyCollectionException;

/**
 * This collector fulfills the contract of
 * {@link RepositorySystem#collectDependencies(RepositorySystemSession, CollectRequest)}.
 * 
 * @author Benjamin Bentmann
 */
public interface DependencyCollector
{

    /**
     * @see RepositorySystem#collectDependencies(RepositorySystemSession, CollectRequest)
     */
    CollectResult collectDependencies( RepositorySystemSession session, CollectRequest request )
        throws DependencyCollectionException;

}
