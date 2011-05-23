package org.sonatype.aether.util.listener;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import static org.junit.Assert.*;

import java.lang.reflect.Method;

import org.junit.Test;
import org.sonatype.aether.RepositoryListener;

/**
 * @author Benjamin Bentmann
 */
public class ChainedRepositoryListenerTest
{

    @Test
    public void testAllEventTypesHandled()
        throws Exception
    {
        for ( Method method : RepositoryListener.class.getMethods() )
        {
            assertNotNull( ChainedRepositoryListener.class.getDeclaredMethod( method.getName(),
                                                                              method.getParameterTypes() ) );
        }
    }

}
