package org.sonatype.aether.transfer;

/*
 * Copyright (c) 2010 Sonatype, Inc. All rights reserved.
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

import static org.junit.Assert.*;

import java.lang.reflect.Method;

import org.junit.Test;

/**
 * @author Benjamin Bentmann
 */
public class AbstractTransferListenerTest
{

    @Test
    public void testAllEventTypesHandled()
        throws Exception
    {
        for ( Method method : TransferListener.class.getMethods() )
        {
            assertNotNull( AbstractTransferListener.class.getDeclaredMethod( method.getName(),
                                                                             method.getParameterTypes() ) );
        }
    }

}
