package org.sonatype.aether.impl.internal;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.aether.repository.LocalRepository;
import org.sonatype.aether.repository.LocalRepositoryManager;
import org.sonatype.aether.repository.NoLocalRepositoryManagerException;
import org.sonatype.aether.spi.localrepo.LocalRepositoryManagerFactory;

/**
 * Creates {@link SimpleLocalRepositoryManager}s for repository type {@code "simple"}.
 * 
 * @author Benjamin Hanzelmann
 */
@Component( role = LocalRepositoryManagerFactory.class, hint = "simple" )
public class SimpleLocalRepositoryManagerFactory
    implements LocalRepositoryManagerFactory
{

    public LocalRepositoryManager newInstance( LocalRepository repository )
        throws NoLocalRepositoryManagerException
    {
        if ( "".equals( repository.getContentType() ) || "simple".equals( repository.getContentType() ) )
        {
            return new SimpleLocalRepositoryManager( repository.getBasedir() );
        }
        else
        {
            throw new NoLocalRepositoryManagerException( repository );
        }
    }

    public int getPriority()
    {
        return 0;
    }

}
