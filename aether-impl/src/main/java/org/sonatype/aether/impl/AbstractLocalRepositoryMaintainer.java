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

/**
 * A skeleton implementation for custom local repository maintainers. The callback methods in this class do nothing.
 * 
 * @author Benjamin Bentmann
 */
@Deprecated
public abstract class AbstractLocalRepositoryMaintainer
    implements LocalRepositoryMaintainer
{

    public void artifactInstalled( LocalRepositoryEvent event )
    {
    }

    public void artifactDownloaded( LocalRepositoryEvent event )
    {
    }

}
