package org.sonatype.aether.impl.internal;

/*
 * Copyright (c) 2010-2011 Sonatype, Inc. All rights reserved.
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

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.aether.repository.LocalRepository;
import org.sonatype.aether.repository.LocalRepositoryManager;
import org.sonatype.aether.repository.NoLocalRepositoryManagerException;
import org.sonatype.aether.spi.localrepo.LocalRepositoryManagerFactory;

/**
 * Creates {@link EnhancedLocalRepositoryManager}s for repository types {@code "default"} or {@code "" (automatic)}.
 * 
 * @author Benjamin Hanzelmann
 */
@Component( role = LocalRepositoryManagerFactory.class, hint = "enhanced" )
public class EnhancedLocalRepositoryManagerFactory
    implements LocalRepositoryManagerFactory
{

    public LocalRepositoryManager newInstance( LocalRepository repository )
        throws NoLocalRepositoryManagerException
    {
        if ( "".equals( repository.getContentType() ) || "default".equals( repository.getContentType() ) )
        {
            return new EnhancedLocalRepositoryManager( repository.getBasedir() );
        }
        else
        {
            throw new NoLocalRepositoryManagerException( repository );
        }
    }

    public int getPriority()
    {
        return 10;
    }

}
