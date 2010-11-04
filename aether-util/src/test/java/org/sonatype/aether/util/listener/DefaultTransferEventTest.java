package org.sonatype.aether.util.listener;

/*******************************************************************************
 * Copyright (c) 2010 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import java.nio.ByteBuffer;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Benjamin Hanzelmann
 *
 */
public class DefaultTransferEventTest
{

    @Test
    public void testByteArrayConversion()
    {

        DefaultTransferEvent event = new DefaultTransferEvent();
        byte[] buffer = new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
        int length = buffer.length - 2;
        int offset = 1;
        event.setDataBuffer( buffer, offset, length );

        ByteBuffer bb = event.getDataBuffer();
        byte[] dst = new byte[bb.remaining()];
        bb.get( dst );

        byte[] expected = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8 };
        Assert.assertArrayEquals( expected, dst );
    }

}
