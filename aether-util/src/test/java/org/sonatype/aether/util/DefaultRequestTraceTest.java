package org.sonatype.aether.util;

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

import static org.junit.Assert.*;

import org.junit.Test;
import org.sonatype.aether.RequestTrace;

/**
 * @author Benjamin Bentmann
 */
public class DefaultRequestTraceTest
{

    @Test
    public void testConstructor()
    {
        DefaultRequestTrace trace = new DefaultRequestTrace( null );
        assertSame( null, trace.getData() );

        trace = new DefaultRequestTrace( this );
        assertSame( this, trace.getData() );
    }

    @Test
    public void testParentChaining()
    {
        RequestTrace trace1 = new DefaultRequestTrace( null );
        RequestTrace trace2 = trace1.newChild( this );

        assertSame( null, trace1.getParent() );
        assertSame( null, trace1.getData() );
        assertSame( trace1, trace2.getParent() );
        assertSame( this, trace2.getData() );
    }

    @Test
    public void testNewChildRequestTrace()
    {
        RequestTrace trace = DefaultRequestTrace.newChild( null, this );
        assertNotNull( trace );
        assertSame( null, trace.getParent() );
        assertSame( this, trace.getData() );
    }

}
